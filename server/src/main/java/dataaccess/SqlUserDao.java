package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlUserDao implements UserDao {
    private static final String userDataTable = "UserData";
    private static final String userAuthTable = "UserAuth";

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
            String user_data_statement = String.format("INSERT INTO %s (username, email, password) VALUES (?, ?, ?)", userDataTable);
            executeUpdate(user_data_statement, user.username(), user.email(), user.password());
            String user_auth_statement = String.format("INSERT INTO %s (authToken, username) VALUES (?, ?)", userAuthTable);
            executeUpdate(user_auth_statement, authToken, user.username());
            return new AuthData(authToken, user.username());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData login(UserData user) throws DataAccessException {
        try {
            String authToken = UUID.randomUUID().toString();
            String statement = String.format("INSERT INTO %s (authToken, username) VALUES (?, ?)", userAuthTable);
            executeUpdate(statement, authToken, user.username());
            return new AuthData(authToken, user.username());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public void logout(AuthData authData) throws DataAccessException {
        try {
            String statement = String.format("DELETE FROM %s WHERE authToken = ?", userAuthTable);
            executeUpdate(statement, authData.authToken());
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public AuthData getAuthByToken(String token) throws DataAccessException {
        try {
            String statement = String.format("SELECT * FROM %s WHERE authToken = ?", userAuthTable);
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, token);
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new AuthData(rs.getString("authToken"), rs.getString("username"));
                        }
                        return new AuthData(null, null);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
    }

    @Override
    public UserData getUserDataByUserData(UserData user) throws DataAccessException {
        try {
            String statement = String.format("SELECT * FROM %s WHERE username = ?", userDataTable);
            try (var conn = DatabaseManager.getConnection()) {
                try (var ps = conn.prepareStatement(statement)) {
                    ps.setString(1, user.username());
                    try (var rs = ps.executeQuery()) {
                        if (rs.next()) {
                            return new UserData(rs.getString("username"), rs.getString("email"), rs.getString("password"));
                        }
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e.getMessage());
        }
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
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
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
