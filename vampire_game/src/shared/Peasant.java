package shared;

public class Peasant extends Role {
    private static final long serialVersionUID = 1L;

    public String getName() {
        return "Peasant";
    }

    public Team getTeam() {
        return Team.VILLAGE;
    }

    public boolean hasNightAction() {
        return false;
    }
}