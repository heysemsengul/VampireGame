package client;

import java.io.IOException;
import java.util.Scanner;

import shared.Message;
import shared.MessageType;

public class ClientMain {
    public static void main(String[] args) {
        GameClient client = new GameClient();

        try {
            client.connect();
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            return;
        }

        client.start();

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();

        System.out.println();
        System.out.println("[1] Create new room");
        System.out.println("[2] Join existing room");
        System.out.print("Choose: ");
        String choice = scanner.nextLine();

        if (choice.equals("1")) {
            client.sendMessage(new Message(MessageType.CREATE_ROOM, name, ""));
        } else if (choice.equals("2")) {
            System.out.print("Enter room code: ");
            String code = scanner.nextLine();
            client.sendMessage(new Message(MessageType.JOIN_ROOM, name, code));
        } else {
            System.out.println("Invalid choice. Exiting.");
            System.exit(0);
        }

        try { Thread.sleep(500); } catch (InterruptedException e) {}

        System.out.println();
        printHelp();

        while (true) {
            String line = scanner.nextLine();

            if (line.equals("quit")) {
                break;
            } else if (line.equals("/help")) {
                printHelp();
            } else if (line.equals("/start")) {
                client.sendMessage(new Message(MessageType.START_GAME, name, ""));
            } else if (line.startsWith("/target ")) {
                String target = line.substring(8).trim();
                client.sendMessage(new Message(MessageType.NIGHT_ACTION, name, target));
            } else if (line.startsWith("/vote ")) {
                String target = line.substring(6).trim();
                client.sendMessage(new Message(MessageType.VOTE, name, target));
            } else if (line.startsWith("/")) {
                System.out.println("Unknown command. Type /help.");
            } else if (!line.isEmpty()) {
                client.sendMessage(new Message(MessageType.CHAT, name, line));
            }
        }

        scanner.close();
        System.exit(0);
    }

    private static void printHelp() {
        System.out.println("=== Commands ===");
        System.out.println("  /start        — host starts the game");
        System.out.println("  /target NAME  — at night, pick someone to act on");
        System.out.println("  /vote NAME    — during VOTE phase, vote to eliminate");
        System.out.println("  /help         — show this list");
        System.out.println("  quit          — exit the client");
        System.out.println("Anything else is chat.");
        System.out.println();
    }
}