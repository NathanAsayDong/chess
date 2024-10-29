package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.GameDao;
import dataaccess.MemoryUserDao;
import dataaccess.UserDao;
import model.GameData;
import org.junit.jupiter.api.*;
import service.ChessService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChessServiceTest {

    private ChessService chessService;

    @BeforeEach
    public void setUp() throws Exception {
        GameDao gameDao = new GameDao();
        UserDao userDao = new MemoryUserDao();

        chessService = new ChessService(userDao, gameDao);
        chessService.clear();
    }

    @AfterEach
    public void tearDown() throws Exception {
        chessService.clear();
    }

    /**
     * Positive test case for the clear method.
     * It should successfully clear the database.
     */
    @Test
    public void testClearPositive() {
        try {
            chessService.createGame("Test Game 1");
            chessService.createGame("Test Game 2");
            List<GameData> gamesBeforeClear = chessService.getAllGames();
            assertEquals(2, gamesBeforeClear.size(), "There should be 2 games before clearing");
            chessService.clear();
            List<GameData> gamesAfterClear = chessService.getAllGames();
            assertEquals(0, gamesAfterClear.size(), "There should be no games after clearing");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive clear test: " + e.getMessage());
        }
    }

    /**
     * Positive test case for the getAllGames method.
     * It should successfully return a list of all games.
     */
    @Test
    public void testGetAllGamesPositive() {
        try {
            chessService.createGame("Game 1");
            chessService.createGame("Game 2");
            List<GameData> games = chessService.getAllGames();
            assertNotNull(games, "Games list should not be null");
            assertEquals(2, games.size(), "There should be 2 games in the list");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive getAllGames test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the getAllGames method.
     * It should handle the case where there are no games.
     */
    @Test
    public void testGetAllGamesNegative() {
        try {
            chessService.clear();
            List<GameData> games = chessService.getAllGames();
            assertNotNull(games, "Games list should not be null even when empty");
            assertEquals(0, games.size(), "Games list should be empty");
        } catch (Exception e) {
            fail("Exception should not be thrown in negative getAllGames test: " + e.getMessage());
        }
    }

    /**
     * Positive test case for the createGame method.
     * It should successfully create a new game and return its ID.
     */
    @Test
    public void testCreateGamePositive() {
        try {
            Integer gameId = chessService.createGame("New Game");
            assertNotNull(gameId, "Game ID should not be null after creation");
            GameData gameData = chessService.gameDao.getGameById(gameId);
            assertNotNull(gameData, "GameData should not be null for the created game");
            assertEquals("New Game", gameData.gameName(), "Game name should match the one provided");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive createGame test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the createGame method.
     * It should handle the case where the game name is null or empty.
     */
    @Test
    public void testCreateGameNegative() {
        try {
            chessService.createGame(null);
            fail("Exception should have been thrown due to null game name");
        } catch (Exception e) {
            assertEquals("Error: game name cannot be empty", e.getMessage(), "Exception message should indicate null game name");
        }

        try {
            chessService.createGame("");
            fail("Exception should have been thrown due to empty game name");
        } catch (Exception e) {
            assertEquals("Error: game name cannot be empty", e.getMessage(), "Exception message should indicate bad game name");
        }
    }

    /**
     * Positive test case for the joinGame method.
     * It should successfully join a player to a game.
     */
    @Test
    public void testJoinGamePositive() {
        try {
            Integer gameId = chessService.createGame("Joinable Game");
            chessService.joinGame(gameId, ChessGame.TeamColor.WHITE, "PlayerWhite");
            GameData gameData = chessService.gameDao.getGameById(gameId);
            assertEquals("PlayerWhite", gameData.whiteUsername(), "White player username should be 'PlayerWhite'");
            assertNull(gameData.blackUsername(), "Black player should be null");
            chessService.joinGame(gameId, ChessGame.TeamColor.BLACK, "PlayerBlack");
            gameData = chessService.gameDao.getGameById(gameId);
            assertEquals("PlayerBlack", gameData.blackUsername(), "Black player username should be 'PlayerBlack'");
        } catch (Exception e) {
            fail("Exception should not be thrown in positive joinGame test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the joinGame method.
     * It should handle the case where the game ID does not exist.
     */
    @Test
    public void testJoinGameNegativeGameNotFound() {
        try {
            // Attempt to join a non-existent game
            chessService.joinGame(999, ChessGame.TeamColor.WHITE, "Player");
            fail("Exception should have been thrown due to non-existent game ID");
        } catch (Exception e) {
            assertEquals("Error: game does not exist", e.getMessage(), "Exception message should indicate game not found");
        }
    }

    /**
     * Negative test case for the joinGame method.
     * It should handle the case where the team color is already taken.
     */
    @Test
    public void testJoinGameNegativeTeamTaken() {
        try {
            Integer gameId = chessService.createGame("Team Taken Game");
            chessService.joinGame(gameId, ChessGame.TeamColor.WHITE, "Player1");
            chessService.joinGame(gameId, ChessGame.TeamColor.WHITE, "Player2");
            fail("Exception should have been thrown due to team color already taken");
        } catch (Exception e) {
            assertEquals("Error: already taken", e.getMessage(), "Exception message should indicate team already taken");
        }
    }

    /**
     * Negative test case for the joinGame method.
     * It should handle the case where an invalid team color is provided.
     */
    @Test
    public void testJoinGameNegativeInvalidTeamColor() {
        try {
            Integer gameId = chessService.createGame("Invalid Team Color Game");
            chessService.joinGame(gameId, null, "Player");
            fail("Exception should have been thrown due to null team color");
        } catch (Exception e) {
            assertEquals("Error: team color cannot be null", e.getMessage(), "Exception message should indicate invalid parameter");
        }
    }

    /**
     * Negative test case for the joinGame method.
     * It should handle the case where the username is null or empty.
     */
    @Test
    public void testJoinGameNegativeInvalidUsername() {
        try {
            Integer gameId = chessService.createGame("Invalid Username Game");
            chessService.joinGame(gameId, ChessGame.TeamColor.WHITE, null);
            fail("Exception should have been thrown due to null username");
        } catch (Exception e) {
            assertEquals("Error: username cannot be empty", e.getMessage(), "Exception message should indicate invalid parameter");
        }
    }
}
