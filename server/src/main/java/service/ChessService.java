package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.UUID;

public class ChessService {
    UserDao userDao = new UserDao();
    GameDao gameDao = new GameDao();

    public void clear() throws Exception {
        try {
            userDao.clear();
            gameDao.clear();
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception("Error clearing database");
        }
    }


    public Integer createGame(GameData data) throws Exception {
        try {
            return gameDao.createGame(data);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception("Error creating game");
        }
    }

}
