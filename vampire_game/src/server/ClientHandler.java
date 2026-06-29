package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import shared.Message;
import shared.MessageType;
import shared.Phase;
import shared.Vampire;

public class ClientHandler extends Thread {
    private static final long CHAT_COOLDOWN_MS = 3000;

    private Socket socket;
    private GameServer server;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private GameRoom currentRoom;
    private String playerName;
    private long lastChatTime = 0;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error setting up streams: " + e.getMessage());
        }
    }

    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Message msg = (Message) in.readObject();
                handleMessage(msg);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    private void handleMessage(Message msg) {
        switch (msg.getType()) {
        	case CREATE_ROOM:  handleCreateRoom(msg);  break;
        	case JOIN_ROOM:    handleJoinRoom(msg);    break;
            case CHAT:         handleChat(msg);        break;
            case START_GAME:   handleStartGame(msg);   break;
            case NIGHT_ACTION: handleNightAction(msg); break;
            case VOTE:         handleVote(msg);        break;
            case SKIP_PHASE:   handleSkipPhase();   break;
            case UPDATE_SETTINGS: handleUpdateSettings(msg.getContent()); break;
            case LEAVE_ROOM:      handleLeaveRoom();                      break;
            default:                                   break;
        }
    }
    
    private void handleSkipPhase() {
        if (currentRoom == null) return;
        if (this != currentRoom.getHost()) {
            sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Only the host can skip phases."));
            return;
        }
        GameManager manager = currentRoom.getManager();
        if (manager == null) return;
        manager.requestPhaseSkip();
    }
    
    private void handleLeaveRoom() {
        if (currentRoom == null) return;
        GameRoom room = currentRoom;
        GameManager manager = room.getManager();
        currentRoom = null;

        if (manager != null) {
            manager.handlePlayerLeave(this);
        }
        room.removePlayer(this);

        sendMessage(new Message(MessageType.SYSTEM, "SERVER", "You left the room."));
    }
    
    private void handleUpdateSettings(String content) {
        if (currentRoom == null) return;
        if (this != currentRoom.getHost()) {
        	sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Only the host can change settings."));
            return;
        }
        try {
            String[] parts = content.split(",");
            int discussion  = Integer.parseInt(parts[0].trim());
            int vote        = Integer.parseInt(parts[1].trim());
            int night       = Integer.parseInt(parts[2].trim());
            int playerCount = Integer.parseInt(parts[3].trim());
            currentRoom.setSettings(new shared.GameSettings(discussion, vote, night, playerCount));
        } catch (Exception e) {
            sendMessage(new Message(MessageType.SYSTEM, "SERVER","Invalid settings format."));
        }
    }

    private void handleCreateRoom(Message msg) {
        playerName = msg.getSender();
        GameRoom room = server.createRoom();
        currentRoom = room;
        sendMessage(new Message(MessageType.ROOM_CREATED, "SERVER", room.getCode()));
        room.addPlayer(this);
    }

    private void handleJoinRoom(Message msg) {
        playerName = msg.getSender();
        GameRoom room = server.getRoom(msg.getContent().toUpperCase());
        if (room == null) {
            sendMessage(new Message(MessageType.ROOM_ERROR, "SERVER", "No room with code " + msg.getContent()));
            return;
        }
        currentRoom = room;
        sendMessage(new Message(MessageType.ROOM_JOINED, "SERVER", room.getCode()));
        room.addPlayer(this);
    }

    private void handleStartGame(Message msg) {
        if (currentRoom == null) {
            sendMessage(new Message(MessageType.ROOM_ERROR, "SERVER", "Not in a room."));
            return;
        }
        GameManager manager = currentRoom.initManager();
        manager.startGame(this);
    }

    private void handleNightAction(Message msg) {
        if (currentRoom == null) return;
        GameManager manager = currentRoom.getManager();
        if (manager == null) return;
        manager.registerNightAction(this, msg.getContent());
    }

    private void handleVote(Message msg) {
        if (currentRoom == null) return;
        GameManager manager = currentRoom.getManager();
        if (manager == null) return;
        manager.registerVote(this, msg.getContent());
    }

    private void handleChat(Message msg) {
        if (currentRoom == null) {
            sendMessage(new Message(MessageType.ROOM_ERROR, "SERVER", "You are not in a room yet."));
            return;
        }

        long now = System.currentTimeMillis();
        
        if (now - lastChatTime < CHAT_COOLDOWN_MS) {
            sendMessage(new Message(MessageType.SYSTEM, "SERVER", "Slow down — wait before sending again."));
            return;
        }
        lastChatTime = now;

        GameManager manager = currentRoom.getManager();

        if (manager == null || manager.getCurrentPhase() == Phase.GAME_OVER) {
            currentRoom.broadcast(msg);
            return;
        }

        Phase phase = manager.getCurrentPhase();
        boolean alive = manager.isAlive(this);

        if (!alive) {
            Message tagged = new Message(MessageType.CHAT, "[dead] " + msg.getSender(), msg.getContent());
            for (ClientHandler p : currentRoom.getPlayers()) {
                if (!manager.isAlive(p)) {
                    p.sendMessage(tagged);
                }
            }
            return;
        }

        if (phase == Phase.NIGHT) {
            if (manager.getRole(this) instanceof Vampire) {
                Message tagged = new Message(MessageType.CHAT, "[vamp] " + msg.getSender(), msg.getContent());
                for (ClientHandler p : currentRoom.getPlayers()) {
                    boolean isAlivePlayer = manager.isAlive(p);
                    boolean isVamp = manager.getRole(p) instanceof Vampire;
                    if ((isAlivePlayer && isVamp) || !isAlivePlayer) {
                        p.sendMessage(tagged);
                    }
                }
            } else {
                sendMessage(new Message(MessageType.SYSTEM, "SERVER", "It's night — you cannot speak."));
            }
            return;
        }

        currentRoom.broadcast(msg);
    }

    public void sendMessage(Message message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
    }

    private void cleanup() {
    	if (currentRoom != null) {
    	    GameRoom r = currentRoom;
    	    GameManager m = r.getManager();
    	    if (m != null) {
    	        m.handlePlayerLeave(this);
    	    }
    	    r.removePlayer(this);
    	    currentRoom = null;
    	}
        try { socket.close(); } catch (IOException e) { }
    }
}