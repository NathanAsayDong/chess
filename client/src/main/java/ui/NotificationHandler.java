package ui;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import javax.management.Notification;

public class NotificationHandler {
    private final Gson gson = new Gson();
    private final ChessClient client;

    public NotificationHandler(ChessClient client) {
        this.client = client;
    }

    void notify(Notification notification) {
        ServerMessage message = gson.fromJson(notification.getMessage(), ServerMessage.class);
        ServerMessage.ServerMessageType type = message.getServerMessageType();

        switch (type) {
            case NOTIFICATION:
                client.notification(message.getNotificationMessage());
                break;
            case ERROR:
                client.errorMessage(message.getErrorMessage());
                break;
            default:
                client.loadGame(message.getGameData());
                break;
        }
    }

}