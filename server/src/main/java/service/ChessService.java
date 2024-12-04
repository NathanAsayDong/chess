package service;

import chess.ChessGame;
import chess.ChessMove;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.List;
import java.util.UUID;

public class ChessService {
    public static UserDao userDao;
    public static GameDao gameDao ;

    public ChessService(UserDao userDao, GameDao gameDao) {
        ChessService.userDao = userDao;
        ChessService.gameDao = gameDao;
    }

    public void clear() throws Exception {
        userDao.clear();
        gameDao.clear();
    }

    public List<GameData> getAllGames() throws Exception {
        return gameDao.getAllGames();
    }

    public Integer createGame(String gameName) throws Exception {
        if (gameName == null || gameName.isEmpty()) {
            throw new Exception("Error: game name cannot be empty");
        }
        return gameDao.createGame(gameName);
    }

    public void joinGame(Integer gameID, ChessGame.TeamColor teamColor, String username) throws Exception {
        GameData game = gameDao.getGameById(gameID);
        if (game == null) {
            throw new Exception("Error: game does not exist");
        }
        if (teamColor == null) {
            throw new Exception("Error: team color cannot be null");
        }
        if (username == null || username.isEmpty()) {
            throw new Exception("Error: username cannot be empty");
        }
        if ((teamColor == ChessGame.TeamColor.WHITE && game.whiteUsername() != null)
                || (teamColor == ChessGame.TeamColor.BLACK && game.blackUsername() != null)) {
            throw new DuplicateInfoException("Error: already taken");
        }
        if (teamColor == ChessGame.TeamColor.WHITE) {
            GameData updatedGame = new GameData(gameID, username, game.blackUsername(), game.gameName(), game.game());
            gameDao.updateGame(updatedGame);
        } else {
            GameData updatedGame = new GameData(gameID, game.whiteUsername(), username, game.gameName(), game.game());
            gameDao.updateGame(updatedGame);
        }
    }

    public GameData getGameById(Integer gameID) throws Exception {
        return gameDao.getGameById(gameID);
    }

    public GameData makeMove(GameData game, ChessMove move) throws Exception {
        game.game().makeMove(move);
        gameDao.updateGame(game);
        return game;
    }

    public void removePlayerFromGame(GameData game, ChessGame.TeamColor teamColor) throws Exception {
        try {
            GameData updatedGame;
            if (teamColor == ChessGame.TeamColor.WHITE) {
                updatedGame = new GameData(game.gameID(), null, game.blackUsername(), game.gameName(), game.game());
            } else {
                updatedGame = new GameData(game.gameID(), game.whiteUsername(), null, game.gameName(), game.game());
            }
            gameDao.updateGame(updatedGame);
        } catch (Exception e) {
            throw new Exception("Error: could not remove player from game");
        }
    }

    public void playerQuitsGame(GameData game) throws Exception {
        try {
            gameDao.deleteGameById(game.gameID());
        } catch (Exception e) {
            throw new Exception("Error: could not remove player from game");
        }
    }

    public static void updateTeamTurn(GameData game) throws Exception {
        try {
            if (game.game().getTeamTurn() == ChessGame.TeamColor.WHITE) {
                game.game().setTeamTurn(ChessGame.TeamColor.BLACK);
            } else {
                game.game().setTeamTurn(ChessGame.TeamColor.WHITE);
            }
            GameData updatedGame = new GameData(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
            gameDao.updateGame(updatedGame);
        } catch (Exception e) {
            throw new Exception("Error: could not update team turn");
        }
    }

}
