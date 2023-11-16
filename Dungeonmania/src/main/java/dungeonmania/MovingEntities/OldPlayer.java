package dungeonmania.MovingEntities;

import java.util.HashMap;
import java.util.List;

import dungeonmania.Helper;
import dungeonmania.BuildableEntities.MidnightArmour;
import dungeonmania.CollectableEntities.SunStone;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.games.Game;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class OldPlayer extends Player {

    int currTick;
    int deathTick;
    private HashMap<Integer, Direction> playerMovesHistory;
    private HashMap<Integer, String> tickItemHistory;
    private HashMap<Integer, List<String>> buildHistory;
    private HashMap<Integer, List<String>> interactHistory;


    public OldPlayer(String id, String type, Position pos, Game game) {
        super(id,"old_player", pos, game);
    }

    public boolean isBattleable () {
        // false if player wearning midignt armous, has a sunstone or is invisible.
        return (this.getInventory().stream().anyMatch(i -> i instanceof SunStone) ||
                this.getInventory().stream().anyMatch(i -> i instanceof MidnightArmour) ||
                this.getIsInvisible()) ? false : true;

    }
    // cant implement tickable and just tick it because of the exceptions it may throw
    public void retraceSteps(Game game) throws InvalidActionException{

        // the player ceases to exist if not killed off already onces it reaches the point it time travelled to
        if (currTick == deathTick) {
            game.removeEntity(this);
            game.setImpostPlayer(null);
            return;
        }

        if (playerMovesHistory.containsKey(currTick)) {
            this.move(game, playerMovesHistory.get(currTick));
            attemptNoTickMove(game);
            currTick++;
            attemptNoTickMove(game);
        } else {
            // ticks that consumer items are ALWAYS AFTER a normal moving tick
            if (tickItemHistory.containsKey(currTick)) {

                String itemUsedId = tickItemHistory.get(currTick);
                try {
                    attemptNoTickMove(game);
                }
                catch (Exception e) {
                    new InvalidActionException("");
                    new IllegalArgumentException("");
                } finally {
                    if (this.getItemViaId(itemUsedId) == null){
                        currTick++;
                        throw new InvalidActionException("Cannot use item, not in players inventory");
                    }
                    if (!Helper.isUsableItem(this.getItemViaId(itemUsedId))){
                        currTick++;
                        throw new IllegalArgumentException();
                    }
                    game.attemptUseItem(itemUsedId, this);
                    currTick++;
                    attemptNoTickMove(game);
                }
            }
        }

    }

    public void attemptNoTickMove(Game game) {
        try {
            List<String> interacts = interactHistory.get(currTick);

            for (String entityId : interacts) {
                this.interact(entityId, game);
            }
        }
        catch(Exception e) {
            new InvalidActionException("");
            new IllegalArgumentException("");
        }
        finally {
            try {

                List<String> builds = buildHistory.get(currTick);
                for (String item : builds) {
                    game.attemptBuild(item, this);
                }

            }
            catch(Exception e) {
                new InvalidActionException("");
                new IllegalArgumentException("");
            }
        }
    }

    public int getCurrTick () {return currTick;}
    public void setCurrTick(Integer currTick) {this.currTick = currTick;}
    public void setDeathTick(Integer deathTick) {this.deathTick = deathTick;}
    public void setPlayerMovesHistory(HashMap<Integer, Direction> playerMovesHistory) {this.playerMovesHistory = playerMovesHistory;}
    public void setTickItemHistory(HashMap<Integer, String> tickItemHistory) {this.tickItemHistory = tickItemHistory;}
    public void setBuildHistory(HashMap<Integer, List<String>> buildHistory) {this.buildHistory = buildHistory;}
    public void setInteractHistory(HashMap<Integer, List<String>> interactHistory) {this.interactHistory = interactHistory;}

    public HashMap<Integer, Direction> getPlayerMovesHistory() {return playerMovesHistory;}
    public HashMap<Integer, String> getTickItemHistory() {return tickItemHistory;}
    public HashMap<Integer, List<String>> getBuildHistory() {return buildHistory;}
    public HashMap<Integer, List<String>> getInteractHistory() {return interactHistory;}
}
