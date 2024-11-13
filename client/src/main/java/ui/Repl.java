package ui;

import static ui.EscapeSequences.*;

import java.util.Scanner;

public class Repl {
    private final ChessClient client;
    private String username = null;

    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
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
                System.out.print(SET_TEXT_COLOR_WHITE + result);
                
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
                System.out.print(SET_TEXT_COLOR_RED + "Error: " + e.getMessage() + RESET_TEXT_COLOR);
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
}