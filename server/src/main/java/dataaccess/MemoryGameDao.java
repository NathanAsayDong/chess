package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public class MemoryGameDao implements GameDao {
    Map<Integer, GameData> games = new HashMap<>();

    public List<GameData> getAllGames() throws DataAccessException {
        try {
            return new ArrayList<>(games.values());
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public Integer createGame(String gameName) throws DataAccessException {
        try {
            Integer gameID = games.size() + 1;
            games.put(gameID, new GameData(gameID, null, null, gameName, new ChessGame()));
            return gameID;
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public void updateGame(GameData game) throws DataAccessException {
        try {
            games.remove(game.gameID());
            games.put(game.gameID(), game);
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public GameData getGameById(Integer gameId) {
        try {
            return games.get(gameId);
        } catch (Exception e) {
            return null;
        }
    }

    public void deleteGameById(Integer gameId) throws DataAccessException {
        try {
            games.remove(gameId);
        } catch (Exception e) {
            throw new DataAccessException("Error accessing database");
        }
    }

    public void clear() {
        games.clear();
    }
}
