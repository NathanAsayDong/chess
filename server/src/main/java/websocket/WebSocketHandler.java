package websocket;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;

import service.ChessService;
import service.UserService;
import spark.Session;
import websocket.commands.UserGameCommand;

import java.util.HashMap;
import java.util.Map;

public class WebSocketHandler {

    private Map<String, Session> sessions = new HashMap<>();
    private ChessService chessService;
    private UserService userService;

    public WebSocketHandler(ChessService chessService, UserService userService) {
        this.chessService = chessService;
        this.userService = userService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("WebSocket Client connected: " + session.id());
        sessions.put(session.id(), session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("WebSocket Client disconnected: " + session.id());
        sessions.remove(session.id());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, UserGameCommand message) {
        System.out.println("Received message from client: " + message);
        handleSocketMessage(session, message);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("WebSocket Error: " + error.getMessage());
    }

    private void handleSocketMessage(Session session, UserGameCommand message) {
        try {
            switch (message.getCommandType()) {
                case CONNECT:
                    // handle connect
                    break;
                case MAKE_MOVE:
                    makeMove(session, message);
                    break;
                case LEAVE:
                    leaveGame(session, message);
                    break;
                case RESIGN:
                    resignGame(session, message);
                    break;
                default:
                    System.err.println("Unknown command type: " + message.getCommandType());
            }
        } catch (Exception ex) {
            System.err.println("Error handling message: " + ex.getMessage());
        }
    }

    private void makeMove(Session session, UserGameCommand message) throws Exception {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            if (game == null || user == null) {
                throw new Exception("Error: game or user does not exist");
            }
            chessService.makeMove(game, user, message.getMove());
        } catch (Exception ex) {
            System.err.println("Error making move: " + ex.getMessage());
        }
    }

    private void leaveGame(Session session, UserGameCommand message) {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            chessService.removePlayerFromGame(game, user);
        } catch (Exception ex) {
            System.err.println("Error leaving game: " + ex.getMessage());
        }
    }

    private void resignGame(Session session, UserGameCommand message) {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            chessService.playerQuitsGame(game, user);
        } catch (Exception ex) {
            System.err.println("Error resigning game: " + ex.getMessage());
        }
    }
}