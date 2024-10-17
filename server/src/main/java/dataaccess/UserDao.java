package dataaccess;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserDao {

    public AuthData createUser(UserData userData) throws DataAccessException {
        String authToken =  UUID.randomUUID().toString();
        return new AuthData(authToken, userData.username());
    }

    public AuthData login(UserData userData) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        return new AuthData(authToken, userData.username());
    }

    public AuthData getAuthData(String authToken) throws DataAccessException {
        return new AuthData(authToken, "username");
    }

    public void logout(AuthData authData) throws DataAccessException {
        return;
    }

    public void clear() throws DataAccessException {
        //clear logic here
        return;
    }

}
