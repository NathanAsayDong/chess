package server;

import com.google.gson.Gson;
import model.UserData;
import service.ChessService;
import spark.*;

public class Server {
    ChessService chessService = new ChessService();

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
        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public String registerUser(Request req, Response res) {
        UserData userData = new Gson().fromJson(req.body(), UserData.class);
        chessService.createUser(userData);
        return "User registered";
    }

    public String loginUser(Request req, Response res) {
        return "User logged in";
    }

    public String logoutUser(Request req, Response res) {
        return "User logged out";
    }

    public String getAllGames(Request req, Response res) {
        return "All games";
    }

    public String createGame(Request req, Response res) {
        return "Game created";
    }

    public String joinGame(Request req, Response res) {
        return "Game joined";
    }

    public String clearApplication(Request req, Response res) {
        return "Application cleared";
    }
}
