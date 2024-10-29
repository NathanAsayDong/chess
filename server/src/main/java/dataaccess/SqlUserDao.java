package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;

public class SqlUserDao implements UserDao {
    @Override
    public AuthData createUser(UserData user) throws DataAccessException {
        return null;
    }

    @Override
    public AuthData login(UserData user) throws DataAccessException {
        return null;
    }

    @Override
    public void logout(AuthData authData) throws DataAccessException {

    }

    @Override
    public AuthData getAuthByToken(String token) throws DataAccessException {
        return null;
    }

    @Override
    public UserData getUserDataByUserData(UserData user) throws DataAccessException {
        return null;
    }

    @Override
    public void clear() throws DataAccessException {
    }

    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createUserDataStatment) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
            for (var statement : createUserAuthStatment) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    private final String[] createUserDataStatment = {
        """
        CREATE TABLE IF NOT EXISTS UserData (
          `id` int NOT NULL AUTO_INCREMENT,
          `username` varchar(256) NOT NULL,
          `email` varchar(256) NOT NULL,
          `password` varchar(256) NOT NULL,
          PRIMARY KEY (`id`),
          INDEX(email),
          INDEX(username)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };

    private final String[] createUserAuthStatment = {
        """
        CREATE TABLE IF NOT EXISTS UserAuth (
          `id` int NOT NULL AUTO_INCREMENT,
          `authToken` varchar(256) NOT NULL,
          `username` varchar(256) NOT NULL,
          PRIMARY KEY (`id`),
          INDEX(authtoken),
          INDEX(username)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
        """
    };


}
