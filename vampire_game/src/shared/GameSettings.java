package shared;

import java.io.Serializable;

public class GameSettings implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int MIN_DISCUSSION = 30;
    public static final int MAX_DISCUSSION = 180;
    public static final int DEFAULT_DISCUSSION = 90;

    public static final int MIN_VOTE = 15;
    public static final int MAX_VOTE = 60;
    public static final int DEFAULT_VOTE = 45;

    public static final int MIN_NIGHT = 15;
    public static final int MAX_NIGHT = 60;
    public static final int DEFAULT_NIGHT = 45;

    public static final int[] VALID_PLAYER_COUNTS = {5, 7, 8, 9, 10, 11};
    public static final int DEFAULT_PLAYER_COUNT = 5;

    private int discussionSeconds;
    private int voteSeconds;
    private int nightSeconds;
    private int playerCount;

    public GameSettings() {
        this(DEFAULT_DISCUSSION, DEFAULT_VOTE, DEFAULT_NIGHT, DEFAULT_PLAYER_COUNT);
    }

    public GameSettings(int discussionSeconds, int voteSeconds, int nightSeconds, int playerCount) {
        this.discussionSeconds = clamp(discussionSeconds, MIN_DISCUSSION, MAX_DISCUSSION);
        this.voteSeconds = clamp(voteSeconds, MIN_VOTE, MAX_VOTE);
        this.nightSeconds = clamp(nightSeconds, MIN_NIGHT, MAX_NIGHT);
        this.playerCount = validatePlayerCount(playerCount);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static int validatePlayerCount(int count) {
        for (int valid : VALID_PLAYER_COUNTS) {
            if (count == valid) return count;
        }
        return DEFAULT_PLAYER_COUNT;
    }

    public int getDiscussionSeconds() { return discussionSeconds; }
    public int getVoteSeconds() { return voteSeconds; }
    public int getNightSeconds() { return nightSeconds; }
    public int getPlayerCount() { return playerCount; }

    public int getVampireCount() {
        switch (playerCount) {
            case 5: return 1;
            case 7: case 8: case 9: return 2;
            case 10: case 11: return 3;
            default: return 2;
        }
    }
    public int getSeerCount() {
        return 1;
    }

    public int getDoctorCount() {
        return 1;
    }

    public int getPeasantCount() {
        return playerCount - getVampireCount() - getSeerCount() - getDoctorCount();
    }
}