package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlUserDao implements UserDao {
    private static final String USERDATATABLE = "UserData";
    private static final String USERAUTHTABLE = "UserAuth";

    public SqlUserDao() throws DataAccessException {
        try {
            configureDatabase();
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData createUser(UserData user) throws DataAccessException {
        try {
            String authToken = UUID.randomUUID().toString();
            String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
            String userDataStatement = String.format("INSERT INTO %s (username, email, password) VALUES (?, ?, ?)", USERDATATABLE);
            executeUpdate(userDataStatement, user.username(), user.email(), hashedPassword);
            String userAuthStatement = String.format("INSERT INTO %s (authToken, username) VALUES (?, ?)", USERAUTHTABLE);
            executeUpdate(userAuthStatement, authToken, user.username());
            return new AuthData(authToken, user.username());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData login(UserData user) throws DataAccessException {
        try {
            String authToken = UUID.randomUUID().toString();
            String statement = String.format("INSERT INTO %s (authToken, username) VALUES (?, ?)", USERAUTHTABLE);
            executeUpdate(statement, authToken, user.username());
            return new AuthData(authToken, user.username());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void logout(AuthData authData) throws DataAccessException {
        try {
            String statement = String.format("DELETE FROM %s WHERE authToken = ?", USERAUTHTABLE);
            executeUpdate(statement, authData.authToken());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData getAuthByToken(String token) throws DataAccessException {
        String statement = String.format("SELECT * FROM %s WHERE authToken = ?", USERAUTHTABLE);
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, token);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new AuthData(rs.getString("authToken"), rs.getString("username"));
                } else {
                    return new AuthData(null, null);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }


    @Override
    public UserData getUserDataByUserData(UserData user) throws DataAccessException {
        String statement = String.format("SELECT * FROM %s WHERE username = ?", USERDATATABLE);
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(statement)) {
            ps.setString(1, user.username());
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserData(
                            rs.getString("username"),
                            rs.getString("password"),
                            rs.getString("email")
                    );
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
        return null;
    }


    @Override
    public void clear() throws DataAccessException {
        try {
            executeUpdate("DELETE FROM UserData");
            executeUpdate("DELETE FROM UserAuth");
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
          `username` varchar(256) NOT NULL UNIQUE,
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
