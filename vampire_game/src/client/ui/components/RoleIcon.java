package client.ui.components;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JComponent;

import client.ui.Theme;

public class RoleIcon extends JComponent {

    private String role = "";

    public RoleIcon() {
        this(32);
    }

    public RoleIcon(int size) {
        setPreferredSize(new Dimension(size, size));
        setOpaque(false);
    }

    public void setRole(String role) {
        this.role = role == null ? "" : role;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int cx = w / 2;
        int cy = h / 2;
        int r  = (Math.min(w, h) - 6) / 2;

        switch (role) {
            case "Vampire": drawVampire(g2, cx, cy, r); break;
            case "Seer":    drawSeer(g2, cx, cy, r);    break;
            case "Doctor":  drawDoctor(g2, cx, cy, r);  break;
            case "Peasant": drawPeasant(g2, cx, cy, r); break;
            default: break;
        }

        g2.dispose();
    }

    private void drawVampire(Graphics2D g2, int cx, int cy, int r) {
        g2.setColor(Theme.danger);
        int fangW = r * 4 / 5;
        int gap = 2;
        int[] lx = { cx - gap - fangW, cx - gap,             cx - gap - fangW / 2 };
        int[] ly = { cy - r,            cy - r,              cy + r };
        g2.fillPolygon(lx, ly, 3);
        int[] rx = { cx + gap,          cx + gap + fangW,    cx + gap + fangW / 2 };
        int[] ry = { cy - r,            cy - r,              cy + r };
        g2.fillPolygon(rx, ry, 3);
    }

    private void drawSeer(Graphics2D g2, int cx, int cy, int r) {
        g2.setColor(Theme.text);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - r, cy - r / 2, r * 2, r);
        g2.fillOval(cx - r / 2, cy - r / 2, r, r);
    }

    private void drawDoctor(Graphics2D g2, int cx, int cy, int r) {
        int thickness = Math.max(4, r * 2 / 3);
        g2.setColor(Theme.danger);
        g2.fillRect(cx - thickness / 2, cy - r, thickness, r * 2);
        g2.fillRect(cx - r, cy - thickness / 2, r * 2, thickness);
    }

    private void drawPeasant(Graphics2D g2, int cx, int cy, int r) {
        g2.setColor(Theme.text);
        int[] xs = { cx - r, cx + r, cx };
        int[] ys = { cy,     cy,     cy - r };
        g2.fillPolygon(xs, ys, 3);
        g2.fillRect(cx - r * 2 / 3, cy, r * 4 / 3, r);
    }
}