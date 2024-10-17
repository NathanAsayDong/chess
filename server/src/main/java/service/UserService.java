package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

public class UserService {
    UserDao userDao = new UserDao();

    public AuthData register(UserData user) throws Exception {
        try {
            return userDao.createUser(user);
        } catch (DuplicateInfoException e) {
            throw new DuplicateInfoException("Username or email already exists");
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception("Error creating user");
        }
    }

    public AuthData login(UserData user) throws Exception {
        try {
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
            userDao.getAuthData(auth.authToken());
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new UnauthorizedException("Unauthorized");
        }
    }
}
