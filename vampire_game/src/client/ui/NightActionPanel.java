package client.ui;

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

public class NightActionPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel hintLabel;
    private JPanel buttonsRow;
    private boolean targetLocked = false;

    public NightActionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        hintLabel = new JLabel("Night");
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

    public void configure(String role, boolean isAlive, List<String> alivePlayers) {
        targetLocked = false;
        buttonsRow.removeAll();

        if (!isAlive) {
            hintLabel.setText("You are eliminated. The living act tonight.");
            buttonsRow.revalidate();
            buttonsRow.repaint();
            return;
        }

        String myName = mainFrame.getPlayerName();

        switch (role) {
            case "Vampire":
                hintLabel.setText("Choose tonight's victim:");
                addTargetButtons(alivePlayers, myName, true);
                break;
            case "Seer":
                hintLabel.setText("Choose someone to investigate:");
                addTargetButtons(alivePlayers, myName, true);
                break;
            case "Doctor":
                hintLabel.setText("Choose someone to protect (not yourself):");
                addTargetButtons(alivePlayers, myName, true);
                break;
            default:
                hintLabel.setText("Sleep tight — peasants have no night action.");
                break;
        }

        buttonsRow.revalidate();
        buttonsRow.repaint();
    }

    private void addTargetButtons(List<String> players, String myName, boolean excludeSelf) {
        for (String name : players) {
            if (excludeSelf && name.equals(myName)) continue;
            GothicButton btn = new GothicButton(name);
            btn.setPreferredSize(new Dimension(120, 32));
            btn.addActionListener(e -> chooseTarget(name));
            buttonsRow.add(btn);
        }
    }

    private void chooseTarget(String target) {
        if (targetLocked) return;
        targetLocked = true;
        for (Component c : buttonsRow.getComponents()) c.setEnabled(false);
        hintLabel.setText("Target locked: " + target);
        mainFrame.getClient().sendMessage(
            new Message(MessageType.NIGHT_ACTION, mainFrame.getPlayerName(), target));
    }
    
    public void applyTheme() {
        setBackground(Theme.background);
        hintLabel.setForeground(Theme.text);
        buttonsRow.setBackground(Theme.background);
        for (java.awt.Component c : buttonsRow.getComponents()) {
            if (c instanceof JButton) {
                JButton b = (JButton) c;
                b.setBackground(Theme.accent);
                b.setForeground(Theme.text);
            }
        }
    }
    
    
}