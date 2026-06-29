package shared;

public class Doctor extends Role {
    private static final long serialVersionUID = 1L;

    public String getName() {
        return "Doctor";
    }

    public Team getTeam() {
        return Team.VILLAGE;
    }

    public boolean hasNightAction() {
        return true;
    }
}