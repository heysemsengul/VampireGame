package client.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import client.GameClient;
import client.MessageListener;
import shared.Message;
import shared.MessageType;

public class MainFrame extends JFrame implements MessageListener {

    public static final String CARD_CONNECT = "CONNECT";
    public static final String CARD_LOBBY   = "LOBBY";
    public static final String CARD_ROOM    = "ROOM";
    public static final String CARD_GAME    = "GAME";
    public static final String CARD_END     = "END";
    
    private LobbyPanel lobbyPanel;
    private RoomPanel roomPanel;
    private GamePanel gamePanel;
    private EndPanel endPanel;
    private String myRole = "—";
    private Map<String, String> finalRoles = new java.util.LinkedHashMap<>();
    private String currentCard;

    private GameClient gameClient;
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private String playerName;
    private String currentRoomCode;

    public MainFrame() {
        setTitle("Vampire Village");
        setSize(900, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 

        gameClient = new GameClient();
        gameClient.setListener(this);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Theme.background);

        cardPanel.add(new ConnectPanel(this), CARD_CONNECT);
        lobbyPanel = new LobbyPanel(this);
        cardPanel.add(lobbyPanel, CARD_LOBBY);
        roomPanel = new RoomPanel(this);
        cardPanel.add(roomPanel, CARD_ROOM);
        gamePanel = new GamePanel(this);
        cardPanel.add(gamePanel, CARD_GAME);
        endPanel = new EndPanel(this);
        cardPanel.add(endPanel, CARD_END);

        add(cardPanel);
        showCard(CARD_CONNECT);
    }
    
    public void leaveRoomAndGoToLobby() {
    	GameClient c = getClient();
    	if (c != null) {
    	    c.sendMessage(new Message(
    	        MessageType.LEAVE_ROOM, playerName, ""));
    	}

        currentRoomCode = null;
        myRole = "—";
        finalRoles.clear();
        gamePanel.resetForNewGame();

        showCard(CARD_LOBBY);
    }

    private JPanel makePlaceholder(String name) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Theme.background);

        JLabel title = new JLabel("Vampire Village", SwingConstants.CENTER);
        title.setFont(Theme.HEADING);
        title.setForeground(Theme.text);

        JLabel info = new JLabel(name + " panel (not built yet)", SwingConstants.CENTER);
        info.setFont(Theme.BODY);
        info.setForeground(Theme.textDim);

        p.add(title, BorderLayout.NORTH);
        p.add(info, BorderLayout.CENTER);
        return p;
    }

    public void showCard(String name) {
        currentCard = name;
        cardLayout.show(cardPanel, name);
        if (CARD_LOBBY.equals(name)) lobbyPanel.onShown();
        if (CARD_ROOM.equals(name))  roomPanel.onShown();
        if (CARD_GAME.equals(name))  gamePanel.onShown();
    }
    
    private void parseFinalRoles(String content) {
        finalRoles.clear();
        if (content.isEmpty()) return;
        for (String pair : content.split(",")) {
            String[] kv = pair.trim().split("=");
            if (kv.length == 2) {
                finalRoles.put(kv[0].trim(), kv[1].trim());
            }
        }
    }
    
    public GameClient getClient() {
        return gameClient;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String name) {
        this.playerName = name;
    }

    public String getCurrentRoomCode() {
        return currentRoomCode;
    }

    public void setCurrentRoomCode(String code) {
        this.currentRoomCode = code;
    }

    @Override
    public void onMessage(Message msg) {
        SwingUtilities.invokeLater(() -> handleMessage(msg));
    }

    private void handleMessage(Message msg) {
        if (msg.getType() == MessageType.ROLE_ASSIGN) {
            myRole = msg.getContent();
        }
        if (msg.getType() == MessageType.PLAYER_LIST) {
            roomPanel.handleMessage(msg);
        }

        switch (msg.getType()) {
            case ROOM_CREATED:
            case ROOM_JOINED:
            case ROOM_ERROR:
                lobbyPanel.handleMessage(msg);
                break;
            case SETTINGS_INFO:
                if (CARD_ROOM.equals(currentCard)) {
                    roomPanel.handleMessage(msg);
                }
                gamePanel.handleMessage(msg);
                break;
            case PHASE_CHANGE:
                if (CARD_ROOM.equals(currentCard)) showCard(CARD_GAME);
                gamePanel.handleMessage(msg);
                break;
            case ROLE_REVEAL:
                parseFinalRoles(msg.getContent());
                break;
            case GAME_OVER:
                gamePanel.handleMessage(msg);
                endPanel.configure(msg.getContent(), finalRoles, myRole);
                showCard(CARD_END);
                break;
            case ROLE_ASSIGN:
            case PLAYER_LIST:
            case CHAT:
            case SYSTEM:
            case DEATH:
            case INVESTIGATION_RESULT:
            case VOTE_RESULT:
                gamePanel.handleMessage(msg);
                break;
            default:
                System.out.println("[GUI] unhandled: " + msg);
        }
    }
}    