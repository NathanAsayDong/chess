package service;

import dataaccess.UserDao;
import model.UserData;

public class ChessService {
    UserDao userDao = new UserDao();

    public void createUser(UserData userData) throws IllegalArgumentException {
        if (userData == null) {
            throw new IllegalArgumentException("User data cannot be null");
        }
        if (userData.username() == null || userData.username().isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (userData.password() == null || userData.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
        try {
            userDao.createUser(userData);
        } catch (Exception e) {
            throw new IllegalArgumentException("Username already exists");
        }
    }



}
