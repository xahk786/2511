package dungeonmania.MoveStates;

import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public interface MovementState {
    public Position findNextPosition ();
    public MovingEntity getMovingEntity();
    public void move(Position nextPosition);
    public void setSwampTile(Position nextPosition, Game game);
    public String getType();
}
