package client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import client.ui.components.GothicButton;
import client.ui.components.RoleIcon;

public class EndPanel extends JPanel {

    private MainFrame mainFrame;
    private JLabel winnerLabel;
    private JLabel subtitleLabel;
    private JPanel rolesPanel;

    public EndPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.background);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(48, 64, 48, 64));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(Theme.background);

        winnerLabel = new JLabel("—");
        winnerLabel.setFont(new Font("Serif", Font.BOLD, 48));
        winnerLabel.setForeground(Theme.text);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(winnerLabel);

        header.add(Box.createVerticalStrut(8));

        subtitleLabel = new JLabel(" ");
        subtitleLabel.setFont(Theme.SUBHEADING.deriveFont(Font.ITALIC, 20f));
        subtitleLabel.setForeground(Theme.accent);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitleLabel);

        return header;
    }

    private JPanel buildCenter() {
        JPanel center = new JPanel();
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBackground(Theme.background);
        center.setBorder(new EmptyBorder(32, 0, 32, 0));

        JLabel header = new JLabel("Final roles");
        header.setFont(Theme.BODY);
        header.setForeground(Theme.textDim);
        header.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(header);

        center.add(Box.createVerticalStrut(8));

        rolesPanel = new JPanel();
        rolesPanel.setLayout(new BoxLayout(rolesPanel, BoxLayout.Y_AXIS));
        rolesPanel.setBackground(Theme.panel);

        JScrollPane scroll = new JScrollPane(rolesPanel);
        scroll.setMaximumSize(new Dimension(420, 240));
        scroll.setPreferredSize(new Dimension(420, 240));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        scroll.getViewport().setBackground(Theme.panel);
        scroll.setBorder(new LineBorder(Theme.border, 1));
        center.add(scroll);

        return center;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        footer.setBackground(Theme.background);

        GothicButton backButton = new GothicButton("Back to Lobby");
        backButton.setPreferredSize(new Dimension(180, 40));
        backButton.addActionListener(e -> mainFrame.leaveRoomAndGoToLobby());
        footer.add(backButton);

        GothicButton quitButton = new GothicButton("Quit");
        quitButton.setPreferredSize(new Dimension(120, 40));
        quitButton.addActionListener(e -> System.exit(0));
        footer.add(quitButton);

        return footer;
    }

    public void configure(String winningTeam, Map<String, String> finalRoles, String myRole) {
        boolean iWon = didMyTeamWin(winningTeam, myRole);

        winnerLabel.setText(prettyTeam(winningTeam) + " wins");
        subtitleLabel.setText(iWon ? "Victory!" : "Defeat.");
        subtitleLabel.setForeground(iWon ? Theme.accent : Theme.danger);

        rolesPanel.removeAll();
        String myName = mainFrame.getPlayerName();
        int i = 0;
        for (Map.Entry<String, String> entry : finalRoles.entrySet()) {
            JPanel row = buildRoleRow(entry.getKey(), entry.getValue(), entry.getKey().equals(myName), i % 2 == 0);
            rolesPanel.add(row);
            i++;
        }
        rolesPanel.revalidate();
        rolesPanel.repaint();
    }

    private JPanel buildRoleRow(String name, String role, boolean isMe, boolean shaded) {
        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(shaded ? Theme.panel : Theme.background);
        row.setBorder(new EmptyBorder(8, 16, 8, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(name + (isMe ? "  (you)" : ""));
        nameLabel.setFont(Theme.BODY);
        nameLabel.setForeground(Theme.text);
        row.add(nameLabel, BorderLayout.WEST);

        JPanel rightSide = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightSide.setOpaque(false);

        RoleIcon icon = new RoleIcon(22);
        icon.setRole(role);
        rightSide.add(icon);

        JLabel roleLabel = new JLabel(role);
        roleLabel.setFont(Theme.BODY);
        roleLabel.setForeground("Vampire".equals(role) ? Theme.danger : Theme.text);
        rightSide.add(roleLabel);

        row.add(rightSide, BorderLayout.EAST);

        return row;
    }

    private boolean didMyTeamWin(String winningTeam, String myRole) {
        boolean iAmVampire = "Vampire".equals(myRole);
        boolean vampireSideWon = "VAMPIRE".equals(winningTeam);
        return iAmVampire == vampireSideWon;
    }

    private String prettyTeam(String team) {
        if ("VAMPIRE".equals(team)) return "Vampires";
        if ("VILLAGE".equals(team)) return "Village";
        return team;
    }
}