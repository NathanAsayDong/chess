package dataaccess;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserDao {

    public String createUser(UserData userData) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        return authToken;
    }

    public AuthData getAuthData(String authToken) throws DataAccessException {
        return new AuthData(authToken, "username");
    }
}
