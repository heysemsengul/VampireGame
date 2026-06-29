package client.ui.components;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import client.ui.Theme;

public class ChatPane extends JPanel {

    private JTextPane textPane;
    private JScrollPane scrollPane;
    private StyledDocument doc;

    private Style chatStyle;
    private Style senderStyle;
    private Style systemStyle;
    private Style deathStyle;
    private Style voteStyle;
    private Style investigationStyle;
    private Style phaseStyle;

    public ChatPane() {
        setLayout(new BorderLayout());
        setOpaque(false);

        textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(Theme.BODY);
        textPane.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        doc = textPane.getStyledDocument();
        chatStyle          = doc.addStyle("chat",          null);
        senderStyle        = doc.addStyle("sender",        null);
        systemStyle        = doc.addStyle("system",        null);
        deathStyle         = doc.addStyle("death",         null);
        voteStyle          = doc.addStyle("vote",          null);
        investigationStyle = doc.addStyle("investigation", null);
        phaseStyle         = doc.addStyle("phase",         null);

        setupStyleAttributes();

        scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        applyTheme();
    }

    private void setupStyleAttributes() {
        StyleConstants.setBold(senderStyle, true);
        StyleConstants.setItalic(systemStyle, true);
        StyleConstants.setBold(deathStyle, true);
        StyleConstants.setBold(voteStyle, true);
        StyleConstants.setItalic(investigationStyle, true);
        StyleConstants.setBold(phaseStyle, true);
        StyleConstants.setAlignment(phaseStyle, StyleConstants.ALIGN_CENTER);
    }

    public void appendChat(String sender, String message) {
        appendStyled(sender + ": ", senderStyle);
        appendStyled(message + "\n", chatStyle);
    }

    public void appendSystem(String text) {
        appendStyled("• " + text + "\n", systemStyle);
    }

    public void appendDeath(String text) {
        appendStyled("• " + text + "\n", deathStyle);
    }

    public void appendVoteResult(String text) {
        appendStyled("• " + text + "\n", voteStyle);
    }

    public void appendInvestigation(String text) {
        appendStyled("✦ " + text + "\n", investigationStyle);
    }

    public void appendPhase(String phaseName) {
        String text = "\n── " + phaseName + " ──\n";
        try {
            int start = doc.getLength();
            doc.insertString(start, text, phaseStyle);
            doc.setParagraphAttributes(start, text.length(), phaseStyle, false);
        } catch (BadLocationException e) {
        	
        }
        scrollToBottom();
    }

    public void appendBare(String text) {
        appendStyled(text + "\n", chatStyle);
    }

    public void clear() {
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
        	
        }
    }

    private void appendStyled(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
        	
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        textPane.setCaretPosition(doc.getLength());
    }

    public void applyTheme() {
        StyleConstants.setForeground(chatStyle,          Theme.text);
        StyleConstants.setForeground(senderStyle,        Theme.accent);
        StyleConstants.setForeground(systemStyle,        Theme.textDim);
        StyleConstants.setForeground(deathStyle,         Theme.danger);
        StyleConstants.setForeground(voteStyle,          Theme.accent);
        StyleConstants.setForeground(investigationStyle, Theme.accent);
        StyleConstants.setForeground(phaseStyle,         Theme.textDim);

        textPane.setBackground(Theme.panel);
        textPane.setForeground(Theme.text);
        if (scrollPane != null) {
            scrollPane.setBorder(new LineBorder(Theme.border, 1));
            scrollPane.getViewport().setBackground(Theme.panel);
        }
    }
}