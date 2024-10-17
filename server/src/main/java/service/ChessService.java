package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

public class ChessService {
    UserDao userDao = new UserDao();
    GameDao gameDao = new GameDao();
    AuthDao authDao = new AuthDao();

    public void clear() throws Exception {
        try {
            userDao.clear();
            gameDao.clear();
            authDao.clear();
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception("Error clearing database");
        }
    }



}
