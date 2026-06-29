package client.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import client.ui.Theme;

public class PlayerAvatar extends JComponent {

    private String name = "";
    private boolean alive = true;
    private boolean isMe = false;

    public PlayerAvatar() {
        setPreferredSize(new Dimension(200, 44));
    }

    public void setData(String name, boolean alive, boolean isMe) {
        this.name = name == null ? "" : name;
        this.alive = alive;
        this.isMe = isMe;
        repaint();
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int h = getHeight();
        int circleSize = h - 10;
        int cx = 4;
        int cy = (h - circleSize) / 2;

        Color fillColor, ringColor, initialColor, nameColor;
        if (!alive) {
            fillColor = Theme.background;
            ringColor = Theme.textDim;
            initialColor = Theme.textDim;
            nameColor = Theme.textDim;
        } else if (isMe) {
            fillColor = Theme.accent;
            ringColor = Theme.accent;
            initialColor = Theme.text;
            nameColor = Theme.accent;
        } else {
            fillColor = Theme.background;
            ringColor = Theme.border;
            initialColor = Theme.text;
            nameColor = Theme.text;
        }

        g2.setColor(fillColor);
        g2.fillOval(cx, cy, circleSize, circleSize);
        g2.setColor(ringColor);
        g2.setStroke(new BasicStroke(isMe ? 1.5f : 1f));
        g2.drawOval(cx, cy, circleSize, circleSize);

        if (!name.isEmpty()) {
            String initial = name.substring(0, 1).toUpperCase();
            g2.setFont(Theme.SUBHEADING);
            g2.setColor(initialColor);
            FontMetrics fm = g2.getFontMetrics();
            int textX = cx + (circleSize - fm.stringWidth(initial)) / 2;
            int textY = cy + (circleSize + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(initial, textX, textY);
        }

        g2.setFont(Theme.BODY);
        g2.setColor(nameColor);
        FontMetrics fmName = g2.getFontMetrics();
        String displayName = name + (isMe ? "  (you)" : "");
        int nameX = cx + circleSize + 12;
        int nameY = (h + fmName.getAscent() - fmName.getDescent()) / 2;
        g2.drawString(displayName, nameX, nameY);

        g2.dispose();
    }
}