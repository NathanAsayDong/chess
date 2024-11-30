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
    private static final String TABLE = "GameData";

    public SqlGameDao() throws DataAccessException {
        try {
            configureDatabase();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        String statement = String.format("SELECT * FROM %s", TABLE);
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement);
             var rs = ps.executeQuery()) {
            var games = new ArrayList<GameData>();
            Gson gson = new Gson();
            while (rs.next()) {
                games.add(new GameData(
                        rs.getInt("gameId"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gson.fromJson(rs.getString("game"), ChessGame.class)
                ));
            }
            return games;
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public Integer createGame(String gameName) throws DataAccessException {
        try {
            ChessGame game = new ChessGame();
            if (gameName == null || gameName.isEmpty()) {
                throw new DataAccessException("Error: game name cannot be null");
            }
            return executeUpdate("INSERT INTO GameData (gameName, game) VALUES (?, ?)", gameName, new Gson().toJson(game));
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
        String statement = String.format("SELECT * FROM %s WHERE gameId = ?", TABLE);
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setInt(1, gameId);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    Gson gson = new Gson();
                    return new GameData(
                            rs.getInt("gameId"),
                            rs.getString("whiteUsername"),
                            rs.getString("blackUsername"),
                            rs.getString("gameName"),
                            gson.fromJson(rs.getString("game"), ChessGame.class)
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }

    @Override
    public void deleteGameById(Integer gameId) throws DataAccessException {
        try {
            executeUpdate("DELETE FROM GameData WHERE gameId = ?", gameId);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
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
        try {
            SqlExecuteUpdate update = new SqlExecuteUpdate();
            return update.executeUpdate(statement, params);
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
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
          `game` TEXT NOT NULL,
          PRIMARY KEY (`gameId`),
          INDEX(gameName)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };
}
