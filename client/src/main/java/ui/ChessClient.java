package ui;

import java.util.*;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;

public class ChessClient {
    private String authToken = null;
    private final ServerFacade server;
    private  ClientWebsocketHandler websocket;
    private final String serverUrl;
    private StateEnum state = StateEnum.SIGNEDOUT;
    private Map<Integer, Integer> gameIDMap = new HashMap<>();
    private Integer currentGameId = null;
    private GameData currentGame = null;
    private ChessGame.TeamColor currentTeam = null;
    private ViewEnum currentView = ViewEnum.VIEW;
    private final Repl repl;

    public ChessClient(String serverUrl, Repl repl) {
        server = new ServerFacade(serverUrl);
        websocket = new ClientWebsocketHandler(serverUrl, this);
        this.serverUrl = serverUrl;
        this.repl = repl;
    }

    public String eval(String input) {
        try {
            var tokens = input.toLowerCase().split(" ");
            var cmd = (tokens.length > 0) ? tokens[0] : "help";
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (this.state == StateEnum.SIGNEDOUT) {
                if (cmd.equals("register") || cmd.equals("login") || cmd.equals("help") || cmd.equals("quit")) {
                    return switch (cmd) {
                        case "register" -> register(params);
                        case "login" -> login(params);
                        case "help" -> help();
                        case "quit" -> "quit";
                        default -> help();
                    };
                }
                return "You must sign in first.";
            }
            else if (this.state == StateEnum.INGAME) {
                if (cmd.equals("move") || cmd.equals("highlight") ||
                        cmd.equals("redraw") || cmd.equals("leave") ||
                        cmd.equals("resign") || cmd.equals("help")) {
                    return switch (cmd) {
                        case "move" -> makeMove(params);
                        case "highlight" -> highlightLegalMoves(params);
                        case "redraw" -> getGameView(currentGame, currentView, currentTeam == ChessGame.TeamColor.WHITE);
                        case "leave" -> leaveGame();
                        case "resign" -> resignGame();
                        case "help" -> help();
                        default -> help();
                    };
                }
                return "Invalid command.";
            }
            else {
                if (cmd.equals("register") || cmd.equals("login") ||
                        cmd.equals("logout") || cmd.equals("create") ||
                        cmd.equals("list") || cmd.equals("play") || cmd.equals("observe") ||
                        cmd.equals("clear") || cmd.equals("quit") || cmd.equals("help")) {
                    return switch (cmd) {
                        case "logout" -> logout();
                        case "create" -> createGame(params);
                        case "list" -> listGames();
                        case "play" -> playGame(params);
                        case "observe" -> observeGame(params);
                        case "clear" -> clear();
                        case "quit" -> "quit";
                        default -> help();
                    };
                };
                return "Invalid command.";
            }
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
        currentTeam = null;
        currentGame = null;
        return "You have been logged out.";
    }

    public String createGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            String gameName = String.join(" ", params);
            var response = server.createGame(gameName, authToken);
            Double gameIdDouble = (Double) response.get("gameID");
            int gameId = gameIdDouble.intValue();
            int mapSize = gameIDMap.size() + 1;
            gameIDMap.put(mapSize, gameId);
            return String.format("Created game %s with ID: %d", gameName, mapSize);
        }
        throw new Exception("Expected: <GAME_NAME>");
    }

    public String listGames() throws Exception {
        assertSignedIn();
        ListGamesResult result = server.listGames(authToken);
        if (result.games().isEmpty()) {
            return "No games available.";
        }
        //update the map
        for (int i = 0; i < result.games().size(); i++) {
            gameIDMap.put(i + 1, result.games().get(i).gameID());
        }
        StringBuilder output = new StringBuilder();
        output.append("Available Games:\n");
        int index = 1;
        for (GameData game : result.games()) {
            output.append(String.format("%s (ID: %d)\n", game.gameName(), index));
            output.append(String.format("   White: %s\n", game.whiteUsername() != null ? game.whiteUsername() : "EMPTY"));
            output.append(String.format("   Black: %s\n", game.blackUsername() != null ? game.blackUsername() : "EMPTY"));
            index++;
        }
        return output.toString();
    }

    public String playGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 2) {
            int gameId = Integer.parseInt(params[0]);
            if (!gameIDMap.containsKey(gameId)) {
                throw new Exception("Game not found");
            }
            int oldGameId = gameId;
            gameId = gameIDMap.get(gameId);
            String color = params[1].toUpperCase();
            ChessGame.TeamColor teamColor = switch (color) {
                case "WHITE" -> ChessGame.TeamColor.WHITE;
                case "BLACK" -> ChessGame.TeamColor.BLACK;
                default -> throw new Exception("Invalid team color. Use WHITE or BLACK");
            };
            server.joinGame(gameId, teamColor, authToken);
            websocket = new ClientWebsocketHandler(serverUrl, this);
            websocket.connect(authToken, gameId, teamColor);
            currentView = ViewEnum.VIEW;
            ListGamesResult result = server.listGames(authToken);
            int finalGameId = gameId;
            GameData game = result.games().stream()
                .filter(g -> g.gameID() == finalGameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Game not found"));
            state = StateEnum.INGAME;
            currentGameId = gameId;
            currentTeam = teamColor;
            currentGame = game;
            return String.format("You joined game %d as %s", oldGameId, color);
        }
        throw new Exception("Expected: <GAME_ID> <WHITE|BLACK>");
    }

    public String observeGame(String... params) throws Exception {
        assertSignedIn();
        if (params.length >= 1) {
            int gameId = Integer.parseInt(params[0]);
            if (!gameIDMap.containsKey(gameId)) {
                throw new Exception("Game not found");
            }
            int oldGameId = gameId;
            gameId = gameIDMap.get(gameId);
            websocket.observe(authToken, gameId);
            currentView = ViewEnum.OBSERVE;
            ListGamesResult result = server.listGames(authToken);
            int finalGameId = gameId;
            GameData game = result.games().stream()
                .filter(g -> g.gameID() == finalGameId)
                .findFirst()
                .orElseThrow(() -> new Exception("Game not found"));
            String gameView = getGameView(game, currentView, false);
            return String.format("You are now observing game %d\n%s", oldGameId, gameView);
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
        else if (state == StateEnum.INGAME) {
            return """
                Available commands:
                - move <start row> <start col> <end row> <end col>
                - highlight <start row> <start col> (shows legal moves for given piece)
                - redraw
                - leave
                - resign
                - help
                """;
        } 
        else {
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
    }

    private void assertSignedIn() throws Exception {
        if (state == StateEnum.SIGNEDOUT) {
            throw new Exception("You must sign in first.");
        }
    }

    private String getGameView(GameData game, ViewEnum viewType, boolean isWhiteView) {
        ChessGame chessGame = game.game();
        StringBuilder view = new StringBuilder();

        if (currentGame == null) {
            this.currentGame = game;
        }
    
        // Draw for white players
        if (viewType == ViewEnum.VIEW && isWhiteView) {
            view.append("WHITE's view: (Team Turn :" + this.currentGame.game().getTeamTurn().toString() + ")\n");
            drawBoardView(view, chessGame, true, new ArrayList<>());
        }

        // Draw for black players
        if (viewType == ViewEnum.VIEW && !isWhiteView) {
            view.append("BLACK's view: (Team Turn: " + this.currentGame.game().getTeamTurn().toString() + ")\n");
            drawBoardView(view, chessGame, false, new ArrayList<>());
        }
        
        // Draw for Observers
        if (viewType == ViewEnum.OBSERVE) {
            view.append("OBSERVER's view: (Team Turn: " + this.currentGame.game().getTeamTurn().toString() + ")\n");
            view.append(getGameView(game, ViewEnum.VIEW, true));
        }
    
        return view.toString();
    }
    
    private void drawBoardView(StringBuilder view, ChessGame chessGame, boolean isWhiteView, Collection<ChessMove> validMoves) {
        for (int row = isWhiteView ? 7 : 0; isWhiteView ? row >= 0 : row < 8; row += isWhiteView ? -1 : 1) {
            view.append(row + 1).append(" ");
            for (int col = isWhiteView ? 0 : 7; isWhiteView ? col < 8 : col >= 0; col += isWhiteView ? 1 : -1) {
                ChessPosition position = new ChessPosition(row + 1, col + 1);
                var piece = chessGame.getBoard().getPiece(position);
                if (validMoves.stream().anyMatch(move -> move.getEndPosition().equals(position))) {
                    view.append(EscapeSequences.SET_BG_COLOR_YELLOW);
                } else if ((row + col) % 2 == 0) {
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

        view.append(isWhiteView ? "  a  b  c  d  e  f  g  h" : "  h  g  f  e  d  c  b  a");
    }

    private String highlightLegalMoves(String... params) throws Exception {
        assertSignedIn();

        if (currentGame == null || currentTeam == null) {
            throw new Exception("No game or team selected.");
        }

        if (params.length < 2) {
            throw new Exception("Expected: <row> <col>");
        }

        StringBuilder view = new StringBuilder();
        ChessGame chessGame = currentGame.game();

        int row = Integer.parseInt(params[0]);
        int col = convertColumnLetterToNumber(params[1]);

        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = chessGame.getBoard().getPiece(position);
        if (piece.getPieceType() == null) {
            throw new Exception("No piece at that position.");
        }
        if (piece.getTeamColor() != currentTeam) {
            throw new Exception("You can only highlight moves for your own pieces.");
        }

        Collection<ChessMove> validMoves = chessGame.validMoves(position);
        drawBoardView(view, chessGame, currentTeam == ChessGame.TeamColor.WHITE, validMoves);
        return view.toString();
    }

    public String makeMove(String... params) throws Exception {
        assertSignedIn();
        if (currentGame == null || currentTeam == null) {
            throw new Exception("No game or team selected.");
        }

        if (params.length < 4) {
            throw new Exception("Expected: <start row> <start col> <end row> <end col>");
        }

        int startRow = Integer.parseInt(params[0]);
        int startCol = convertColumnLetterToNumber(params[1]);
        int endRow = Integer.parseInt(params[2]);
        int endCol = convertColumnLetterToNumber(params[3]);

        ChessPosition startPosition = new ChessPosition(startRow, startCol);
        ChessPosition endPosition = new ChessPosition(endRow, endCol);
        Boolean moveNeedsPromotion = checkIfMoveNeedsPromotion(startPosition, endPosition);
        ChessPiece.PieceType promotionPiece = null;
        if (moveNeedsPromotion) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter the piece you want to promote to (Q, R, B, N): ");
            String promotionPieceType = scanner.nextLine().trim().toUpperCase();
            promotionPiece = switch (promotionPieceType) {
                case "Q" -> ChessPiece.PieceType.QUEEN;
                case "R" -> ChessPiece.PieceType.ROOK;
                case "B" -> ChessPiece.PieceType.BISHOP;
                case "N" -> ChessPiece.PieceType.KNIGHT;
                default -> throw new Exception("Invalid promotion piece.");
            };
        }


        ChessMove move = new ChessMove(startPosition, endPosition, promotionPiece);
        websocket.makeMove(authToken, currentGameId, move);
        return "";
    }

    public String leaveGame() throws Exception {
        if (currentGame == null) {
            throw new Exception("You are not currently in a game.");
        }

        websocket.leave(authToken, currentGameId, currentTeam);
        currentGame = null;
        currentTeam = null;
        currentGameId = null;
        state = StateEnum.SIGNEDIN;
        currentView = ViewEnum.VIEW;
        this.updateLoadedGames();
        return "You have left the game.";
    }

    public String resignGame() throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Are you sure you want to resign? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        if (response.equals("y")) {
            websocket.resign(authToken, currentGameId);
            currentGame = null;
            currentTeam = null;
            currentGameId = null;
            state = StateEnum.SIGNEDIN;
            this.updateLoadedGames();
            return "You have resigned from the game.";
        } else {
            return "Resignation cancelled.";
        }
    }

    private void updateLoadedGames() throws Exception {
        ListGamesResult result = server.listGames(authToken);
        for (int i = 0; i < result.games().size(); i++) {
            gameIDMap.put(i + 1, result.games().get(i).gameID());
        }
    }

    private Integer convertColumnLetterToNumber(String col) throws Exception {
        return switch (col) {
            case "a" -> 1;
            case "b" -> 2;
            case "c" -> 3;
            case "d" -> 4;
            case "e" -> 5;
            case "f" -> 6;
            case "g" -> 7;
            case "h" -> 8;
            default -> throw new Exception("Invalid column letter.");
        };
    }

    private Boolean checkIfMoveNeedsPromotion(ChessPosition startPosition, ChessPosition endPosition) {
        ChessGame chessGame = currentGame.game();
        ChessPiece piece = chessGame.getBoard().getPiece(startPosition);
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE && endPosition.getRow() == 8) {
                return true;
            }
            return piece.getTeamColor() == ChessGame.TeamColor.BLACK && endPosition.getRow() == 1;
        }
        return false;
    }

    //WEBSOCKET UPDATERS
    public void loadGame(GameData game) {
        this.currentGame = game;
        String gameView = getGameView(game, currentView, currentTeam == ChessGame.TeamColor.WHITE);
        repl.printMessage(gameView);
    }

    public void notification(String message) {
        if (message.contains("resigned")) {
            currentGame = null;
            currentTeam = null;
            currentGameId = null;
            state = StateEnum.SIGNEDIN;
            currentView = ViewEnum.VIEW;
        }
        repl.printMessage(EscapeSequences.SET_TEXT_COLOR_BLUE + message + EscapeSequences.RESET_TEXT_COLOR);
    }

    public void errorMessage(String message) {
        repl.printMessage(EscapeSequences.SET_TEXT_COLOR_RED + message + EscapeSequences.RESET_TEXT_COLOR);
    }


}

