package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlGameDao implements GameDao {
    private static final String table = "GameData";
    private static final String gameId = "GameData.gameId";
    private static final String whiteUsername = "GameData.whiteUsername";
    private static final String blackUsername = "GameData.blackUsername";
    private static final String gameName = "GameData.gameName";

    public SqlGameDao() throws DataAccessException {
        try {
            configureDatabase();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        try {
            String statment = String.format("SELECT * FROM %s", table);
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(statment)) {
                    try (var rs = ps.executeQuery()) {
                        var games = new ArrayList<GameData>();
                        while (rs.next()) {
                            games.add(new GameData(
                                    rs.getInt("gameId"),
                                    rs.getString("whiteUsername"),
                                    rs.getString("blackUsername"),
                                    rs.getString("gameName"),
                                    new Gson().fromJson(rs.getString("game"), ChessGame.class)
                            ));
                        }
                        return games;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Integer createGame(String gameName) throws DataAccessException {
        try {
            ChessGame game = new ChessGame();
            return executeUpdate("INSERT INTO GameData (gameName, game) VALUES (?, ?)", gameName, game.toString());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try {
            executeUpdate("UPDATE GameData SET whiteUsername = ?, blackUsername = ?, gameName = ?, game = ? WHERE gameId = ?",
                    game.whiteUsername(), game.blackUsername(), game.gameName(), game.game(), game.gameID());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public GameData getGameById(Integer gameId) throws DataAccessException {
        try {
            String statment = String.format("SELECT * FROM %s WHERE %s = ?", table, gameId);
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(statment)) {
                    ps.setInt(1, gameId);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new GameData(
                                    rs.getInt("gameId"),
                                    rs.getString("whiteUsername"),
                                    rs.getString("blackUsername"),
                                    rs.getString("gameName"),
                                    new Gson().fromJson(rs.getString("game"), ChessGame.class)
                            );
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
        try {
            executeUpdate("DELETE FROM GameData");
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }

    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof ChessGame p) ps.setString(i + 1, new Gson().toJson(p));
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                var rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createGameTableStatment) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createGameTableStatment = {
            """
        CREATE TABLE IF NOT EXISTS GameData (
          `gameId` int NOT NULL AUTO_INCREMENT,
          `whiteUsername` varchar(256),
          `blackUsername` varchar(256),
          `gameName` varchar(256) NOT NULL,
          'game' varchar(256) NOT NULL,
          PRIMARY KEY (`gameId`),
          INDEX(gameName),
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };
}
