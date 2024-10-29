package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.UserData;

import java.sql.SQLException;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SqlUserDao implements UserDao {
    private static final String userDataTable = "UserData";
    private static final String userAuthTable = "UserAuth";

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
