package ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;
import model.UserData;

public class ServerFacade {
    private final String serverUrl;
    private WebSocket webSocket;
    private final WebSocket.Listener wsListener;

    public ServerFacade(String serverUrl, WebSocket.Listener wsListener) {
        this.serverUrl = serverUrl;
        this.wsListener = wsListener;
    }

    public void clear() throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);
    }


    public AuthData register(String username, String password, String email) throws Exception {
        var userData = new UserData(username, password, email);
        var path = "/user";
        
        return this.makeRequest("POST", path, userData, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var userData = new UserData(username, password, null);
        var path = "/session";
        
        return this.makeRequest("POST", path, userData, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, authToken, null);
    }

    public ListGamesResult listGames(String authToken) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, null, authToken, ListGamesResult.class);
    }

    public Map createGame(String gameName, String authToken) throws Exception {
        var path = "/game";
        var body = Map.of("gameName", gameName);
        return this.makeRequest("POST", path, body, authToken, Map.class);
    }

    public GameData joinGame(int gameID, ChessGame.TeamColor playerColor, String authToken) throws Exception {
        var path = "/game";
        var body = Map.of(
            "gameID", gameID,
            "playerColor", playerColor != null ? playerColor.toString() : null
        );
        return this.makeRequest("PUT", path, body, authToken, GameData.class);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass) throws Exception {
        try {
            URL url = new URI(serverUrl + path).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.addRequestProperty("Content-Type", "application/json");

            if (authToken != null) {
                http.addRequestProperty("authorization", authToken);
            }

            if (request != null) {
                writeBody(request, http);
            }

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, Exception {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respBody = http.getErrorStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                Map<String, String> errorResponse = new Gson().fromJson(reader, Map.class);
                String message = errorResponse.get("message");
                throw new Exception(message);
            }
        }
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }


    //WEBSOCKET
    public void connectToWebSocket() throws Exception {
        String wsUri = serverUrl.replace("http", "ws") + "/connect";
        HttpClient client = HttpClient.newHttpClient();
        
        CompletableFuture<WebSocket> ws = client.newWebSocketBuilder()
            .buildAsync(URI.create(wsUri), wsListener);
            
        webSocket = ws.join();
    }

    public void disconnectWebSocket() {
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Client disconnecting");
        }
    }

    public void sendWebSocketMessage(String message) {
        if (webSocket != null) {
            webSocket.sendText(message, true);
        }
    }

}
