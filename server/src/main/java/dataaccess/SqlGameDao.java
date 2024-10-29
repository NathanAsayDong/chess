package dataaccess;

import model.GameData;

import java.sql.SQLException;
import java.util.List;

public class SqlGameDao implements GameDao {
    private static final String table = "GameData";
    private static final String gameId = "GameData.gameId";
    private static final String whiteUsername = "GameData.whiteUsername";
    private static final String blackUsername = "GameData.blackUsername";
    private static final String gameName = "GameData.gameName";

    @Override
    public List<GameData> getAllGames() throws DataAccessException {
        return List.of();
    }

    @Override
    public Integer createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGameById(Integer gameId) {
        return null;
    }

    @Override
    public void clear() {

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
          'game'
          PRIMARY KEY (`gameId`),
          INDEX(gameName),
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };
}
