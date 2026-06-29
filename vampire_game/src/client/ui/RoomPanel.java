package client.ui;

import java.util.prefs.Preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import client.ui.components.GothicButton;
import shared.GameSettings;
import shared.Message;
import shared.MessageType;

public class RoomPanel extends JPanel {
	private static final Preferences PREFS = Preferences.userNodeForPackage(RoomPanel.class);
	
	
    private MainFrame mainFrame;
    private JLabel codeLabel;
    private JLabel countLabel;
    private JPanel playerListPanel;
    private JSlider discussionSlider;
    private JLabel  discussionValueLabel;
    private JSlider voteSlider;
    private JLabel  voteValueLabel;
    private JSlider nightSlider;
    private JLabel  nightValueLabel;
    private GothicButton startButton;
    private JLabel statusLabel;
    private JComboBox<Integer> playerCountBox;
    private int requiredPlayers = GameSettings.DEFAULT_PLAYER_COUNT;
    private boolean updatingFromServer = false;

    private String hostName;
    
    private static final String PREF_DISCUSSION = "discussion";
    private static final String PREF_VOTE       = "vote";
    private static final String PREF_NIGHT      = "night";
    private static final String PREF_PLAYERS    = "players";

    public RoomPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(32, 64, 32, 64));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.background);

        JLabel title = new JLabel("Room Code");
        title.setFont(Theme.BODY);
        title.setForeground(Theme.textDim);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);

        codeLabel = new JLabel("----");
        codeLabel.setFont(new Font("Serif", Font.BOLD, 56));
        codeLabel.setForeground(Theme.accent);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        codeLabel.setBorder(new EmptyBorder(8, 16, 8, 16));
        header.add(codeLabel);

        JLabel hint = new JLabel("Share this code with friends");
        hint.setFont(Theme.BODY.deriveFont(Font.ITALIC, 12f));
        hint.setForeground(Theme.textDim);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(hint);

        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.background);
        center.setBorder(new EmptyBorder(24, 0, 24, 0));

        countLabel = new JLabel("Players (0/" + requiredPlayers + ")");
        countLabel.setFont(Theme.SUBHEADING);
        countLabel.setForeground(Theme.text);
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(countLabel);

        center.add(Box.createVerticalStrut(12));

        playerListPanel = new JPanel();
        playerListPanel.setLayout(new BoxLayout(playerListPanel, BoxLayout.Y_AXIS));
        playerListPanel.setBackground(Theme.panel);

        JScrollPane scroll = new JScrollPane(playerListPanel);
        scroll.setMaximumSize(new Dimension(420, 240));
        scroll.setPreferredSize(new Dimension(420, 240));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.getViewport().setBackground(Theme.panel);
        scroll.setBorder(new LineBorder(Theme.border, 1));
        center.add(scroll);
        
        center.add(Box.createVerticalStrut(20));
        center.add(buildSettingsSection());

        return center;
    }
    
    private JPanel buildSettingsSection() {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Theme.background);
        section.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("Game settings");
        title.setFont(Theme.SUBHEADING);
        title.setForeground(Theme.text);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        section.add(title);
        section.add(Box.createVerticalStrut(12));
        section.add(buildPlayerCountRow());

        int savedDiscussion = clampInt(
        	    PREFS.getInt(PREF_DISCUSSION, GameSettings.DEFAULT_DISCUSSION),
        	    GameSettings.MIN_DISCUSSION, GameSettings.MAX_DISCUSSION);
        	discussionSlider = makeTimerSlider(
        	    GameSettings.MIN_DISCUSSION, GameSettings.MAX_DISCUSSION, savedDiscussion);
        	discussionValueLabel = makeValueLabel(savedDiscussion);
        discussionSlider.addChangeListener(e ->
            handleSliderChange(discussionSlider, discussionValueLabel));
        section.add(makeSliderRow("Discussion:", discussionSlider, discussionValueLabel));

        int savedVote = clampInt(
        	    PREFS.getInt(PREF_VOTE, GameSettings.DEFAULT_VOTE),
        	    GameSettings.MIN_VOTE, GameSettings.MAX_VOTE);
        	voteSlider = makeTimerSlider(
        	    GameSettings.MIN_VOTE, GameSettings.MAX_VOTE, savedVote);
        	voteValueLabel = makeValueLabel(savedVote);
        voteSlider.addChangeListener(e ->
            handleSliderChange(voteSlider, voteValueLabel));
        section.add(makeSliderRow("Vote:", voteSlider, voteValueLabel));

        int savedNight = clampInt(
        	    PREFS.getInt(PREF_NIGHT, GameSettings.DEFAULT_NIGHT),
        	    GameSettings.MIN_NIGHT, GameSettings.MAX_NIGHT);
        	nightSlider = makeTimerSlider(
        	    GameSettings.MIN_NIGHT, GameSettings.MAX_NIGHT, savedNight);
        	nightValueLabel = makeValueLabel(savedNight);
        nightSlider.addChangeListener(e ->
            handleSliderChange(nightSlider, nightValueLabel));
        section.add(makeSliderRow("Night:", nightSlider, nightValueLabel));

        return section;
    }
    
    private static int clampInt(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    private static boolean isValidPlayerCount(int n) {
        return n == 5 || n == 7 || n == 8 || n == 9 || n == 10 || n == 11;
    }

    private JPanel buildPlayerCountRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        row.setBackground(Theme.background);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel("Players needed:");
        label.setFont(Theme.BODY);
        label.setForeground(Theme.textDim);
        label.setPreferredSize(new Dimension(120, 24));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(label);

        Integer[] options = {5, 7, 8, 9, 10, 11};
        playerCountBox = new JComboBox<>(options);
        playerCountBox.setFont(Theme.BODY);
        int savedPlayers = PREFS.getInt(PREF_PLAYERS, GameSettings.DEFAULT_PLAYER_COUNT);
        if (isValidPlayerCount(savedPlayers)) {
            playerCountBox.setSelectedItem(savedPlayers);
        } else {
            playerCountBox.setSelectedItem(GameSettings.DEFAULT_PLAYER_COUNT);
        }
        playerCountBox.addActionListener(e -> {
            if (updatingFromServer) return;
            sendSettingsUpdate();
        });
        row.add(playerCountBox);
        return row;
    }

    private JSlider makeTimerSlider(int min, int max, int initial) {
        JSlider s = new JSlider(min, max, initial);
        s.setBackground(Theme.background);
        s.setOpaque(false);
        s.setPreferredSize(new Dimension(220, 24));
        return s;
    }

    private JLabel makeValueLabel(int initialValue) {
        JLabel l = new JLabel(initialValue + "s");
        l.setFont(Theme.BODY);
        l.setForeground(Theme.accent);
        l.setPreferredSize(new Dimension(40, 24));
        return l;
    }

    private JPanel makeSliderRow(String labelText, JSlider slider, JLabel valueLabel) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 4));
        row.setBackground(Theme.background);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setFont(Theme.BODY);
        label.setForeground(Theme.textDim);
        label.setPreferredSize(new Dimension(120, 24));
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        row.add(label);

        row.add(slider);
        row.add(valueLabel);
        return row;
    }

    private void handleSliderChange(JSlider slider, JLabel valueLabel) {
        valueLabel.setText(slider.getValue() + "s");
        if (!slider.getValueIsAdjusting() && !updatingFromServer) {
            sendSettingsUpdate();
        }
    }

    private void sendSettingsUpdate() {
        int players    = (Integer) playerCountBox.getSelectedItem();
        int discussion = discussionSlider.getValue();
        int vote       = voteSlider.getValue();
        int night      = nightSlider.getValue();

        PREFS.putInt(PREF_PLAYERS,    players);
        PREFS.putInt(PREF_DISCUSSION, discussion);
        PREFS.putInt(PREF_VOTE,       vote);
        PREFS.putInt(PREF_NIGHT,      night);

        String content = discussion + "," + vote + "," + night + "," + players;
        mainFrame.getClient().sendMessage(new Message(MessageType.UPDATE_SETTINGS, mainFrame.getPlayerName(), content));
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        footer.setBackground(Theme.background);

        startButton = new GothicButton("Start Game");
        startButton.setFont(Theme.BUTTON);
        startButton.setBackground(Theme.accent);
        startButton.setForeground(Theme.text);
        startButton.setFocusPainted(false);
        startButton.setBorderPainted(false);
        startButton.setMaximumSize(new Dimension(200, 44));
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.setEnabled(false);
        startButton.addActionListener(e -> doStart());
        footer.add(startButton);

        footer.add(Box.createVerticalStrut(12));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.BODY.deriveFont(Font.ITALIC));
        statusLabel.setForeground(Theme.textDim);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.add(statusLabel);

        return footer;
    }

    public void onShown() {
        codeLabel.setText(mainFrame.getCurrentRoomCode());
        statusLabel.setText("Waiting for players…");
        startButton.setEnabled(false);
        playerListPanel.removeAll();
        playerListPanel.revalidate();
        playerListPanel.repaint();
    }

    public void handleMessage(Message msg) {
        switch (msg.getType()) {
            case PLAYER_LIST:
                updatePlayerList(msg.getContent());
                break;
            case SETTINGS_INFO:
                applySettings(msg.getContent());
                break;
            case SYSTEM:
                break;
            default:
                break;
        }
    }

    private void applySettings(String content) {
        try {
            String[] parts = content.split(",");
            int discussion = Integer.parseInt(parts[0].trim());
            int vote       = Integer.parseInt(parts[1].trim());
            int night      = Integer.parseInt(parts[2].trim());
            int playerCount = Integer.parseInt(parts[3].trim());
            requiredPlayers = playerCount;

            updatingFromServer = true;
            playerCountBox.setSelectedItem(playerCount);
            discussionSlider.setValue(discussion);
            voteSlider.setValue(vote);
            nightSlider.setValue(night);
            discussionValueLabel.setText(discussion + "s");
            voteValueLabel.setText(vote + "s");
            nightValueLabel.setText(night + "s");
            updatingFromServer = false;
        } catch (Exception e) {
        	
        }
    }

    private void updatePlayerList(String content) {
        String[] names = content.isEmpty() ? new String[0] : content.split(",");
        hostName = names.length > 0 ? names[0].trim() : null;

        playerListPanel.removeAll();
        for (int i = 0; i < names.length; i++) {
            String name = names[i].trim();
            String label = "   " + name + (i == 0 ? "   (host)" : "");
            JLabel row = new JLabel(label);
            row.setFont(Theme.BODY);
            row.setForeground(Theme.text);
            row.setOpaque(true);
            row.setBackground(i % 2 == 0 ? Theme.panel : Theme.background);
            row.setBorder(new EmptyBorder(10, 8, 10, 8));
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            playerListPanel.add(row);
        }
        playerListPanel.revalidate();
        playerListPanel.repaint();

        int count = names.length;
        countLabel.setText("Players (" + count + "/" + requiredPlayers + ")");

        boolean iAmHost = mainFrame.getPlayerName().equals(hostName);
        boolean enoughForMinimum = count >= requiredPlayers;
        playerCountBox.setEnabled(iAmHost);
        discussionSlider.setEnabled(iAmHost);
        voteSlider.setEnabled(iAmHost);
        nightSlider.setEnabled(iAmHost);

        if (iAmHost) {
            startButton.setEnabled(enoughForMinimum);
            if (!enoughForMinimum) {
            	statusLabel.setText("Need " + (requiredPlayers - count) + " more player(s)");
            } else {
                statusLabel.setText("Click Start when ready");
            }
        } else {
            startButton.setEnabled(false);
            statusLabel.setText("Waiting for " + hostName + " to start…");
        }
    }

    private void doStart() {
        startButton.setEnabled(false);
        statusLabel.setText("Starting…");
        mainFrame.getClient().sendMessage(
            new Message(MessageType.START_GAME, mainFrame.getPlayerName(), ""));
    }
}