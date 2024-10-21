package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;
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
            throw new Exception(e.getMessage());
        }
    }

    public List<GameData> getAllGames() throws Exception {
        try {
            return gameDao.getAllGames();
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public Integer createGame(String gameName) throws Exception {
        try {
            return gameDao.createGame(gameName);
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    public void joinGame(Integer gameID, ChessGame.TeamColor teamColor, String username) throws Exception {
        try {
            GameData game = gameDao.getGameById(gameID);
            if (game == null) {
                throw new Exception("Error: game does not exist");
            }
            if ((teamColor == ChessGame.TeamColor.WHITE && game.whiteUsername() != null) || (teamColor == ChessGame.TeamColor.BLACK && game.blackUsername() != null)) {
                throw new Exception("Error: already taken");
            }
            if (teamColor == ChessGame.TeamColor.WHITE) {
                GameData updatedGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
                gameDao.updateGame(updatedGame);
            } else {
                GameData updatedGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
                gameDao.updateGame(updatedGame);
            }
        } catch (DataAccessException e) {
            throw new DataAccessException("Error accessing database");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

}
