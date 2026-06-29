package client;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.FloatControl;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundManager {

    private static final Map<String, Clip> CLIPS = new HashMap<>();
    private static boolean muted = false;
    private static boolean initialized = false;

    public static void init() {
        if (initialized) return;
        initialized = true;
        preload("transition", "/assets/sounds/transition.wav",   0f);
        preload("death",      "/assets/sounds/death.wav",       -3f);
        preload("game_over",  "/assets/sounds/game_over.wav",  -24f);
    }

    private static void preload(String name, String classpathPath, float gainDb) {
        try (InputStream raw = SoundManager.class.getResourceAsStream(classpathPath)) {
            if (raw == null) {
                System.err.println("Sound not found: " + classpathPath);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(raw));
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float clamped = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), gainDb));
                gain.setValue(clamped);
            }

            CLIPS.put(name, clip);
        } catch (Exception e) {
            System.err.println("Failed to load " + classpathPath + ": " + e.getMessage());
        }
    }

    public static void play(String name) {
        if (muted) return;
        Clip clip = CLIPS.get(name);
        if (clip == null) return;
        if (clip.isRunning()) clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public static void setMuted(boolean m) { muted = m; }
    public static boolean isMuted() { return muted; }
}