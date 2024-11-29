package ui;


import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;

public class ClientWebsocketHandler {
    private final String serverUrl;
    private final WebSocket.Listener wsListener;
    private WebSocket webSocket;

    public ClientWebsocketHandler(String serverUrl, WebSocket.Listener wsListener) {
        this.serverUrl = serverUrl;
        this.wsListener = wsListener;
    }

    public void connect(String authToken) {
        var uri = URI.create(serverUrl + "/game");
        var headers = Map.of("Authorization", authToken);
        var client = HttpClient.newHttpClient();
        this.webSocket = client.newWebSocketBuilder()
                .buildAsync(uri, wsListener)
                .join();
    }

    public void disconnect() {
        this.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnecting");
    }

    public void sendCommand(UserGameCommand command) {
        var gson = new Gson();
        var json = gson.toJson(command);
        this.webSocket.sendText(json, true);
    }


    public void joinGame(String authToken, int gameID) {
        UserGameCommand command = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        sendCommand(command);
    }

}
