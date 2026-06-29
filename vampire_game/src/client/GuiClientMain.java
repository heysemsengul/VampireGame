package client;

import javax.swing.SwingUtilities;

import client.ui.MainFrame;

public class GuiClientMain {
    public static void main(String[] args) {
      /*  SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
        */
        SwingUtilities.invokeLater(() -> {
            SoundManager.init();
            new MainFrame().setVisible(true);
        });
    }
}