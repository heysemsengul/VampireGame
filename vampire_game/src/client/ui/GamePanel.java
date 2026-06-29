package client.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import javax.imageio.ImageIO;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import client.ui.components.GothicButton;
import client.ui.components.CircularTimer;
import client.ui.components.PlayerAvatar;
import client.ui.components.ChatPane;
import client.ui.components.RoleIcon;
import client.SoundManager;
import shared.GameSettings;
import shared.Message;
import shared.MessageType;
import shared.Phase;

public class GamePanel extends JPanel {
    private static final String ACTION_DEFAULT = "DEFAULT";
    private static final String ACTION_NIGHT   = "NIGHT";
    private static final String ACTION_VOTE    = "VOTE";

    private static final int THEME_FRAMES   = 20;
    private static final int THEME_FRAME_MS = 25;

    private MainFrame mainFrame;
    private JPanel headerBar;
    private JLabel phaseLabel;
    private CircularTimer circularTimer;
    private JPanel sidebar;
    private JLabel yourRoleHeader;
    private JLabel roleLabel;
    private JLabel playersHeader;
    private JPanel playerListPanel;
    private JPanel roleCard;
    private JPanel chatContainer;
    private JPanel inputRow;
    private ChatPane chatPane;
    private JTextField chatInput;
    private GothicButton sendButton;
    private GothicButton muteButton;
    private GothicButton leaveButton;
    private GothicButton skipButton;
    private JPanel actionPanel;
    private CardLayout actionCardLayout;
    private JPanel defaultCard;
    private JLabel actionHintLabel;
    private NightActionPanel nightActionPanel;
    private VotePanel votePanel;
    private RoleIcon roleIcon;

    private String myRole = "—";
    private Phase currentPhase = Phase.LOBBY;
    private boolean isAlive = true;
    private List<String> alivePlayers = new ArrayList<>();
    private Timer countdownTimer;
    private int remainingSeconds = 0;
    private int totalSeconds = 0;
    private Timer themeTimer;
    private int settingsDiscussion = GameSettings.DEFAULT_DISCUSSION;
    private int settingsVote       = GameSettings.DEFAULT_VOTE;
    private int settingsNight      = GameSettings.DEFAULT_NIGHT;
    private List<String> startingRoster = null;
    private BufferedImage dayBgImage;
    private BufferedImage nightBgImage;
    private int[] starX;
    private int[] starY;
    private int[] starSize;
    
