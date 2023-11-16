package dungeonmania.goals;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.List;

import dungeonmania.Entity;
import dungeonmania.games.Game;

public class AndGoal implements Goal {
    private Goal goal1;
    private Goal goal2;

    public AndGoal (JSONArray subgoals, Game game) {
        JSONObject jsongoal1 = subgoals.getJSONObject(0);
        this.goal1 = GoalFactory.createGoal(jsongoal1, game);
        
        JSONObject jsongoal2 = subgoals.getJSONObject(1);
        this.goal2 = GoalFactory.createGoal(jsongoal2, game);
    }

    @Override
    public Boolean isGoalCompleted(List<Entity> entities) {
        if (goal1.isGoalCompleted(entities) && goal2.isGoalCompleted(entities)) {
            return true;
        }
        return false;

    }

    @Override
    public String getGoalString(List<Entity> entities) {
        if (isGoalCompleted(entities)) {
            return "";
        }
        if (goal1.isGoalCompleted(entities) && !goal2.isGoalCompleted(entities)) {
            return goal2.getGoalString(entities);
        }
        if (!goal1.isGoalCompleted(entities) && goal2.isGoalCompleted(entities)) {
            return goal1.getGoalString(entities);
        }
        return "(" + goal1.getGoalString(entities) + " AND " + goal2.getGoalString(entities) + ")";
    }


    @Override
    public String getStartGoalString() {
        return "(" + goal1.getStartGoalString() + " AND " + goal2.getStartGoalString() + ")";
    }
    
}
