package client.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class Theme {

    public enum Palette { DAY, NIGHT }

    public static final Color DAY_BACKGROUND  = new Color(0xE8DCC0);
    public static final Color DAY_PANEL       = new Color(0xF4E8CC);
    public static final Color DAY_TEXT        = new Color(0x1A1A1A);
    public static final Color DAY_TEXT_DIM    = new Color(0x5C4A3A);
    public static final Color DAY_ACCENT      = new Color(0xD4A849);
    public static final Color DAY_DANGER      = new Color(0x8B0000);
    public static final Color DAY_BORDER      = new Color(0x968264);

    public static final Color NIGHT_BACKGROUND = new Color(0x0F1419);
    public static final Color NIGHT_PANEL      = new Color(0x1A2028);
    public static final Color NIGHT_TEXT       = new Color(0xE8E4D5);
    public static final Color NIGHT_TEXT_DIM   = new Color(0x7A8290);
    public static final Color NIGHT_ACCENT     = new Color(0xB5C5D6);
    public static final Color NIGHT_DANGER     = new Color(0xA03030);
    public static final Color NIGHT_BORDER     = new Color(0x3C4650);

    public static Color background = DAY_BACKGROUND;
    public static Color panel = DAY_PANEL;
    public static Color text = DAY_TEXT;
    public static Color textDim = DAY_TEXT_DIM;
    public static Color accent = DAY_ACCENT;
    public static Color danger = DAY_DANGER;
    public static Color border = DAY_BORDER;

    public static Palette current = Palette.DAY;

    public static Font HEADING;
    public static Font SUBHEADING;
    public static Font BODY;
    public static Font BUTTON;

    static {
        Font displayBase = loadFont("/assets/fonts/Cinzel-Regular.ttf", new Font("Serif", Font.PLAIN, 12));
        Font bodyBase    = loadFont("/assets/fonts/EBGaramond-Regular.ttf", new Font("Serif", Font.PLAIN, 12));

        HEADING    = displayBase.deriveFont(Font.BOLD, 22f);
        SUBHEADING = displayBase.deriveFont(Font.BOLD, 16f);
        BUTTON     = displayBase.deriveFont(Font.BOLD, 14f);
        BODY       = bodyBase.deriveFont(14f);
    }

    private static Font loadFont(String classpathPath, Font fallback) {
        try (InputStream is = Theme.class.getResourceAsStream(classpathPath)) {
            if (is == null) {
                System.err.println("Font not found on classpath: " + classpathPath + " — using fallback");
                return fallback;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            return font;
        } catch (Exception e) {
            System.err.println("Failed to load font " + classpathPath + ": " + e.getMessage() + " — using fallback");
            return fallback;
        }
    }

    public static void setPalette(Palette palette) {
        current = palette;
        if (palette == Palette.NIGHT) {
            background = NIGHT_BACKGROUND;
            panel = NIGHT_PANEL;
            text = NIGHT_TEXT;
            textDim = NIGHT_TEXT_DIM;
            accent = NIGHT_ACCENT;
            danger = NIGHT_DANGER;
            border = NIGHT_BORDER;
        } else {
            background = DAY_BACKGROUND;
            panel = DAY_PANEL;
            text = DAY_TEXT;
            textDim = DAY_TEXT_DIM;
            accent = DAY_ACCENT;
            danger = DAY_DANGER;
            border = DAY_BORDER;
        }
    }
}