package dungeonmania.goals;

import dungeonmania.MovingEntities.Player;
import dungeonmania.games.Game;

import java.util.List;
import dungeonmania.Item;
import dungeonmania.Entity;

public class TreasureGoal implements Goal {
    private int nTreasureRequired;
    private Game game;

    public TreasureGoal(int nTreasureRequired, Game game) {
        this.nTreasureRequired = nTreasureRequired;
        this.game = game;
    }

    @Override
    public Boolean isGoalCompleted(List<Entity> entities) {
        Player player = game.getPlayer();
        List<Item> inventory = player.getInventory();
        long tCount = inventory.stream().filter(i -> i.getType().equals("treasure")).count();
        long ssCount = inventory.stream().filter(i -> i.getType().equals("sun_stone")).count();

        long count = tCount + ssCount;
        if (count >= nTreasureRequired) {
            return true;
        } 

        return false;
    }
    
    @Override
    public String getGoalString(List<Entity> entities) {
        if (isGoalCompleted(entities)) {
            return "";
        }
        if (!isGoalCompleted(entities)) {
            return ":treasure";
        }
        return "error";
    }

    @Override
    public String getStartGoalString() {
        return ":treasure";
    }

}
