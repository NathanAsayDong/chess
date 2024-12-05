package websocket.messages;

import model.GameData;

import java.util.Objects;

/**
 * Represents a Message the server can send through a WebSocket
 * 
 * Note: You can add to this class, but you should not alter the existing
 * methods.
 */
public class ServerMessage {
    ServerMessageType serverMessageType;
    GameData game;
    String errorMessage;
    String message;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public void addGameData(GameData gameData) {
        this.game = gameData;
    }

    public void addErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addNotificationMessage(String notificationMessage) {
        this.message = notificationMessage;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public GameData getGameData() {
        return this.game;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getNotificationMessage() {
        return this.message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ServerMessage)) {
            return false;
        }
        ServerMessage that = (ServerMessage) o;
        return getServerMessageType() == that.getServerMessageType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getServerMessageType());
    }
}
