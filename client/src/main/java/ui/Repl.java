// File: Repl.java
package ui;

import static ui.EscapeSequences.*;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;
    private String username = null;
    private final Object lock = new Object(); // For synchronized access

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl, this); // Pass Repl reference to ChessClient
    }

    public void run() {
        System.out.println(ERASE_SCREEN + SET_TEXT_COLOR_WHITE);
        System.out.println("♔ Welcome to Chess Game ♚");
        System.out.println("Sign in or create an account to start playing");
        System.out.print(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                printOutput(result);

                // Update username when logging in or registering
                if (line.startsWith("login") || line.startsWith("register")) {
                    String[] tokens = line.split(" ");
                    if (tokens.length > 1 && result.contains("logged in")) {
                        username = tokens[1];
                    }
                } else if (line.startsWith("logout") || line.startsWith("clear")) {
                    username = null;
                }
            } catch (Throwable e) {
                printError("Error: " + e.getMessage());
            }
        }
        scanner.close();
        System.out.println(SET_TEXT_COLOR_BLUE + "\nThanks for playing!" + RESET_TEXT_COLOR);
        System.out.println();
    }

    private void printPrompt() {
        var prompt = username == null ? ">>> " : username + " >>> ";
        System.out.print("\n" + SET_TEXT_COLOR_GREEN + prompt + SET_TEXT_COLOR_WHITE);
    }

    // Synchronized method to print regular output
    public void printOutput(String message) {
        synchronized (lock) {
            System.out.print(SET_TEXT_COLOR_WHITE + message);
        }
    }

    // Synchronized method to print errors
    public void printError(String message) {
        synchronized (lock) {
            System.out.print(SET_TEXT_COLOR_RED + message + RESET_TEXT_COLOR);
        }
    }

    // Synchronized method to print messages from WebSocket
    public void printMessage(String message) {
        synchronized (lock) {
            System.out.println("\n" + message);
            printPrompt();
        }
    }
}