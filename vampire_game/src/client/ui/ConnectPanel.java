package client.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import client.GameClient;
import client.ui.components.GothicButton;

public class ConnectPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField nameField;
    private GothicButton connectButton;
    private JLabel statusLabel;

    public ConnectPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BorderLayout());

        Box centerBox = Box.createVerticalBox();
        centerBox.add(Box.createVerticalGlue());

        JLabel title = new JLabel("Vampire Village");
        title.setFont(Theme.HEADING.deriveFont(40f));
        title.setForeground(Theme.text);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(title);

        JLabel subtitle = new JLabel("A game of deception by candlelight");
        subtitle.setFont(Theme.BODY.deriveFont(Font.ITALIC, 14f));
        subtitle.setForeground(Theme.textDim);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(Box.createVerticalStrut(8));
        centerBox.add(subtitle);

        centerBox.add(Box.createVerticalStrut(48));

        JLabel nameLabel = new JLabel("Your name");
        nameLabel.setFont(Theme.BODY);
        nameLabel.setForeground(Theme.text);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(nameLabel);

        nameField = new JTextField();
        nameField.setMaximumSize(new Dimension(280, 36));
        nameField.setFont(Theme.BODY);
        nameField.setBackground(Theme.panel);
        nameField.setForeground(Theme.text);
        nameField.setCaretColor(Theme.text);
        nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameField.addActionListener(e -> tryConnect());
        centerBox.add(Box.createVerticalStrut(8));
        centerBox.add(nameField);

        centerBox.add(Box.createVerticalStrut(24));

        connectButton = new GothicButton("Connect");
        connectButton.setMaximumSize(new Dimension(160, 40));
        connectButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectButton.addActionListener(e -> tryConnect());
        centerBox.add(connectButton);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(Theme.BODY);
        statusLabel.setForeground(Theme.textDim);
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerBox.add(Box.createVerticalStrut(20));
        centerBox.add(statusLabel);

        centerBox.add(Box.createVerticalGlue());

        add(centerBox, BorderLayout.CENTER);
    }

    private void tryConnect() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            statusLabel.setText("Please enter a name.");
            statusLabel.setForeground(Theme.danger);
            return;
        }

        connectButton.setEnabled(false);
        statusLabel.setText("Connecting…");
        statusLabel.setForeground(Theme.textDim);

        new Thread(() -> {
            try {
                GameClient client = mainFrame.getClient();
                client.connect();
                client.start();

                SwingUtilities.invokeLater(() -> {
                    mainFrame.setPlayerName(name);
                    mainFrame.showCard(MainFrame.CARD_LOBBY);
                });
            } catch (IOException ex) {
                SwingUtilities.invokeLater(() -> {
                    statusLabel.setText("Couldn't connect: " + ex.getMessage());
                    statusLabel.setForeground(Theme.danger);
                    connectButton.setEnabled(true);
                });
            }
        }).start();
    }
}