package ui;

import java.net.InetSocketAddress;
import java.net.http.WebSocket;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;


public class WebsocketServer {
    private int PORT = 8080;
    private final Map<WebSocket, String> connections = new HashMap<>();
    
    public WebsocketServer() {

    }

    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        // Connection opened
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        connections.remove(conn);
    }

    public void onMessage(WebSocket conn, String message) {
        try {
            UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
            
            switch (command.getCommandType()) {
                case CONNECT:
                    handleConnect(conn, command);
                    break;
                case MAKE_MOVE:
                    handleMove(conn, command);
                    break;
                case LEAVE:
                    handleLeave(conn, command);
                    break;
                case RESIGN:
                    handleResign(conn, command);
                    break;
            }
        } catch (Exception e) {
            sendErrorMessage(conn, "Error processing message: " + e.getMessage());
        }
    }

    public void onError(WebSocket conn, Exception ex) {
        if (conn != null) {
            connections.remove(conn);
        }
        System.err.println("WebSocket error: " + ex.getMessage());
    }

    public void onStart() {
        System.out.println("WebSocket server started");
    }

    private void handleConnect(WebSocket conn, UserGameCommand command) {
        connections.put(conn, command.getAuthToken());
        // Additional connection logic here
    }

    private void handleMove(WebSocket conn, UserGameCommand command) {
        // Handle move logic
    }

    private void handleLeave(WebSocket conn, UserGameCommand command) {
        connections.remove(conn);
        // Additional leave logic here
    }

    private void handleResign(WebSocket conn, UserGameCommand command) {
        // Handle resign logic
    }

    private void sendErrorMessage(WebSocket conn, String message) {
        ServerMessage errorMessage = new ServerMessage(ServerMessage.ServerMessageType.ERROR);
        conn.send(new Gson().toJson(errorMessage));
    }
}
