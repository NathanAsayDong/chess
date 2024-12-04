package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.AuthData;
import websocket.commands.UserGameCommand;

import javax.management.Notification;
import java.net.URI;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import javax.websocket.*;


public class ClientWebsocketHandler extends Endpoint {
    Session session;
    NotificationHandler notificationHandler;

    public ClientWebsocketHandler(String serverUrl, ChessClient client) {
        try {
            serverUrl = serverUrl.replace("http", "ws");
            URI socketURI = new URI(serverUrl + "/ws");
            this.notificationHandler = new NotificationHandler(client);

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    Notification notification = new Gson().fromJson(message, Notification.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onOpen(javax.websocket.Session session, EndpointConfig endpointConfig) {
        System.out.println("Connected to server");
    }


    public void connect(String authToken, Integer gameId, ChessGame.TeamColor teamColor) throws Exception {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Error connecting to websocket" + EscapeSequences.RESET_TEXT_COLOR);
            throw new Exception("Unable to connect to websocket");
        }
    }

    public void observe(String authToken, Integer gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void makeMove(String authToken, Integer gameId, ChessMove move) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId);
            command.addMove(move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void leave(String authToken, Integer gameId, ChessGame.TeamColor teamColor) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resign(String authToken, Integer gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
