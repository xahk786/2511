package dungeonmania.MoveStates;

import java.util.List;
import java.util.stream.IntStream;

import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;
import dungeonmania.StaticEntities.SwampTile;


public class CircularState implements MovementState {
    private String type = "circular";
    private MovingEntity entity;

    public CircularState(MovingEntity entity) {
        this.entity = entity;
    }

    // returns the next position. only executes
    // how a spider moves: spawns at c and goes up to index one.
    // 0 1 2
    // 7 c 3
    // 6 5 4
    public Position findNextPosition () {
        Position center = entity.getSpawnPosition();    // spawn position for spider
        Position currentPos = entity.getPosition();     // current spider postiion
        List<Position> adjacentPositions = center.getAdjacentPositions();
        int movementRange = adjacentPositions.size();   // circular movement range excluding spawn (should be 8)

        // index of spiders current positions in relation to the list o adjacent positions
        int currPosIndex = IntStream.range(0, movementRange)
                                    .filter(i -> adjacentPositions.get(i).equals(currentPos))
                                    .findFirst()
                                    .orElse(-1);

        // if spider just spawned, move up to 1
        if (currentPos.equals(center)) {
            if (entity.getIsReversedDirection()) {
                return  adjacentPositions.get(5);
            } else {
                return adjacentPositions.get(1);
            }
        // if spider if already circling and not reversed
        } else if (!entity.getIsReversedDirection()) {
            // if pos at indx 7, goes back to index 0
            if (currPosIndex == movementRange - 1) {
                return adjacentPositions.get(0);
            } else {
                return adjacentPositions.get(currPosIndex + 1);
            }
        }
        // not in spanw centre, and reversed.
        // if pos at indx 7, goes back to index 0
        if (currPosIndex == 0) {
            return adjacentPositions.get(7);
        }
        return adjacentPositions.get(currPosIndex - 1);
    }

    public MovingEntity getMovingEntity() {
        return entity;
    }

    // changes the entities current position if entity can move to next position.
    public void move(Position nextPosition) {
        entity.setPosition(nextPosition);
    }

    // checks if nextPos is swamp tile, if so sets MF
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

