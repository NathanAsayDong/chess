package dataaccess;
import model.AuthData;
import model.UserData;

import java.util.*;

public interface UserDao {

    public AuthData createUser(UserData user) throws DataAccessException;

    public AuthData login(UserData user) throws DataAccessException;

    public void logout(AuthData authData) throws DataAccessException;

    public void clear() throws DataAccessException;

    public AuthData getAuthByToken(String token) throws DataAccessException;

    public UserData getUserDataByUserData(UserData user) throws DataAccessException;

    public UserData getUserDataByToken(String token) throws DataAccessException;
}
