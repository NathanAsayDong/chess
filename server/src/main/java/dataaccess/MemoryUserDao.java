package dataaccess;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryUserDao implements UserDao {
    Map<String, UserData> userAuth = new HashMap<>();
    Map<String, UserData> userData = new HashMap<>();

    public AuthData createUser(UserData user) throws DataAccessException {
        String authToken =  UUID.randomUUID().toString();
        UserData hashedPasswordUser = new UserData(user.username(), user.email(), BCrypt.hashpw(user.password(), BCrypt.gensalt()));
        try {
            userData.put(user.username(), hashedPasswordUser);
            userAuth.put(authToken, hashedPasswordUser);
            return new AuthData(authToken, user.username());
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public AuthData login(UserData user) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        try {
            userAuth.put(authToken, user);
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
        return new AuthData(authToken, user.username());
    }

    public void logout(AuthData authData) throws DataAccessException {
        try {
            userAuth.remove(authData.authToken());
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public void clear() throws DataAccessException {
        try {
            userAuth.clear();
            userData.clear();
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public AuthData getAuthByToken(String token) throws DataAccessException {
        try {
            if (!userAuth.containsKey(token)) {
                return new AuthData(null, null);
            }
            return new AuthData(token, userAuth.get(token).username());
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public UserData getUserDataByUserData(UserData user) throws DataAccessException {
        try {
            return userData.get(user.username());
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    @Override
    public UserData getUserDataByToken(String token) throws DataAccessException {
        return null;
    }
}


