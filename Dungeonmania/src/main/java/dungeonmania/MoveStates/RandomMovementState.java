package dungeonmania.MoveStates;
import java.util.List;
import java.util.Random;

import dungeonmania.Distance;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;
import dungeonmania.StaticEntities.SwampTile;

public class RandomMovementState implements MovementState{
    private String type = "random";
    private MovingEntity movingEntity;
    private Game game;

    public RandomMovementState(MovingEntity entity, Game game) {
        this.movingEntity = entity;
        this.game = game;
    }


    // x 1 x
    // 7 z 3
    // x 5 x
    // zombie goes to a random position adjacent to it (only cardinally)
    public Position findNextPosition () {

        Position currentPos = movingEntity.getPosition();
        List<Position> validCardinal = new Distance(movingEntity).getValidCardinalNeighbour(game, currentPos);

        // check if zombie is trapped -> can go on portals, floorswitch, and null
        if (validCardinal.isEmpty()) {
            return currentPos;
        }

        Random rand = new Random();
        int randomNum = rand.nextInt(validCardinal.size());
        return (validCardinal.get(randomNum));
    }

    // per tick
    public void move (Position nextPosition) {
        movingEntity.setPosition(nextPosition);
    }


    @Override
    public MovingEntity getMovingEntity() {
        return movingEntity;
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
