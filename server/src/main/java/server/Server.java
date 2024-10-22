package server;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.ListGamesResult;
import model.UserData;
import service.ChessService;
import service.UserService;
import spark.*;

import java.util.List;
import java.util.Map;

public class Server {
    UserDao toplevel_userdao = new UserDao();
    GameDao toplevel_gamedao = new GameDao();

    ChessService chessService = new ChessService(toplevel_userdao, toplevel_gamedao);
    UserService userService = new UserService(toplevel_userdao);

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);

        Spark.get("/game", this::getAllGames);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);

        Spark.delete("/db", this::clearApplication);
        Spark.init();

        //Create an exception handler
        //Write a positive and a negative JUNIT test case for each public method on your Service classes, except for Clear which only needs a positive test case.

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public Object registerUser(Request req, Response res) {
        UserData userData;
        try {
            userData = new Gson().fromJson(req.body(), UserData.class);
            if (userData == null || userData.username() == null || userData.password() == null) {
                throw new InvalidParametersException("Invalid Request");
            }
        } catch (Exception e) {
            return exceptionHandler(new InvalidParametersException("Invalid Request"), req, res);
        }

        try {
            AuthData authData = userService.register(userData);
            res.status(200);
            return new Gson().toJson(authData);
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object loginUser(Request req, Response res) {
        UserData userData;
        try {
            userData = new Gson().fromJson(req.body(), UserData.class);
        } catch (Exception e) {
            return exceptionHandler(new InvalidParametersException("Invalid Request"), req, res);
        }

        if (userData == null || userData.username() == null || userData.password() == null) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        }
        try {
            AuthData authData = userService.login(userData);
            res.status(200);
            return new Gson().toJson(authData);
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object logoutUser(Request req, Response res) {
        try {
            String authToken = req.headers("authorization");
            if (authToken == null) {
                throw new InvalidParametersException("Invalid Request");
            }
            userService.logout(new AuthData(authToken, ""));
            res.status(200);
            return new Gson().toJson(Map.of("message", "Logged out"));
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object getAllGames(Request req, Response res) {
        String authToken = req.headers("authorization");
        if (authToken == null) {
            throw new InvalidParametersException("Invalid Request");
        }
        try {
            userService.verifyAuth(new AuthData(authToken, ""));
            List<GameData> games = chessService.getAllGames();
            res.status(200);
            ListGamesResult response = new ListGamesResult(games);
            return new Gson().toJson(response);
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    //TODO : ask TA about this endpoint
    public Object createGame(Request req, Response res) {
        String gameName = new Gson().fromJson(req.body(), Map.class).get("gameName").toString();
        String authToken = req.headers("authorization");
        if (gameName == null) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        }
        try {
            userService.verifyAuth(new AuthData(authToken, ""));
            Integer gameID = chessService.createGame(gameName);
            res.status(200);
            return new Gson().toJson(Map.of("gameID", gameID));
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object joinGame(Request req, Response res) {
        String authToken = req.headers("authorization");
        Map body = new Gson().fromJson(req.body(), Map.class);
        Integer gameID;
        ChessGame.TeamColor teamColor;
        try {
            gameID = Math.round(Float.parseFloat(body.get("gameID").toString()));
            teamColor = ChessGame.TeamColor.valueOf(body.get("playerColor").toString());
            if (authToken == null || body.get("playerColor") == null || body.get("gameID") == null) {
                res.status(400);
                return new Gson().toJson(Map.of("message", "Error: bad request"));
            }
        } catch (Exception e) {
            return exceptionHandler(new InvalidParametersException("Invalid Request"), req, res);
        }

        try {
            userService.verifyAuth(new AuthData(authToken, ""));
            AuthData auth = userService.getAuthByToken(authToken);
            chessService.joinGame(gameID, teamColor, auth.username());
            res.status(200);
            return new Gson().toJson(Map.of("message", "Joined game"));
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object clearApplication(Request req, Response res) {
        try {
            chessService.clear();
            res.status(200);
            return new Gson().toJson(Map.of("message", "Cleared application"));
        } catch (Exception e) {
            return exceptionHandler(e, req, res);
        }
    }

    public Object exceptionHandler(Exception e, Request req, Response res) {
        if (e instanceof DuplicateInfoException) {
            res.status(403);
            return new Gson().toJson(Map.of("message", "Error: already taken"));
        } else if (e instanceof DataAccessException) {
            res.status(403);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        } else if (e instanceof UnauthorizedException) {
            res.status(401);
            return new Gson().toJson(Map.of("message", "Error: Unauthorized"));
        } else if (e instanceof InvalidParametersException) {
            res.status(400);
            return new Gson().toJson(Map.of("message", "Error: bad request"));
        } else {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}