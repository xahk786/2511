package dungeonmania.goals;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Entity;

public class BouldersGoal implements Goal {

    @Override
    public Boolean isGoalCompleted(List<Entity> entities) {
        List<Entity> switches = entities.stream().filter(e -> e.getType().equals("switch")).collect(Collectors.toList());
        List<Entity> boulders = entities.stream().filter(e -> e.getType().equals("boulder")).collect(Collectors.toList());

        int nTriggered = 0;
        for (Entity entity : switches) {
            if (boulders.stream().anyMatch(e -> e.getPosition().equals(entity.getPosition()))) {
                nTriggered++;
            }
        }

        if (nTriggered == switches.size()) {
            return true;
        }
        
        return false;
    }

    @Override
    public String getGoalString(List<Entity> entities) {
        if (isGoalCompleted(entities)) {
            return "";
        }
        return ":boulders";
    }

    @Override
    public String getStartGoalString() {
        return ":boulders";
    }
    
}
