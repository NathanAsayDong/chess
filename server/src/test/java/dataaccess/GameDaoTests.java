package dataaccess;

import model.GameData;
import chess.ChessGame;
import com.google.gson.Gson;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class GameDaoTests {

    private GameDao gameDao;

    @BeforeEach
    public void setUp() throws Exception {
        // Initialize your GameDao implementation
        gameDao = new SqlGameDao(); // Replace with your actual implementation
        gameDao.clear();
    }

    @AfterEach
    public void tearDown() throws Exception {
        gameDao.clear();
    }

    /**
     * Positive test case for the createGame method.
     * It should successfully create a new game and return the game ID.
     */
    @Test
    public void testCreateGamePositive() {
        try {
            String gameName = "Test Game";
            Integer gameId = gameDao.createGame(gameName);
            assertNotNull(gameId, "Game ID should not be null after creation");
            assertTrue(gameId > 0, "Game ID should be greater than zero");

            GameData gameData = gameDao.getGameById(gameId);
            assertNotNull(gameData, "GameData should not be null for the created game");
            assertEquals(gameName, gameData.gameName(), "Game names should match");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive createGame test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the createGame method.
     * It should fail to create a game with a null game name.
     */
    @Test
    public void testCreateGameNegative() {
        try {
            gameDao.createGame(null);
            fail("DataAccessException should have been thrown due to null game name");
        } catch (DataAccessException e) {
            assertEquals("Error: game name cannot be null", e.getMessage(), "Exception message should indicate null game name");
        }
    }

    /**
     * Positive test case for the getAllGames method.
     * It should retrieve all games in the database.
     */
    @Test
    public void testGetAllGamesPositive() {
        try {
            gameDao.createGame("Game 1");
            gameDao.createGame("Game 2");

            List<GameData> games = gameDao.getAllGames();
            assertNotNull(games, "Games list should not be null");
            assertEquals(2, games.size(), "There should be two games in the list");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive getAllGames test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the getAllGames method.
     * It should return an empty list if there are no games.
     */
    @Test
    public void testGetAllGamesNegative() {
        try {
            List<GameData> games = gameDao.getAllGames();
            assertNotNull(games, "Games list should not be null");
            assertTrue(games.isEmpty(), "Games list should be empty when no games are present");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in negative getAllGames test: " + e.getMessage());
        }
    }

    /**
     * Positive test case for the updateGame method.
     * It should successfully update an existing game.
     */
    @Test
    public void testUpdateGamePositive() {
        try {
            String originalGameName = "Original Game";
            Integer gameId = gameDao.createGame(originalGameName);

            GameData gameData = gameDao.getGameById(gameId);
            gameData = new GameData(gameId, "whitePlayer", "blackPlayer", "Updated Game", new ChessGame());

            gameDao.updateGame(gameData);

            GameData updatedGameData = gameDao.getGameById(gameId);
            assertEquals("Updated Game", updatedGameData.gameName(), "Game name should be updated");
            assertEquals("whitePlayer", updatedGameData.whiteUsername(), "White username should be updated");
            assertEquals("blackPlayer", updatedGameData.blackUsername(), "Black username should be updated");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive updateGame test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the updateGame method.
     * It should fail to update a non-existent game.
     */
    @Test
    public void testUpdateGameNegative() {
        try {
            GameData nonExistentGame = new GameData(9999, "whitePlayer", "blackPlayer", "Non-existent Game", new ChessGame());
            gameDao.updateGame(nonExistentGame);
            GameData game = gameDao.getGameById(9999);
            if (game != null) {
                fail("Game should not exist after updateGame failure");
            }
        } catch (DataAccessException e) {
            assertEquals("Game not found", e.getMessage(), "Exception message should indicate game not found");
        }
    }

    /**
     * Positive test case for the getGameById method.
     * It should retrieve a game by its ID.
     */
    @Test
    public void testGetGameByIdPositive() {
        try {
            String gameName = "Test Game";
            Integer gameId = gameDao.createGame(gameName);

            GameData gameData = gameDao.getGameById(gameId);
            assertNotNull(gameData, "GameData should not be null");
            assertEquals(gameId, gameData.gameID(), "Game IDs should match");
            assertEquals(gameName, gameData.gameName(), "Game names should match");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive getGameById test: " + e.getMessage());
        }
    }

    /**
     * Negative test case for the getGameById method.
     * It should return null or throw an exception for a non-existent game ID.
     */
    @Test
    public void testGetGameByIdNegative() {
        try {
            GameData gameData = gameDao.getGameById(9999);
            assertNull(gameData, "GameData should be null for non-existent game ID");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in negative getGameById test: " + e.getMessage());
        }
    }

    /**
     * Positive test case for the clear method.
     * It should successfully clear all game data.
     */
    @Test
    public void testClearPositive() {
        try {
            gameDao.createGame("Game 1");
            gameDao.createGame("Game 2");
            gameDao.clear();

            List<GameData> games = gameDao.getAllGames();
            assertNotNull(games, "Games list should not be null after clear");
            assertTrue(games.isEmpty(), "Games list should be empty after clear");
        } catch (DataAccessException e) {
            fail("Exception should not be thrown in positive clear test: " + e.getMessage());
        }
    }
}
