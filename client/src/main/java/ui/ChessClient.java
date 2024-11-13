package ui;

import java.util.Arrays;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;

public class ChessClient {
    private String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private StateEnum state = StateEnum.SIGNEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> createGame(params);
                case "list" -> listGames();
                case "play" -> playGame(params);
                case "observe" -> observeGame(params);
                case "clear" -> clear();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    public String register(String... params) throws Exception {
        if (params.length >= 3) {
            String username = params[0];
            String password = params[1];
            String email = params[2];
            
            AuthData auth = server.register(username, password, email);
            authToken = auth.authToken();
            state = StateEnum.SIGNEDIN;
            return String.format("You registered and logged in as %s.", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD> <EMAIL>");
    }

    public String login(String... params) throws Exception {
        if (params.length >= 2) {
            String username = params[0];
            String password = params[1];
            
            AuthData auth = server.login(username, password);
            authToken = auth.authToken();
            state = StateEnum.SIGNEDIN;
            return String.format("You logged in as %s.", username);
        }
        throw new Exception("Expected: <USERNAME> <PASSWORD>");
    }

    public String logout() throws Exception {
        assertSignedIn();
        server.logout(authToken);
        authToken = null;
        state = StateEnum.SIGNEDOUT;
        return "You have been logged out.";
    }

    public String createGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            var response = server.createGame(gameName, authToken);
            Double gameIdDouble = (Double) response.get("gameID");
            int gameId = gameIdDouble.intValue();
            return String.format("Created game %s with ID: %d", gameName, gameId);
        }
        throw new Exception("Expected: <GAME_NAME>");
    }

    public String listGames() throws Exception {
        assertSignedIn();
        ListGamesResult result = server.listGames(authToken);
        if (result.games().isEmpty()) {
            return "No games available.";
        }
        
        StringBuilder output = new StringBuilder();
        output.append("Available Games:\n");
        int index = 1;
        for (GameData game : result.games()) {
            output.append(String.format("%d. %s (ID: %d)\n", index++, game.gameName(), game.gameID()));
            output.append(String.format("   White: %s\n", game.whiteUsername() != null ? game.whiteUsername() : "EMPTY"));
            output.append(String.format("   Black: %s\n", game.blackUsername() != null ? game.blackUsername() : "EMPTY"));
        }
        return output.toString();
    }

    public String playGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 2) {
            int gameId = Integer.parseInt(params[0]);
            String color = params[1].toUpperCase();
            ChessGame.TeamColor teamColor = switch (color) {
                case "WHITE" -> ChessGame.TeamColor.WHITE;
                case "BLACK" -> ChessGame.TeamColor.BLACK;
                default -> throw new Exception("Invalid team color. Use WHITE or BLACK");
            };
            
            server.joinGame(gameId, teamColor, authToken);
            ListGamesResult result = server.listGames(authToken);
            GameData game = result.games().stream()
                .filter(g -> g.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Game not found"));
            String gameView = getGameView(game, ViewEnum.VIEW);
            return String.format("You joined game %d as %s\n%s", gameId, color, gameView);
        }
        throw new Exception("Expected: <GAME_ID> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            int gameId = Integer.parseInt(params[0]);
            ListGamesResult result = server.listGames(authToken);
            GameData game = result.games().stream()
                .filter(g -> g.gameID() == gameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Game not found"));
            String gameView = getGameView(game, ViewEnum.OBSERVE);
            return String.format("You are now observing game %d\n%s", gameId, gameView);
        }
        throw new Exception("Expected: <GAME_ID>");
    }

    public String clear() throws Exception {
        assertSignedIn();
        server.logout(authToken);
        server.clear();
        return "Cleared application.";
    }

    public String help() {
        if (state == StateEnum.SIGNEDOUT) {
            return """
                Available commands:
                - register <USERNAME> <PASSWORD> <EMAIL>
                - login <USERNAME> <PASSWORD>
                - help
                - quit
                """;
        }
        return """
            Available commands:
            - create <GAME_NAME>
            - list
            - play <GAME_ID> <WHITE|BLACK>
            - observe <GAME_ID>
            - logout
            - help
            - quit
            - clear
            """;
    }

    private void assertSignedIn() throws Exception {
        if (state == StateEnum.SIGNEDOUT) {
            throw new Exception("You must sign in first.");
        }
    }

    private String getGameView(GameData game) {
        ChessGame chessGame = game.game();
        StringBuilder view = new StringBuilder();
        
        // White's view (bottom to top)
        view.append("WHITE's view:\n");
        ChessGame.TeamColor[] colors = {ChessGame.TeamColor.WHITE, ChessGame.TeamColor.BLACK};
        for (int row = 7; row >= 0; row--) {
            view.append(row + 1).append(" ");
            for (int col = 0; col < 8; col++) {
                var piece = chessGame.getBoard().getPiece(new chess.ChessPosition(row + 1, col + 1));
                if ((row + col) % 2 == 0) {
                    view.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                } else {
                    view.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }
                if (piece == null) {
                    view.append("   ");
                } else {
                    view.append(" ").append(piece.toString()).append(" ");
                }
                view.append(EscapeSequences.RESET_BG_COLOR);
            }
            view.append("\n");
        }
        view.append("  a  b  c  d  e  f  g  h\n\n");

        // Black's view (top to bottom)
        view.append("BLACK's view:\n");
        for (int row = 0; row < 8; row++) {
            view.append(row + 1).append(" ");
            for (int col = 7; col >= 0; col--) {
                var piece = chessGame.getBoard().getPiece(new chess.ChessPosition(row + 1, col + 1));
                if ((row + col) % 2 == 0) {
                    view.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                } else {
                    view.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }
                if (piece == null) {
                    view.append("   ");
                } else {
                    view.append(" ").append(piece.toString()).append(" ");
                }
                view.append(EscapeSequences.RESET_BG_COLOR);
            }
            view.append("\n");
        }
        view.append("  h  g  f  e  d  c  b  a");

        return view.toString();
    }
}
