package client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import shared.Message;
import shared.MessageType;
import client.ui.components.GothicButton;

public class LobbyPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel welcomeLabel;
    private GothicButton createButton;
    private JTextField codeField;
    private GothicButton joinButton;
    private JLabel statusLabel;

    public LobbyPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BorderLayout());

        Box centerBox = Box.createVerticalBox();
        centerBox.add(Box.createVerticalGlue());

        welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(Theme.HEADING);
        welcomeLabel.setForeground(Theme.text);
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(welcomeLabel);

        centerBox.add(Box.createVerticalStrut(48));

        createButton = new GothicButton("Create New Room");
        createButton.setFont(Theme.BUTTON);
        createButton.setBackground(Theme.accent);
        createButton.setForeground(Theme.text);
        createButton.setFocusPainted(false);
        createButton.setBorderPainted(false);
        createButton.setMaximumSize(new Dimension(240, 44));
        createButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        createButton.addActionListener(e -> doCreateRoom());
        centerBox.add(createButton);

        centerBox.add(Box.createVerticalStrut(32));

        JLabel orLabel = new JLabel("— or —");
        orLabel.setFont(Theme.BODY.deriveFont(Font.ITALIC));
        orLabel.setForeground(Theme.textDim);
        orLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(orLabel);

        centerBox.add(Box.createVerticalStrut(32));

        JLabel codeLabel = new JLabel("Enter room code");
        codeLabel.setFont(Theme.BODY);
        codeLabel.setForeground(Theme.text);
        codeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(codeLabel);

        centerBox.add(Box.createVerticalStrut(8));

        codeField = new JTextField();
        codeField.setMaximumSize(new Dimension(160, 36));
        codeField.setFont(Theme.HEADING.deriveFont(18f));
        codeField.setBackground(Theme.panel);
        codeField.setForeground(Theme.text);
        codeField.setCaretColor(Theme.text);
        codeField.setHorizontalAlignment(JTextField.CENTER);
        codeField.setAlignmentX(Component.CENTER_ALIGNMENT);
        codeField.addActionListener(e -> doJoinRoom());
        centerBox.add(codeField);

        centerBox.add(Box.createVerticalStrut(16));

        joinButton = new GothicButton("Join");
        joinButton.setFont(Theme.BUTTON);
        joinButton.setBackground(Theme.accent);
        joinButton.setForeground(Theme.text);
        joinButton.setFocusPainted(false);
        joinButton.setBorderPainted(false);
        joinButton.setMaximumSize(new Dimension(160, 40));
        joinButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinButton.addActionListener(e -> doJoinRoom());
        centerBox.add(joinButton);

        centerBox.add(Box.createVerticalStrut(24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.danger);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(statusLabel);

        centerBox.add(Box.createVerticalGlue());

        add(centerBox, BorderLayout.CENTER);
    }

    public void onShown() {
        welcomeLabel.setText("Welcome, " + mainFrame.getPlayerName());
        statusLabel.setText(" ");
        codeField.setText("");
        setControlsEnabled(true);
    }

    private void doCreateRoom() {
        setControlsEnabled(false);
        statusLabel.setForeground(Theme.textDim);
        statusLabel.setText("Creating room…");
        Message m = new Message(MessageType.CREATE_ROOM, mainFrame.getPlayerName(), "");
        mainFrame.getClient().sendMessage(m);
    }

    private void doJoinRoom() {
        String code = codeField.getText().trim().toUpperCase();
        if (code.isEmpty()) {
            statusLabel.setForeground(Theme.danger);
            statusLabel.setText("Please enter a room code.");
            return;
        }
        setControlsEnabled(false);
        statusLabel.setForeground(Theme.textDim);
        statusLabel.setText("Joining…");
        Message m = new Message(MessageType.JOIN_ROOM, mainFrame.getPlayerName(), code);
        mainFrame.getClient().sendMessage(m);
    }

    public void handleMessage(Message msg) {
        switch (msg.getType()) {
            case ROOM_CREATED:
            case ROOM_JOINED:
                mainFrame.setCurrentRoomCode(msg.getContent());
                mainFrame.showCard(MainFrame.CARD_ROOM);
                break;
            case ROOM_ERROR:
                statusLabel.setForeground(Theme.danger);
                statusLabel.setText(msg.getContent());
                setControlsEnabled(true);
                break;
            default:
                break;
        }
    }

    private void setControlsEnabled(boolean enabled) {
        createButton.setEnabled(enabled);
        joinButton.setEnabled(enabled);
        codeField.setEnabled(enabled);
    }
}