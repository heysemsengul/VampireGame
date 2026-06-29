package shared;

import java.io.Serializable;

public abstract class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract String getName();
    public abstract Team getTeam();
    public abstract boolean hasNightAction();

    @Override
    public String toString() {
        return getName();
    }
}