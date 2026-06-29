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

public class CircularTimer extends JComponent {

    private int remainingSeconds = 0;
    private int totalSeconds = 0;

    public CircularTimer() {
        setPreferredSize(new Dimension(64, 64));
        setOpaque(false);
    }

    public void setTime(int remaining, int total) {
        this.remainingSeconds = Math.max(0, remaining);
        this.totalSeconds = Math.max(1, total);
        repaint();
    }

    public void clear() {
        this.remainingSeconds = 0;
        this.totalSeconds = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int size = Math.min(w, h) - 8;
        int x = (w - size) / 2;
        int y = (h - size) / 2;

        g2.setColor(Theme.border);
        g2.setStroke(new BasicStroke(3f));
        g2.drawArc(x, y, size, size, 0, 360);

        if (totalSeconds > 0) {
            float ratio = (float) remainingSeconds / totalSeconds;
            int arcExtent = -(int) Math.round(ratio * 360);
            Color arcColor = (remainingSeconds > 0 && remainingSeconds <= 10) ? Theme.danger : Theme.accent;
            g2.setColor(arcColor);
            g2.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x, y, size, size, 90, arcExtent);
        }

        String text = formatTime(remainingSeconds);
        g2.setColor(Theme.text);
        g2.setFont(Theme.SUBHEADING);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(text)) / 2;
        int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, tx, ty);

        g2.dispose();
    }

    private static String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}