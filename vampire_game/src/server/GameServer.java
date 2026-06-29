package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameServer {
    private static final int PORT = 5555;
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;

    private ServerSocket serverSocket;
    private Map<String, GameRoom> rooms;
    private Random random;

    public GameServer() {
        rooms = new HashMap<>();
        random = new Random();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected from " + socket.getInetAddress());

                ClientHandler handler = new ClientHandler(socket, this);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    public synchronized GameRoom createRoom() {
        String code;
        do {
            code = generateRoomCode();
        } while (rooms.containsKey(code));

        GameRoom room = new GameRoom(code);
        rooms.put(code, room);
        System.out.println("Room created: " + code + " (total rooms: " + rooms.size() + ")");
        return room;
    }

    public synchronized GameRoom getRoom(String code) {
        return rooms.get(code);
    }

    public synchronized void removeRoom(String code) {
        rooms.remove(code);
        System.out.println("Room removed: " + code + " (total rooms: " + rooms.size() + ")");
    }

    private String generateRoomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }
}