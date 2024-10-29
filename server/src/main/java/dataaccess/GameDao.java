package dataaccess;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;


public interface GameDao {

    public List<GameData> getAllGames() throws DataAccessException;

    public Integer createGame(String gameName) throws DataAccessException;

    public void updateGame(GameData game) throws DataAccessException;

    public GameData getGameById(Integer gameId);

    public void clear();
}
