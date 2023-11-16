package dungeonmania.goals;

import java.util.List;

import dungeonmania.Entity;

public class ExitGoal implements Goal {

    @Override
    public Boolean isGoalCompleted(List<Entity> entities) {
        Entity player = entities.stream().filter(e -> e.getType().equals("player")).findFirst().orElse(null);
        if (player == null) { return false;}
        Entity exit = entities.stream().filter(e -> e.getType().equals("exit")).findFirst().orElse(null);

        if (player.getPosition().equals(exit.getPosition())) {
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public String getGoalString(List<Entity> entities) {
        if (isGoalCompleted(entities)) {
            return "";
        } 

        if (!isGoalCompleted(entities)) {
            return ":exit";
        } 

        return "error";

    }

    @Override
    public String getStartGoalString() {
        return ":exit";
    }

    
    
}
