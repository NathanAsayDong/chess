package client;

import java.util.Map;

import model.GameData;
import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import chess.ChessGame;
import model.AuthData;
import model.ListGamesResult;
import server.Server;
import ui.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;
    private static AuthData authData;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(8080);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);
    }

    @BeforeEach
    public void clearDatabase() throws Exception {
        facade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void clearPositive() throws Exception {
        AuthData authData = facade.register("player1", "password123", "p1@email.com");
        facade.clear();
        assertThrows(Exception.class, () -> {
            facade.login("player1", "password123");
        });
    }

    @Test
    public void clearNegative() throws Exception {
        facade.clear();
    }

    @Test
    public void registerPositive() throws Exception {
        AuthData authData = facade.register("player1", "password123", "p1@email.com");
        assertNotNull(authData);
        assertNotNull(authData.authToken());
        assertEquals("player1", authData.username());
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        assertThrows(Exception.class, () -> {
            facade.register("player1", "differentpassword", "different@email.com");
        });
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        AuthData authData = facade.login("player1", "password123");
        assertNotNull(authData);
    }

    @Test
    public void loginNegative() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        assertThrows(Exception.class, () -> {
            facade.login("player1", "wrongpassword");
        });
    }

    @Test
    public void logoutPositive() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        AuthData authData = facade.login("player1", "password123");
        facade.logout(authData.authToken());
    }

    @Test
    public void listGamesPositive() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        authData = facade.login("player1", "password123");
        facade.createGame("testGame", authData.authToken());
        ListGamesResult result = facade.listGames(authData.authToken());
        assertEquals(1, result.games().size());
    }

    @Test
    public void listGamesNegative() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.listGames("invalidToken");
        });
    }

    @Test
    public void createGamePositive() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        authData = facade.login("player1", "password123");
        Map<String, Object> result = facade.createGame("testGame", authData.authToken());
        assertNotNull(result);
    }

    @Test
    public void createGameNegative() throws Exception {
        assertThrows(Exception.class, () -> {
            facade.createGame("testGame", "invalidToken");
        });
    }

    @Test
    public void joinGamePositive() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        authData = facade.login("player1", "password123");
        Map<String, Object> result = facade.createGame("testGame", authData.authToken());
        GameData game = facade.joinGame(((Double) result.get("gameID")).intValue(), ChessGame.TeamColor.WHITE, authData.authToken());
        assertNotNull(game);
    }

    @Test
    public void joinGameNegative() throws Exception {
        facade.register("player1", "password123", "p1@email.com");
        authData = facade.login("player1", "password123");
        Map<String, Object> result = facade.createGame("testGame", authData.authToken());
        assertNotNull(result);
        
        assertThrows(Exception.class, () -> {
            facade.joinGame(-1, ChessGame.TeamColor.WHITE, authData.authToken());
        });
    }
}
