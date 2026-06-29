package shared;

public class Seer extends Role {
    private static final long serialVersionUID = 1L;

    public String getName() {
        return "Seer";
    }

    public Team getTeam() {
        return Team.VILLAGE;
    }

    public boolean hasNightAction() {
        return true;
    }
}