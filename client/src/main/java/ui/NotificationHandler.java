package ui;

import com.google.gson.Gson;
import websocket.messages.ServerMessage;

import javax.management.Notification;

public class NotificationHandler {
    private final Gson gson = new Gson();

    void notify(Notification notification) {
        ServerMessage message = gson.fromJson(notification.getMessage(), ServerMessage.class);
        ServerMessage.ServerMessageType type = message.getServerMessageType();

        switch (type) {
            case NOTIFICATION:
                System.out.println(message.getNotificationMessage());
                break;
            case ERROR:
                System.out.println(message.getErrorMessage());
                break;
            default:
                //
                break;
        }
    }

}