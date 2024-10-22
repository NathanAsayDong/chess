package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.Objects;

public class UserService {
    UserDao userDao = new UserDao();

    public AuthData register(UserData user) throws Exception {
        try {
            if (userNameTaken(user)) {
                throw new DuplicateInfoException("Username already exists");
            }
            return userDao.createUser(user);
        } catch (DuplicateInfoException e) {
            throw new DuplicateInfoException(e.getMessage());
        } catch (DataAccessException e) {
            throw new DataAccessException(e.getMessage());
        } catch (Exception e) {
            throw new Exception("Error creating user");
        }
    }

    public AuthData login(UserData user) throws Exception {
        try {
            UserData dbUserData = userDao.getUserDataByUserData(user);
            if (!Objects.equals(dbUserData.password(), user.password())) {
                throw new UnauthorizedException("Unauthorized");
            }
            return userDao.login(user);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("Unauthorized");
        } catch (Exception e) {
            throw new Exception("Error creating user");
        }
    }

    public void logout(AuthData auth) throws Exception {
        try {
            verifyAuth(auth);
            auth = userDao.getAuthByToken(auth.authToken());
            userDao.logout(auth);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (UnauthorizedException e) {
            throw new UnauthorizedException("Unauthorized");
        } catch (Exception e) {
            throw new Exception("Error logging out user");
        }
    }

    public void verifyAuth(AuthData auth) throws Exception {
        try {
            AuthData data = userDao.getAuthByToken(auth.authToken());
            if (data.username() == null || data.username().isEmpty()) {
                throw new UnauthorizedException("Unauthorized");
            }
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new UnauthorizedException("Unauthorized");
        }
    }

    public AuthData getAuthByToken(String authToken) throws Exception {
        try {
            return userDao.getAuthByToken(authToken);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception("Error getting auth by token");
        }
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