    public GamePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(12, 12, 12, 12));

        add(buildHeader(),     BorderLayout.NORTH);
        add(buildSidebar(),    BorderLayout.WEST);
        add(buildChatArea(),   BorderLayout.CENTER);
        add(buildActionArea(), BorderLayout.SOUTH);
        
        dayBgImage   = loadImage("/assets/images/day.png");
        nightBgImage = loadImage("/assets/images/night.png");

        Random rng = new Random(42);
        int starCount = 50;
        starX = new int[starCount];
        starY = new int[starCount];
        starSize = new int[starCount];
        for (int i = 0; i < starCount; i++) {
            starX[i] = rng.nextInt(1000);
            starY[i] = rng.nextInt(700);
            starSize[i] = 1 + rng.nextInt(2);
        }
    }
    
    public void resetForNewGame() {
        myRole = "—";
        currentPhase = Phase.LOBBY;
        isAlive = true;
        alivePlayers = new ArrayList<>();
        startingRoster = null;

        if (countdownTimer != null) countdownTimer.stop();
        if (themeTimer != null) themeTimer.stop();

        phaseLabel.setText("Lobby");
        circularTimer.clear();
        roleLabel.setText("—");
        roleIcon.setRole("");
        chatPane.clear();
        chatInput.setEnabled(true);
        playerListPanel.removeAll();
        playerListPanel.revalidate();
        playerListPanel.repaint();

        Theme.setPalette(Theme.Palette.DAY);
        applyTheme();

        actionCardLayout.show(actionPanel, ACTION_DEFAULT);
        actionHintLabel.setText("Waiting…");
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        boolean night = (currentPhase == Phase.NIGHT);
        BufferedImage bg = night ? nightBgImage : dayBgImage;

        if (bg != null) {
            g2.drawImage(bg, 0, 0, w, h, null);
            g2.setColor(new Color(Theme.background.getRed(), Theme.background.getGreen(), Theme.background.getBlue(), 140));
            g2.fillRect(0, 0, w, h);
        } else {
            Color top    = shifted(Theme.background,  0.04f);
            Color bottom = shifted(Theme.background, -0.04f);
            g2.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
            g2.fillRect(0, 0, w, h);

            if (night) drawStars(g2, w, h);
        }

        g2.dispose();
    }
    
    private BufferedImage loadImage(String classpathPath) {
        try (InputStream is = GamePanel.class.getResourceAsStream(classpathPath)) {
            if (is == null) return null;
            return ImageIO.read(is);
        } catch (IOException e) {
            return null;
        }
    }

    private Color shifted(Color c, float delta) {
        int adj = Math.round(255 * delta);
        return new Color(clamp(c.getRed()   + adj), clamp(c.getGreen() + adj), clamp(c.getBlue()  + adj));
    }

    private int clamp(int v) { return Math.max(0, Math.min(255, v)); }

    private void drawStars(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(Theme.text.getRed(), Theme.text.getGreen(), Theme.text.getBlue(), 80));
        for (int i = 0; i < starX.length; i++) {
            int x = starX[i] % w;
            int y = starY[i] % h;
            int s = starSize[i];
            g2.fillOval(x, y, s, s);
        }
    }

    private JPanel buildHeader() {
        headerBar = new JPanel(new BorderLayout());
        headerBar.setBackground(Theme.panel);
        headerBar.setBorder(headerBorder());

        phaseLabel = new JLabel("Lobby", SwingConstants.LEFT);
        phaseLabel.setFont(Theme.HEADING);
        phaseLabel.setForeground(Theme.text);
        headerBar.add(phaseLabel, BorderLayout.WEST);

        circularTimer = new CircularTimer();
        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        rightSide.setOpaque(false);

        leaveButton = new GothicButton("Leave", GothicButton.Variant.DANGER);
        leaveButton.setPreferredSize(new Dimension(90, 30));
        leaveButton.addActionListener(e -> confirmAndLeave());
        rightSide.add(leaveButton);

        muteButton = new GothicButton(SoundManager.isMuted() ? "Sound: OFF" : "Sound: ON");
        muteButton.setPreferredSize(new Dimension(120, 30));
        muteButton.addActionListener(e -> {
            SoundManager.setMuted(!SoundManager.isMuted());
            muteButton.setText(SoundManager.isMuted() ? "Sound: OFF" : "Sound: ON");
        });
        rightSide.add(muteButton);

        rightSide.add(circularTimer);
        headerBar.add(rightSide, BorderLayout.EAST);

        return headerBar;
    }
    
    private void confirmAndLeave() {
        int result = JOptionPane.showConfirmDialog(this, "Leaving now will count as a forfeit and reveal your role.\nLeave anyway?", "Leave game?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (result == JOptionPane.YES_OPTION) {
            mainFrame.leaveRoomAndGoToLobby();
        }
    }

    private JPanel buildSidebar() {
        sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(Theme.panel);
        sidebar.setBorder(sidebarBorder());
        sidebar.setPreferredSize(new Dimension(220, 0));

        roleCard = new JPanel();
        roleCard.setLayout(new BoxLayout(roleCard, BoxLayout.Y_AXIS));
        roleCard.setBackground(Theme.panel);
        roleCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1), new EmptyBorder(16, 16, 20, 16)));
        roleCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        roleCard.setMaximumSize(new Dimension(200, 180));

        yourRoleHeader = new JLabel("YOUR ROLE");
        yourRoleHeader.setFont(Theme.BODY.deriveFont(11f));
        yourRoleHeader.setForeground(Theme.textDim);
        yourRoleHeader.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleCard.add(yourRoleHeader);

        roleCard.add(Box.createVerticalStrut(16));

        roleIcon = new RoleIcon(52);
        roleIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleCard.add(roleIcon);

        roleCard.add(Box.createVerticalStrut(12));

        roleLabel = new JLabel(myRole);
        roleLabel.setFont(Theme.HEADING.deriveFont(22f));
        roleLabel.setForeground(Theme.accent);
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        roleCard.add(roleLabel);

        sidebar.add(roleCard);

        sidebar.add(Box.createVerticalStrut(24));

        playersHeader = new JLabel("Players");
        playersHeader.setFont(Theme.BODY);
        playersHeader.setForeground(Theme.textDim);
        playersHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(playersHeader);

        sidebar.add(Box.createVerticalStrut(4));

        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(Theme.panel);
        playerListPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(playerListPanel);

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JPanel buildChatArea() {
        chatContainer = new JPanel(new BorderLayout());
        chatContainer.setBackground(Theme.background);
        chatContainer.setBorder(new EmptyBorder(0, 12, 0, 0));

        chatPane = new ChatPane();
        chatContainer.add(chatPane, BorderLayout.CENTER);

        inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setBackground(Theme.background);
        inputRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        chatInput = new JTextField();
        chatInput.setFont(Theme.BODY);
        chatInput.setBackground(Theme.panel);
        chatInput.setForeground(Theme.text);
        chatInput.setCaretColor(Theme.text);
        chatInput.setBorder(inputBorder());
        chatInput.addActionListener(e -> sendChat());
        inputRow.add(chatInput, BorderLayout.CENTER);

        sendButton = new GothicButton("Send");
        sendButton.addActionListener(e -> sendChat());
        inputRow.add(sendButton, BorderLayout.EAST);

        chatContainer.add(inputRow, BorderLayout.SOUTH);
        return chatContainer;
    }

    private JPanel buildActionArea() {
        actionCardLayout = new CardLayout();
        actionPanel = new JPanel(actionCardLayout);
        actionPanel.setBackground(Theme.background);
        actionPanel.setBorder(actionBorder());
        actionPanel.setPreferredSize(new Dimension(0, 100));

        defaultCard = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        defaultCard.setBackground(Theme.background);

        actionHintLabel = new JLabel("Actions will appear here based on phase and role");
        actionHintLabel.setFont(Theme.BODY.deriveFont(Font.ITALIC));
        actionHintLabel.setForeground(Theme.textDim);
        defaultCard.add(actionHintLabel);

        skipButton = new GothicButton("Skip to Vote");
        skipButton.setPreferredSize(new Dimension(140, 32));
        skipButton.setVisible(false);
        skipButton.addActionListener(e -> sendSkipPhase());
        defaultCard.add(skipButton);

        actionPanel.add(defaultCard, ACTION_DEFAULT);

        nightActionPanel = new NightActionPanel(mainFrame);
        actionPanel.add(nightActionPanel, ACTION_NIGHT);

        votePanel = new VotePanel(mainFrame);
        actionPanel.add(votePanel, ACTION_VOTE);

        return actionPanel;
    }
    
    private void sendSkipPhase() {
        skipButton.setEnabled(false);
        mainFrame.getClient().sendMessage(new Message(MessageType.SKIP_PHASE, mainFrame.getPlayerName(), ""));
    }
    
    private boolean iAmHost() {
        if (startingRoster == null || startingRoster.isEmpty()) return false;
        return startingRoster.get(0).equals(mainFrame.getPlayerName());
    }

    public void onShown() {
        chatInput.requestFocusInWindow();
    }

    public void handleMessage(Message msg) {
        switch (msg.getType()) {
        	case SETTINGS_INFO:
            	applySettings(msg.getContent());
            	break;
        	case ROLE_ASSIGN:
        	    myRole = msg.getContent();
        	    roleLabel.setText(myRole);
        	    roleIcon.setRole(myRole);
        	    chatPane.appendBare("You are the " + myRole + ".");
        	    break;
            case PHASE_CHANGE:
                handlePhaseChange(msg.getContent());
                break;
            
            case PLAYER_LIST:
                updatePlayerList(msg.getContent());
                break;
            case CHAT:
                chatPane.appendChat(msg.getSender(), msg.getContent());
                break;
            case SYSTEM:
                chatPane.appendSystem(msg.getContent());
                break;
            case DEATH:
                chatPane.appendDeath(msg.getContent());
                SoundManager.play("death");
                break;
            case VOTE_RESULT:
                chatPane.appendVoteResult(msg.getContent());
                break;
            case INVESTIGATION_RESULT:
                String c = msg.getContent();
                int colon = c.indexOf(':');
                if (colon > 0) {
                    String who = c.substring(0, colon);
                    String team = c.substring(colon + 1).toLowerCase();
                    chatPane.appendInvestigation("Investigation: " + who + " is on the " + team + " team.");
                } else {
                    chatPane.appendInvestigation(c);
                }
                break;
            case GAME_OVER:
                handleGameOver(msg.getContent());
                break;
            default:
                break;
        }
    }
    
    private void applySettings(String content) {
        try {
            String[] parts = content.split(",");
            settingsDiscussion = Integer.parseInt(parts[0].trim());
            settingsVote       = Integer.parseInt(parts[1].trim());
            settingsNight      = Integer.parseInt(parts[2].trim());
        } catch (Exception e) {
        	
        }
    }

    private void handlePhaseChange(String phaseName) {
    	 try {
    	        currentPhase = Phase.valueOf(phaseName);
    	    } catch (IllegalArgumentException ex) {
    	        return;
    	    }
    	    SoundManager.play("transition");
        try {
            currentPhase = Phase.valueOf(phaseName);
        } catch (IllegalArgumentException ex) {
            return;
        }
        if (startingRoster == null && currentPhase != Phase.LOBBY) {
            startingRoster = new ArrayList<>(alivePlayers);
        }
        phaseLabel.setText(prettyPhaseName(currentPhase));
        startCountdown(durationFor(currentPhase));
        chatPane.appendPhase(prettyPhaseName(currentPhase));

        Theme.Palette target = (currentPhase == Phase.NIGHT) ? Theme.Palette.NIGHT : Theme.Palette.DAY;
        if (target != Theme.current) {
            startThemeTransition(target);
        }

        reconfigureActionPanel();
    }

    private void startThemeTransition(Theme.Palette target) {
        if (themeTimer != null) themeTimer.stop();

        final Color fromBg     = Theme.background;
        final Color fromPanel  = Theme.panel;
        final Color fromText   = Theme.text;
        final Color fromTextD  = Theme.textDim;
        final Color fromAccent = Theme.accent;
        final Color fromDanger = Theme.danger;
        final Color fromBorder = Theme.border;

        final boolean toNight = target == Theme.Palette.NIGHT;
        final Color toBg     = toNight ? Theme.NIGHT_BACKGROUND : Theme.DAY_BACKGROUND;
        final Color toPanel  = toNight ? Theme.NIGHT_PANEL      : Theme.DAY_PANEL;
        final Color toText   = toNight ? Theme.NIGHT_TEXT       : Theme.DAY_TEXT;
        final Color toTextD  = toNight ? Theme.NIGHT_TEXT_DIM   : Theme.DAY_TEXT_DIM;
        final Color toAccent = toNight ? Theme.NIGHT_ACCENT     : Theme.DAY_ACCENT;
        final Color toDanger = toNight ? Theme.NIGHT_DANGER     : Theme.DAY_DANGER;
        final Color toBorder = toNight ? Theme.NIGHT_BORDER     : Theme.DAY_BORDER;

        Theme.current = target;

        final int[] frame = { 0 };
        themeTimer = new Timer(THEME_FRAME_MS, e -> {
            frame[0]++;
            float t = (float) frame[0] / THEME_FRAMES;
            if (t >= 1f) {
                t = 1f;
                themeTimer.stop();
            }
            Theme.background = lerp(fromBg,     toBg,     t);
            Theme.panel      = lerp(fromPanel,  toPanel,  t);
            Theme.text       = lerp(fromText,   toText,   t);
            Theme.textDim    = lerp(fromTextD,  toTextD,  t);
            Theme.accent     = lerp(fromAccent, toAccent, t);
            Theme.danger     = lerp(fromDanger, toDanger, t);
            Theme.border     = lerp(fromBorder, toBorder, t);
            applyTheme();
        });
        themeTimer.start();
    }

    private static Color lerp(Color a, Color b, float t) {
        int r  = Math.round(a.getRed()   * (1 - t) + b.getRed()   * t);
        int g  = Math.round(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = Math.round(a.getBlue()  * (1 - t) + b.getBlue()  * t);
        return new Color(r, g, bl);
    }

    private void applyTheme() {
        setBackground(Theme.background);

        headerBar.setBackground(Theme.panel);
        headerBar.setBorder(headerBorder());
        phaseLabel.setForeground(Theme.text);

        sidebar.setBackground(Theme.panel);
        sidebar.setBorder(sidebarBorder());
        yourRoleHeader.setForeground(Theme.textDim);
        roleLabel.setForeground(isAlive ? Theme.accent : Theme.textDim);
        playersHeader.setForeground(Theme.textDim);
        playerListPanel.setBackground(Theme.panel);
        
        chatContainer.setBackground(Theme.background);
        chatPane.applyTheme();
        inputRow.setBackground(Theme.background);
        chatInput.setBackground(Theme.panel);
        chatInput.setForeground(Theme.text);
        chatInput.setCaretColor(Theme.text);
        chatInput.setBorder(inputBorder());
        sendButton.setBackground(Theme.accent);
        sendButton.setForeground(Theme.text);
        
        roleCard.setBackground(Theme.panel);
        roleCard.setBorder(BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1),new EmptyBorder(16, 16, 20, 16)));

        actionPanel.setBackground(Theme.background);
        actionPanel.setBorder(actionBorder());
        defaultCard.setBackground(Theme.background);
        actionHintLabel.setForeground(Theme.textDim);

        nightActionPanel.applyTheme();
        votePanel.applyTheme();

        repaint();
    }
    
    private Border headerBorder() {
        return BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1), new EmptyBorder(12, 20, 12, 20));
    }

    private Border sidebarBorder() {
        return BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1), new EmptyBorder(16, 16, 16, 16));
    }

    private Border inputBorder() {
        return BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1), new EmptyBorder(6, 8, 6, 8));
    }

    private Border actionBorder() {
        return BorderFactory.createCompoundBorder(new LineBorder(Theme.border, 1), new EmptyBorder(12, 12, 12, 12));
    }
    
    private void reconfigureActionPanel() {
        switch (currentPhase) {
            case NIGHT:
                nightActionPanel.configure(myRole, isAlive, alivePlayers);
                actionCardLayout.show(actionPanel, ACTION_NIGHT);
                break;
            case VOTE:
                votePanel.configure(isAlive, alivePlayers);
                actionCardLayout.show(actionPanel, ACTION_VOTE);
                break;
            default:
                actionHintLabel.setText(defaultHintFor(currentPhase));
                boolean showSkip = (currentPhase == Phase.DISCUSSION) && iAmHost();
                skipButton.setVisible(showSkip);
                skipButton.setEnabled(true);
                actionCardLayout.show(actionPanel, ACTION_DEFAULT);
                break;
        }
    }

    private String defaultHintFor(Phase p) {
        switch (p) {
            case DAY_REVEAL: return "Dawn breaks. The fate of the night is revealed.";
            case DISCUSSION: return "Discuss freely in chat.";
            case GAME_OVER:  return "Game over.";
            default:         return "Waiting…";
        }
    }

    private String prettyPhaseName(Phase p) {
        switch (p) {
            case NIGHT:      return "Night";
            case DAY_REVEAL: return "Dawn";
            case DISCUSSION: return "Day — Discussion";
            case VOTE:       return "Day — Vote";
            case GAME_OVER:  return "Game over";
            default:         return p.name();
        }
    }

    private int durationFor(Phase p) {
        switch (p) {
            case NIGHT:      return settingsNight;
            case DAY_REVEAL: return 5;
            case DISCUSSION: return settingsDiscussion;
            case VOTE:       return settingsVote;
            default:         return 0;
        }
    }

    private void startCountdown(int seconds) {
        if (countdownTimer != null) countdownTimer.stop();
        remainingSeconds = seconds;
        totalSeconds = seconds;
        circularTimer.setTime(remainingSeconds, totalSeconds);
        if (seconds <= 0) return;
        countdownTimer = new Timer(1000, e -> {
            remainingSeconds--;
            if (remainingSeconds < 0) {
                remainingSeconds = 0;
                countdownTimer.stop();
            }
            circularTimer.setTime(remainingSeconds, totalSeconds);
        });
        countdownTimer.start();
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void updatePlayerList(String content) {
        alivePlayers = new ArrayList<>();
        if (!content.isEmpty()) {
            for (String n : content.split(",")) alivePlayers.add(n.trim());
        }
        isAlive = alivePlayers.contains(mainFrame.getPlayerName());

        List<String> displayList = (startingRoster != null) ? startingRoster : alivePlayers;

        playerListPanel.removeAll();
        for (String n : displayList) {
            boolean isMe = n.equals(mainFrame.getPlayerName());
            boolean alive = alivePlayers.contains(n);
            PlayerAvatar avatar = new PlayerAvatar();
            avatar.setData(n, alive, isMe);
            avatar.setAlignmentX(Component.LEFT_ALIGNMENT);
            playerListPanel.add(avatar);
        }
        playerListPanel.revalidate();
        playerListPanel.repaint();

        roleLabel.setText(isAlive ? myRole : myRole + "  (dead)");
        reconfigureActionPanel();
    }

    private void sendChat() {
        String text = chatInput.getText().trim();
        if (text.isEmpty()) return;
        mainFrame.getClient().sendMessage(new Message(MessageType.CHAT, mainFrame.getPlayerName(), text));
        chatInput.setText("");
    }

    private void handleGameOver(String winningTeam) {
    	SoundManager.play("game_over");
        if (countdownTimer != null) countdownTimer.stop();
        circularTimer.clear();
        phaseLabel.setText("Game over");
        chatInput.setEnabled(false);
        currentPhase = Phase.GAME_OVER;
        reconfigureActionPanel();
    }
}