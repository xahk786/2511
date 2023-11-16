package dungeonmania.MoveStates;

import java.util.List;

import dungeonmania.Distance;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.StaticEntities.SwampTile;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class RunAwayState implements MovementState {
    private String type = "run_away";
    private MovingEntity entity;
    private Game game;

    public RunAwayState(MovingEntity entity) {
        this.entity = entity;
    }
    public RunAwayState(MovingEntity entity, Game game) {
        this.entity = entity;
        this.game = game;
    }


    // gets the direction the player is moving and they go the other way
    // e.g.
    public Position findNextPosition() {

        // we run dikstra on all valid cardinal positions, find the distance away from each
        // and go the the square with the furthest distance?
        Distance dis = new Distance(entity);
        List<Position> validCardinals = dis.getValidCardinalNeighbour(game, entity.getPosition());
        List<Position> gameMap = dis.getGameMap(game);
        List<Position> unblockedGameMap = dis.getWalkableGameMap(game, gameMap);

        Position furthestPos = entity.getPosition();
        Double   furthestDis = 0.0;


        // for each cardinal, run dikstra, get the distance table, if the distance is longer,
        for (Position src : validCardinals) {
            dis.dijkstra(game, unblockedGameMap, src);
            Double dist = dis.getDistance().get(game.getPlayer().getPosition());


            if (dist > furthestDis) {
                furthestPos = src;
                furthestDis = dist;
            }
        }


        return furthestPos;
    }

    // moves in the opposite direction that the player is moving
    public void move(Position nextPosition) {
        entity.setPosition(nextPosition);
    }

    @Override
    public String getType()     {return type;}
    @Override
    public MovingEntity getMovingEntity() {
        return null;
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
}
