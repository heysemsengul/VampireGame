package client.ui.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

import client.ui.Theme;

public class GothicButton extends JButton {

    public enum Variant { ACCENT, DANGER }

    private final Variant variant;
    private boolean hovered = false;
    private boolean pressed = false;

    public GothicButton(String text) {
        this(text, Variant.ACCENT);
    }

    public GothicButton(String text, Variant variant) {
        super(text);
        this.variant = variant;

        setOpaque(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);

        setFont(Theme.BUTTON);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(variant == Variant.DANGER ? Color.WHITE : Theme.text);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered (MouseEvent e) { hovered = true;  repaint(); }
            @Override public void mouseExited  (MouseEvent e) { hovered = false; pressed = false; repaint(); }
            @Override public void mousePressed (MouseEvent e) { pressed = true;  repaint(); }
            @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(d.width + 24, Math.max(d.height, 36));
    }

    @Override
    protected void paintComponent(Graphics g) {
    	setForeground(variant == Variant.DANGER ? Color.WHITE : Theme.text);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();
        int arc = 8;

        Color base = (variant == Variant.DANGER) ? Theme.danger : Theme.accent;
        Color fill;
        if (!isEnabled()) {
            fill = blend(base, Theme.background, 0.55f);
        } else if (pressed) {
            fill = blend(base, Color.BLACK, 0.20f);
        } else if (hovered) {
            fill = blend(base, Color.WHITE, 0.12f);
        } else {
            fill = base;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.setColor(Theme.border);
        g2.setStroke(new BasicStroke(hovered ? 1.5f : 0.7f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, arc, arc);
        g2.dispose();

        super.paintComponent(g);
    }

    private static Color blend(Color a, Color b, float t) {
        int r  = Math.round(a.getRed()   * (1 - t) + b.getRed()   * t);
        int g  = Math.round(a.getGreen() * (1 - t) + b.getGreen() * t);
        int bl = Math.round(a.getBlue()  * (1 - t) + b.getBlue()  * t);
        return new Color(r, g, bl);
    }
}