package dungeonmania.goals;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Entity;
import dungeonmania.MovingEntities.Player;

public class EnemiesGoal implements Goal {
    private int nEnemiesRequired;

    public EnemiesGoal(int nEnemiesRequired) {
        this.nEnemiesRequired = nEnemiesRequired;
    }

    @Override
    public Boolean isGoalCompleted(List<Entity> entities) {
        Player player = (Player) entities.stream().filter(e -> e.getType().equals("player")).findFirst().orElse(null);
        long nSpawners = entities.stream().filter(e -> e.getType().equals("zombie_toast_spawner")).collect(Collectors.counting());
        if (player.getKillCount() >= nEnemiesRequired && nSpawners <= 0) {
            return true;
        }
        return false;
    }

    @Override
    public String getGoalString(List<Entity> entities) {
        if (isGoalCompleted(entities)) {
            return "";
        }
        return ":enemies";
    }

    @Override
    public String getStartGoalString() {
        return ":enemies";
    }

    
    
}
