package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

public class UserService {
    public UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public AuthData register(UserData user) throws Exception {
        if (userNameTaken(user)) {
            throw new DuplicateInfoException("Username already exists");
        }
        return userDao.createUser(user);
    }

    public AuthData login(UserData user) throws Exception {
        UserData dbUserData = userDao.getUserDataByUserData(user);
        if (dbUserData == null || dbUserData.username() == null || dbUserData.username().isEmpty()) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (!BCrypt.checkpw(user.password(), dbUserData.password())) {
            throw new UnauthorizedException("Unauthorized");
        }
        if (!Objects.equals(dbUserData.username(), user.username())) {
            throw new UnauthorizedException("Unauthorized");
        }
        return userDao.login(user);
    }

    public void logout(AuthData auth) throws Exception {
        verifyAuth(auth);
        auth = userDao.getAuthByToken(auth.authToken());
        userDao.logout(auth);
    }

    public void verifyAuth(AuthData auth) throws Exception {
        AuthData data = userDao.getAuthByToken(auth.authToken());
        if (data.username() == null || data.username().isEmpty()) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    public AuthData getAuthByToken(String authToken) throws Exception {
        return userDao.getAuthByToken(authToken);
    }

    public UserData getUserDataByToken(String authToken) throws Exception {
        return userDao.getUserDataByToken(authToken);
    }

    //private helpers
    private boolean userNameTaken(UserData user) {
        try {
            return userDao.getUserDataByUserData(user).username() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
