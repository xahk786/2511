package dungeonmania.goals;

import java.util.List;

import dungeonmania.Entity;

public interface Goal {
    public Boolean isGoalCompleted(List<Entity> entities);

    public String getGoalString(List<Entity> entities);

    public String getStartGoalString();
}
