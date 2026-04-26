package ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

/**
 * Centralized UI theme — colors, fonts, and helper factories.
 */
public class UITheme {

    // --- Palette ---
    public static final Color BG_DARK        = new Color(15, 17, 26);
    public static final Color BG_PANEL       = new Color(22, 25, 40);
    public static final Color BG_CARD        = new Color(30, 34, 55);
    public static final Color ACCENT_BLUE    = new Color(64, 132, 255);
    public static final Color ACCENT_PURPLE  = new Color(138, 92, 246);
    public static final Color ACCENT_GREEN   = new Color(34, 197, 118);
    public static final Color ACCENT_RED     = new Color(239, 68, 68);
    public static final Color ACCENT_AMBER   = new Color(251, 191, 36);
    public static final Color TEXT_PRIMARY   = new Color(235, 237, 255);
    public static final Color TEXT_SECONDARY = new Color(140, 148, 190);
    public static final Color BORDER_COLOR   = new Color(45, 50, 80);
    public static final Color TABLE_ALT_ROW  = new Color(26, 29, 48);
    public static final Color INPUT_BG       = new Color(20, 23, 38);

    // --- Fonts ---
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 12);

    // --- Borders ---
    public static Border cardBorder() {
        return BorderFactory.createLineBorder(BORDER_COLOR, 1, true);
    }
    public static Border paddedBorder(int v, int h) {
        return BorderFactory.createEmptyBorder(v, h, v, h);
    }

    // --- Styled button factory ---
    public static JButton button(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_SUBTITLE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 20, 9, 20));

        // Hover effect
        Color hover = bg.brighter();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) { btn.setBackground(hover); }
            public void mouseExited(java.awt.event.MouseEvent e)  { btn.setBackground(bg); }
        });
        return btn;
    }

    // --- Styled text field factory ---
    public static JTextField textField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(TEXT_PRIMARY);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    // --- Styled label factory ---
    public static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    // --- Style a JComboBox ---
    public static void styleCombo(JComboBox<?> cb) {
        cb.setBackground(INPUT_BG);
        cb.setForeground(TEXT_PRIMARY);
        cb.setFont(FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }

    // --- Style a JTextArea ---
    public static JTextArea textArea() {
        JTextArea ta = new JTextArea();
        ta.setBackground(INPUT_BG);
        ta.setForeground(TEXT_PRIMARY);
        ta.setCaretColor(TEXT_PRIMARY);
        ta.setFont(FONT_MONO);
        ta.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    // --- Apply global LAF settings ---
    public static void applyGlobalDefaults() {
        UIManager.put("OptionPane.background",         BG_CARD);
        UIManager.put("Panel.background",              BG_CARD);
        UIManager.put("OptionPane.messageForeground",  TEXT_PRIMARY);
        UIManager.put("Button.background",             ACCENT_BLUE);
        UIManager.put("Button.foreground",             TEXT_PRIMARY);
        UIManager.put("ComboBox.background",           INPUT_BG);
        UIManager.put("ComboBox.foreground",           TEXT_PRIMARY);
        UIManager.put("TextField.background",          INPUT_BG);
        UIManager.put("TextField.foreground",          TEXT_PRIMARY);
    }
}
