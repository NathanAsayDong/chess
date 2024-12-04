package websocket;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import chess.ChessGame;
import chess.ChessMove;
import model.AuthData;
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

import static websocket.messages.ServerMessage.ServerMessageType.*;

@WebSocket
public class WebSocketHandler {
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final Map<Session, Integer> gameSessions = new ConcurrentHashMap<>();
    private ChessService chessService;
    private UserService userService;
    private final Gson gson = new Gson();

    public WebSocketHandler(ChessService chessService, UserService userService) {
        this.chessService = new ChessService(chessService.userDao, chessService.gameDao);
        this.userService = new UserService(userService.userDao);
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("Connected: " + session.getRemoteAddress());
        sessions.put(session.getRemoteAddress().toString(), session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("Closed: " + session.getRemoteAddress());
        sessions.remove(session.getRemoteAddress().toString());
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
                    ChessGame.TeamColor teamColor = getTeamColorFromAuth(message);
                    if (teamColor == null) {
                        observeGame(session, message);
                    } else {
                        joinGame(session, message, teamColor);
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
            verifyAuth(message);
            GameData game = chessService.getGameById(message.getGameID());
            if (game == null) {
                throw new Exception("Error: game does not exist");
            }
            gameSessions.put(session, game.gameID());
            ServerMessage response = new ServerMessage(LOAD_GAME);
            response.addGameData(game);
            sendToClient(session, response);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.addNotificationMessage("Player " + getAuth(message.getAuthToken()).username() + " is observing the game");
            broadcastToAllButMe(session, notification, game.gameID());
        } catch (Exception ex) {
            sendErrorToClient(session, "error observing game: " + ex.getMessage());
        }
    }


    private void joinGame(Session session, UserGameCommand message, ChessGame.TeamColor teamColor) {
        try {
            verifyAuth(message);
            AuthData authData = getAuth(message.getAuthToken());
            if (authData.username() == null) {
                throw new Exception("Error: user not authenticated");
            }
            ServerMessage response = new ServerMessage(LOAD_GAME);
            GameData game = chessService.getGameById(message.getGameID());
            response.addGameData(game);
            sendToClient(session, response);
            gameSessions.put(session, game.gameID());
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.addNotificationMessage("Player " + authData.username() + " joined the game");
            broadcastToAllButMe(session, notification, game.gameID());
        } catch (Exception ex) {
            sendErrorToClient(session, "error joining game: " + ex.getMessage());
        }
    }

    private void makeMove(Session session, UserGameCommand message) throws Exception {
        try {
            verifyAuth(message);
            GameData game = chessService.getGameById(message.getGameID());
            AuthData authData = userService.getAuthByToken(message.getAuthToken());
            ChessGame.TeamColor teamColor = getTeamColorFromAuth(message);

            CheckGameStatus(game, teamColor);
            CheckIfValidMove(game, message.getMove());
            CheckIfItsPlayersTurn(game, teamColor);

            GameData updateGame = chessService.makeMove(game, message.getMove());
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.addNotificationMessage("Player " + authData.username() + " made a move");
            broadcastToAllButMe(session, notification, updateGame.gameID());
            ServerMessage response = new ServerMessage(LOAD_GAME);
            response.addGameData(updateGame);
            broadcastToAll(response, updateGame.gameID());
        } catch (Exception ex) {
            ServerMessage response = new ServerMessage(ERROR);
            response.addErrorMessage(ex.getMessage());
            sendToClient(session, response);
        }
    }

    private void leaveGame(Session session, UserGameCommand message) {
        try {
            GameData game = chessService.getGameById(message.getGameID());
            AuthData user = userService.getAuthByToken(message.getAuthToken());
            ChessGame.TeamColor teamColor = getTeamColorFromAuth(message);
            chessService.removePlayerFromGame(game, teamColor);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.addNotificationMessage(user.username() + " left the game");
            broadcastToAllButMe(session, notification, game.gameID());
            sessions.remove(session.getRemoteAddress().toString());
            gameSessions.remove(session);
        } catch (Exception ex) {
            sendErrorToClient(session, "error leaving game: " + ex.getMessage());
        }
    }

    private void resignGame(Session session, UserGameCommand message) {
        try {
            verifyAuth(message);
            GameData game = chessService.getGameById(message.getGameID());
            AuthData auth = userService.getAuthByToken(message.getAuthToken());
            if (!Objects.equals(auth.username(), game.whiteUsername()) && !Objects.equals(auth.username(), game.blackUsername())) {
                throw new Exception("Error: observer cannot resign the game");
            }
            chessService.playerQuitsGame(game);
            ServerMessage notification = new ServerMessage(ServerMessage.ServerMessageType.NOTIFICATION);
            notification.addNotificationMessage("Player " + auth.username() + " resigned the game");
            broadcastToAll(notification, game.gameID());
        } catch (Exception ex) {
            sendErrorToClient(session, "error resigning game: " + ex.getMessage());
        }
    }


    private void broadcastToAllButMe(Session session, ServerMessage message, Integer gameID) {
        sessions.forEach((key, sesh) -> {
            if (sesh != session && Objects.equals(gameSessions.get(sesh), gameID)) {
                sendToClient(sesh, message);
            }
        });
    }

    private void broadcastToAll(ServerMessage message, Integer gameID) {
        sessions.forEach((key, sesh) -> {
            if (Objects.equals(gameSessions.get(sesh), gameID)) {
                sendToClient(sesh, message);
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

    private ChessGame.TeamColor getTeamColorFromAuth(UserGameCommand message) {
        try {
            AuthData auth = userService.getAuthByToken(message.getAuthToken());
            if (auth.username() == null) {
                throw new Exception("Error: user not authenticated");
            }
            GameData game = chessService.getGameById(message.getGameID());
            if (game == null) {
                throw new Exception("Error: game does not exist");
            }
            if (Objects.equals(game.whiteUsername(), auth.username())) {
                return ChessGame.TeamColor.WHITE;
            } else if (Objects.equals(game.blackUsername(), auth.username())) {
                return ChessGame.TeamColor.BLACK;
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private void verifyAuth(UserGameCommand message) throws Exception {
        if (message.getAuthToken() == null) {
            throw new Exception("Error: auth token cannot be null");
        }
        else if (userService.getAuthByToken(message.getAuthToken()).username() == null) {
            throw new Exception("Error: user not authenticated");
        }
    }

    private AuthData getAuth(String authToken) {
        try {
            return userService.getAuthByToken(authToken);
        } catch (Exception e) {
            return new AuthData(null, null);
        }
    }

    private void CheckIfItsPlayersTurn(GameData game, ChessGame.TeamColor teamColor) throws Exception {
        if (game.game().getTeamTurn() != teamColor) {
            throw new Exception("Error: not your turn");
        }
    }

    private void CheckIfValidMove(GameData game, ChessMove move) throws Exception {
        Collection<ChessMove> moves = game.game().validMoves(move.getStartPosition());
        if (!moves.contains(move)) {
            throw new Exception("Error: invalid move");
        }
    }

    private void CheckGameStatus(GameData game, ChessGame.TeamColor teamColor) throws Exception {
        if (game.game().isInCheckmate(teamColor)) {
            throw new Exception("Error: checkmate");
        }
        if (game.game().isInStalemate(teamColor)) {
            ChessService.updateTeamTurn(game);
            throw new Exception("Error: stalemate");
        }
        if (game.game().isInCheck(teamColor)) {
            throw new Exception("Error: check");
        }
    }

}
