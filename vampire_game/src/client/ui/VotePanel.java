package client.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import client.ui.components.GothicButton;
import shared.Message;
import shared.MessageType;

public class VotePanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel hintLabel;
    private JPanel buttonsRow;
    private boolean voted = false;

    public VotePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        hintLabel = new JLabel("Vote");
        hintLabel.setFont(Theme.BODY);
        hintLabel.setForeground(Theme.text);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(hintLabel);

        add(Box.createVerticalStrut(8));

        buttonsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonsRow.setBackground(Theme.background);
        buttonsRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(buttonsRow);
    }

    public void configure(boolean isAlive, List<String> alivePlayers) {
        voted = false;
        buttonsRow.removeAll();

        if (!isAlive) {
            hintLabel.setText("Dead players cannot vote.");
            buttonsRow.revalidate();
            buttonsRow.repaint();
            return;
        }

        hintLabel.setText("Vote to eliminate:");
        String myName = mainFrame.getPlayerName();
        for (String name : alivePlayers) {
            if (name.equals(myName)) continue;
            GothicButton btn = new GothicButton(name, GothicButton.Variant.DANGER);
            btn.setPreferredSize(new Dimension(120, 32));
            btn.addActionListener(e -> voteFor(name));
            buttonsRow.add(btn);
            buttonsRow.add(btn);
        }

        buttonsRow.revalidate();
        buttonsRow.repaint();
    }

    private void voteFor(String target) {
        if (voted) return;
        voted = true;
        for (Component c : buttonsRow.getComponents()) c.setEnabled(false);
        hintLabel.setText("Voted for " + target);
        mainFrame.getClient().sendMessage(
            new Message(MessageType.VOTE, mainFrame.getPlayerName(), target));
    }
    
    public void applyTheme() {
        setBackground(Theme.background);
        hintLabel.setForeground(Theme.text);
        buttonsRow.setBackground(Theme.background);
        for (java.awt.Component c : buttonsRow.getComponents()) {
            if (c instanceof JButton) {
                c.setBackground(Theme.danger);
            }
        }
    }
    
    
}