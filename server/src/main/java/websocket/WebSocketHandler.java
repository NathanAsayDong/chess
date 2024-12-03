package websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import com.google.gson.Gson;

import model.GameData;
import model.UserData;
import service.ChessService;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import static websocket.messages.ServerMessage.ServerMessageType.ERROR;
import static websocket.messages.ServerMessage.ServerMessageType.LOAD_GAME;

@WebSocket
public class WebSocketHandler {
    private final Map<Session, String> sessions = new ConcurrentHashMap<>();
    private final ChessService chessService;
    private final UserService userService;
    private final Gson gson = new Gson();

    public WebSocketHandler(ChessService chessService, UserService userService) {
        this.chessService = chessService;
        this.userService = userService;
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected: " + session.getRemoteAddress());
        sessions.put(session, session.getRemoteAddress().toString());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Closed: " + session.getRemoteAddress());
        sessions.remove(session);
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        try {
            UserGameCommand command = gson.fromJson(message, UserGameCommand.class);
            handleSocketMessage(session, command);
        } catch (Exception e) {
            sendErrorToClient(session, "Error processing message: " + e.getMessage());
        }
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        System.err.println("Error: " + error.getMessage());
        sendErrorToClient(session, error.getMessage());
    }


    private void handleSocketMessage(Session session, UserGameCommand message) {
        try {
            switch (message.getCommandType()) {
                case CONNECT:
                    if (message.getTeamColor() == null) {
                        observeGame(session, message);
                    } else {
                        joinGame(session, message);
                    }
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
                    sendErrorToClient(session, "Unknown command type");
            }
        } catch (Exception ex) {
            sendErrorToClient(session, ex.getMessage());
        }
    }

    private void observeGame(Session session, UserGameCommand message) throws Exception {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            if (game == null) {
                throw new Exception("Error: game does not exist");
            }
            ServerMessage response = new ServerMessage(LOAD_GAME);
            response.addGame(game.game());
            sendToClient(session, response);
        } catch (Exception ex) {
            sendErrorToClient(session, "error observing game: " + ex.getMessage());
        }
    }


    private void joinGame(Session session, UserGameCommand message) throws Exception {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            if (game == null || user == null) {
                throw new Exception("Error: game or user does not exist");
            }
            chessService.joinGame(message.getGameID(), message.getTeamColor(), user.username());
            ServerMessage response = new ServerMessage(LOAD_GAME);
            game = chessService.getGameById(message.getGameID());
            response.addGame(game.game());
            sendToClient(session, response);
        } catch (Exception ex) {
            sendErrorToClient(session, "error joining game: " + ex.getMessage());
        }
    }

    private void makeMove(Session session, UserGameCommand message) throws Exception {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            if (game == null || user == null) {
                throw new Exception("Error: game or user does not exist");
            }
            game = chessService.makeMove(game, user, message.getMove());
            ServerMessage response = new ServerMessage(LOAD_GAME);
            response.addGame(game.game());
            broadcastToAll(response);
        } catch (Exception ex) {
            ServerMessage response = new ServerMessage(ERROR);
            response.addErrorMessage(ex.getMessage());
            sendToClient(session, response);
        }
    }

    private void leaveGame(Session session, UserGameCommand message) {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            chessService.removePlayerFromGame(game, user);
        } catch (Exception ex) {
            sendErrorToClient(session, "error leaving game: " + ex.getMessage());
        }
    }

    private void resignGame(Session session, UserGameCommand message) {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            UserData user = userService.getUserDataByToken(message.getAuthToken());
            chessService.playerQuitsGame(game, user);
        } catch (Exception ex) {
            sendErrorToClient(session, "error resigning game: " + ex.getMessage());
        }
    }


    private void broadcastToAll(ServerMessage message) {
        String jsonMessage = gson.toJson(message);
        sessions.keySet().forEach(session -> {
            try {
                session.getRemote().sendString(jsonMessage);
            } catch (Exception e) {
                System.err.println("Error broadcasting to " + session.getRemoteAddress() + ": " + e.getMessage());
            }
        });
    }

    private void sendToClient(Session session, ServerMessage message) {
        try {
            session.getRemote().sendString(gson.toJson(message));
        } catch (Exception e) {
            System.err.println("Error sending to " + session.getRemoteAddress() + ": " + e.getMessage());
        }
    }

    private void sendErrorToClient(Session session, String errorMessage) {
        ServerMessage message = new ServerMessage(ERROR);
        message.addErrorMessage(errorMessage);
        sendToClient(session, message);
    }
}
