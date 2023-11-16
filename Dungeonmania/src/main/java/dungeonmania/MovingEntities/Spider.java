package dungeonmania.MovingEntities;

import java.util.List;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.tickable;
import dungeonmania.Battles.Battle;
import dungeonmania.MoveStates.CircularState;
import dungeonmania.StaticEntities.Boulder;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class Spider extends MovingEntity implements tickable {
    private int ticksRemaining;
    public Spider (String id, Position position, Game game){
        super(id, "spider", position, false, game);
        this.setMovementState(new CircularState(this));

    }

    public void tick (Game game) {
            this.move(game);

    }

    public static void spawn (Game game, int spawnRate) {
        // spiders spawn at the first available location starting from 0,0.
        // CHECK that location is NOT BLOCKED.
        if ((spawnRate != 0) && (game.getTickCounter() % spawnRate == 0)) {

            List<Position> gameMap = new Distance().getGameMap(game);

            // check if position has an entity that is blocked. if not, make it the spawn position
            // it also cant sppawn on human.
            Position spawnPos = null;
            for (Position gameMapPos : gameMap) {
                List<Entity> entitiesAtPos = game.getEntitiesAtPosition(gameMapPos);
                if (!entitiesAtPos.stream().anyMatch(i -> i instanceof Boulder) &&
                    !entitiesAtPos.stream().anyMatch(i -> i instanceof Player)) {
                    spawnPos = gameMapPos;
                    break;
                }
            }

            if (spawnPos != null) {
                Spider newSpider = (Spider) game.newEntity("spider", spawnPos);
                game.addEntitiy(newSpider);
            }
        }
    }

    public void setTicksRemaining(int ticks) {
        this.ticksRemaining = ticks;
    }

    // Spiders are able to traverse through walls, doors, switches, portals, exits (which have no effect),
    // but not boulders, in which case it will reverse direction (see a visual example here).
    // if they collide with a player they go to battle


    public void move(Game game) {
        // Check how many ticks remaining
        // if ticks remaining: reduce tick by one and return 
        if (ticksRemaining > 0) {
            this.ticksRemaining -= 1;
            return;
        }
        
        Position nextPosition = this.getMovementState().findNextPosition();
        List<Entity> entitiesAtPos = game.getEntitiesAtPosition(nextPosition);

        // if none of the entities existing on the next position we want to go, then move
        if (entitiesAtPos.isEmpty() || !entitiesAtPos.stream().anyMatch(i -> isBlocked(i)))  {
            // if next pos is swamp tile
            this.getMovementState().setSwampTile(nextPosition, game);
            this.getMovementState().move(nextPosition);
        } else { // one side is blocked, check the other side.
            changeDirection();
            Position newNextPos = this.getMovementState().findNextPosition();
            entitiesAtPos = game.getEntitiesAtPosition(newNextPos);

            if (!entitiesAtPos.stream().anyMatch(i -> isBlocked(i))) {
                Position newNextPos1 = this.getMovementState().findNextPosition();
                // if next pos is swamp tile
                this.getMovementState().setSwampTile(newNextPos1, game);
                this.getMovementState().move(newNextPos1);
            }
        }

        // commence battle if enemy runs into a player
        List<Entity> entities = game.getEntitiesAtPosition(this.getPosition());
        if (entities.stream().anyMatch(i -> i instanceof Player)) {
            Player player = entities.stream().filter(i -> i instanceof Player).findAny().map(i -> (Player) i).orElse(null);
            if (!player.getIsInvisible()) {
                player.getBattles().add(new Battle(game.getEntityList(), game.getPlayer(), this));
            }
        }
    }


    // spiders can pass through boulders
    public Boolean isBlocked (Entity entity) {
        return (entity instanceof Boulder) ? true : false;
    }

    public void changeDirection () {
        if (this.getIsReversedDirection()) {
            this.setIsReversedDirection(false);
        } else {
            setIsReversedDirection(true); }
    }
}
