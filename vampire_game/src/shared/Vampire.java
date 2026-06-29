package shared;

public class Vampire extends Role {
    private static final long serialVersionUID = 1L;

    public String getName() {
        return "Vampire";
    }

    public Team getTeam() {
        return Team.VAMPIRE;
    }

    public boolean hasNightAction() {
        return true;
    }
}