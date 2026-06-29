package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Message;

public class GameClient extends Thread {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 5555;

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private MessageListener listener;

    public void connect() throws IOException {
        socket = new Socket(SERVER_HOST, SERVER_PORT);
        System.out.println("Connected to server.");

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    public void setListener(MessageListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                if (listener != null) {
                    listener.onMessage(msg);
                } else {
                    System.out.println(msg);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Disconnected from server: " + e.getMessage());
        }
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending: " + e.getMessage());
        }
    }
}
