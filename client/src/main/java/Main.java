import chess.ChessGame;
import chess.ChessPiece;
import ui.Repl;

public class Main {
    public static void main(String[] args) {
        System.out.println("♕ 240 Chess Client Initialized");

        Repl repl = new Repl("http://localhost:8080");
        
        Thread replThread = new Thread(repl::run);
        replThread.start();
        
        try {
            replThread.join();
        } catch (InterruptedException e) {
            System.err.println("REPL thread interrupted: " + e.getMessage());
        }
    }
}