package dungeonmania.MovingEntities;

import java.util.List;

import dungeonmania.Entity;
import dungeonmania.tickable;
import dungeonmania.Battles.Battle;
import dungeonmania.MoveStates.RandomMovementState;
import dungeonmania.MoveStates.RunAwayState;
import dungeonmania.StaticEntities.Boulder;
import dungeonmania.StaticEntities.Door;
import dungeonmania.StaticEntities.Wall;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class ZombieToast extends MovingEntity implements tickable, ObserverEnemy {
    private int ticksRemaining;

    public ZombieToast (String id, String type, Position position, Game game){
        super(id, type, position, false, game);
        this.setMovementState(new RandomMovementState(this, game));
    }

    public void tick (Game game) {
        this.move(game);
    }


    public Boolean isBlocked (Entity entity) {
        return (entity instanceof Boulder ||
                entity instanceof Wall ||
                entity instanceof Door && ((Door) entity).getIsLocked()) ? true : false;
    }

    // ASSUMPTIONS: assumes only players can push boulders and zombies cant
    public void move(Game game) {
        // Check how many ticks remaining
        // if ticks remaining: reduce tick by one and return 
        if (ticksRemaining > 0) {
            this.ticksRemaining -= 1;
            return;
        }
        Position nextPosition = this.getMovementState().findNextPosition();
        // if next pos is swamptile
        this.getMovementState().setSwampTile(nextPosition, game);
        this.getMovementState().move(nextPosition);
        List<Entity> entities = game.getEntitiesAtPosition(nextPosition);
        if (entities.stream().anyMatch(i -> i instanceof Player)) {
            Player player = entities.stream().filter(i -> i instanceof Player).findAny().map(i -> (Player) i).orElse(null);
            if (!player.getIsInvisible()) {
                player.getBattles().add(new Battle(game.getEntityList(), game.getPlayer(), this));
            }
        }
    }

    @Override
    public void update(SubjectPlayer obj) {
        Player player = (Player) obj;
        if (player.getIsInvincible()) { // run away if player invincible ur not bribed
            this.setMovementState(new RunAwayState(this, this.getGame()));
        }
    }

    public void setTicksRemaining(int ticks) {
        this.ticksRemaining = ticks;
    }
}
