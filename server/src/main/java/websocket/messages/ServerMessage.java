package websocket.messages;

import chess.ChessGame;
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
    GameData gameData;
    String errorMessage;
    String notificationMessage;

    public enum ServerMessageType {
        LOAD_GAME,
        ERROR,
        NOTIFICATION
    }

    public ServerMessage(ServerMessageType type) {
        this.serverMessageType = type;
    }

    public void addGameData(GameData gameData) {
        this.gameData = gameData;
    }

    public void addErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void addNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public ServerMessageType getServerMessageType() {
        return this.serverMessageType;
    }

    public GameData getGameData() {
        return this.gameData;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public String getNotificationMessage() {
        return this.notificationMessage;
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
