package ui;

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


    public void connect(AuthData authData, Integer gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authData.authToken(), gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void makeMove(AuthData authData, Integer gameId, ChessMove move) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.MAKE_MOVE, authData.authToken(), gameId);
            command.addMove(move);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void leave(AuthData authData, Integer gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authData.authToken(), gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void resign(AuthData authData, Integer gameId) {
        try {
            UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authData.authToken(), gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
