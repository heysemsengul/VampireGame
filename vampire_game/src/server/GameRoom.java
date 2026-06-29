package server;

import java.util.ArrayList;
import java.util.List;

import shared.GameSettings;
import shared.Message;

public class GameRoom {
    private String code;
    private List<ClientHandler> players;
    private GameSettings settings;
    private ClientHandler host;
    private GameManager manager;

    public GameRoom(String code) {
        this.code = code;
        this.players = new ArrayList<>();
        this.settings = new GameSettings();  // start with defaults
    }

    public String getCode() {
        return code;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public void setSettings(GameSettings settings) {
        this.settings = settings;
        broadcastSettings();
    }

    public ClientHandler getHost() {
        return host;
    }

    public GameManager getManager() {
        return manager;
    }

    public synchronized GameManager initManager() {
        if (manager == null) {
            manager = new GameManager(this, settings);
        }
        return manager;
    }

    public List<ClientHandler> getPlayers() {
        synchronized (players) {
            return new ArrayList<>(players);
        }
    }

    public void addPlayer(ClientHandler player) {
        synchronized (players) {
            if (players.isEmpty()) {
                host = player;
            }
            players.add(player);
            broadcastRoomMembers();
            broadcastSettings();
        }
    }

    public void removePlayer(ClientHandler player) {
        synchronized (players) {
            players.remove(player);
            if (player == host) {
                host = players.isEmpty() ? null : players.get(0);
            }
            broadcastRoomMembers();
        }
    }

    public int getPlayerCount() {
        synchronized (players) {
            return players.size();
        }
    }

    public boolean isEmpty() {
        synchronized (players) {
            return players.isEmpty();
        }
    }

    private void broadcastRoomMembers() {
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            if (i > 0) names.append(",");
            names.append(players.get(i).getPlayerName());
        }
        broadcast(new shared.Message(
            shared.MessageType.PLAYER_LIST, "SERVER", names.toString()));
    }
    
    private void broadcastSettings() {
        GameSettings s = settings;
        String content = s.getDiscussionSeconds() + ","
                       + s.getVoteSeconds()       + ","
                       + s.getNightSeconds()      + ","
                       + s.getPlayerCount();
        broadcast(new shared.Message(
            shared.MessageType.SETTINGS_INFO, "SERVER", content));
    }
    
    public void broadcast(Message message) {
        synchronized (players) {
            for (ClientHandler p : players) {
                p.sendMessage(message);
            }
        }
    }
}