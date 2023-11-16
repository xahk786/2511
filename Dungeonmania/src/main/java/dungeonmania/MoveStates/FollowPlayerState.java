package dungeonmania.MoveStates;

import java.util.List;
import java.util.Map;

import dungeonmania.Distance;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;
import dungeonmania.StaticEntities.SwampTile;

public class FollowPlayerState implements MovementState {
    private String type = "follow_player";
    private MovingEntity movingEntity;
    private Game game;


    public FollowPlayerState(MovingEntity entity) {
        this.movingEntity = entity;
    }

    public FollowPlayerState(MovingEntity entity, Game game) {
        this.movingEntity = entity;
        this.game = game;
    }


    public Position findNextPosition() {

        Position src = movingEntity.getPosition();
        Position des = game.getPlayer().getPosition();

        // else if not do dijkstra:

        Distance holder = new Distance(movingEntity);
        List <Position> gameMap = holder.getGameMap(game);

        List <Position> unblockedGameMap = holder.getWalkableGameMap(game, gameMap);
        holder.dijkstra(game, unblockedGameMap, src);
        Map<Position, Position> pred = holder.getPredecessors();

        // find the src starting from the des to get the srcs next best position
        Position prev = pred.get(des); // the pred map maps the position to its prev psosiion in the shortest path
        Position curr = null;
        if (prev == null) {
            return movingEntity.getPosition();
        } else if (movingEntity.getPosition().equals(prev)) {
            return prev;
        } else {
            while (!prev.equals(src)) {
                curr = prev;
                prev = pred.get(prev);
            }
        }
        return curr;
    }

    
    public MovingEntity getMovingEntity() {
        return movingEntity;
    }

    // follows the player -> moves to position with the shortest distance away from player
    public void move(Position nextPosition) {
        movingEntity.setPosition(nextPosition);
    }

    @Override
    public void setSwampTile(Position nextPosition, Game game) {
        // check if next position is swampTile
        SwampTile swampTile = (SwampTile) game.getEntitiesAtPosition(nextPosition).stream().filter(i -> i.getType().equals("swamp_tile")).findFirst().orElse(null);
        if (swampTile != null) {
            // set multiplying_factor in entity
            int movement_factor = swampTile.getMovement_factor();
            MovingEntity movEnt = getMovingEntity();
            movEnt.setTicksRemaining(movement_factor);
        }
    }
    
    @Override
    public String getType()     {return type;}
}


//  0 1 2 3 4 5 6 7 8 9   < - X
    //  - - - - - - - - - - 0  Y       smallest = 0 0
    //  X - X X X e X - - - 1  |       biggest = (9,3) this is our game map.
    //  - X - - - - X - - - 2  v
    //  P X X X X X X - - - 3