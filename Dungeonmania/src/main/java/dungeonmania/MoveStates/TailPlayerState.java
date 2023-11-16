package dungeonmania.MoveStates;

import java.util.List;

import dungeonmania.Distance;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.StaticEntities.SwampTile;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class TailPlayerState implements MovementState {
    private String type = "tail_player";
    private MovingEntity entity;
    private Game game;

    public TailPlayerState(MovingEntity entity, Game game) {
        this.entity = entity;
        this.game = game;
    }

    @Override
    public Position findNextPosition() {

        List <Position> cardinals = new Distance(entity).getValidCardinalNeighbour(game, entity.getPosition());
        Position playerPrevPos = game.getPlayer().getPlayerPrevPos();
        // if player is idle, then ally merc is idle (1)

        // if it was the player that moved towards ther mercenary, then its prev position will be 2 spots away,
        // henc emercenary will remain idle as the player moving to it will siginify their closest proximity.
        // i.e. if the players prev position is not within cardnal adjacency of entitiy (2)
        if (game.getPlayer().getIsplayerIdle() || !cardinals.stream().anyMatch(i -> i.equals(playerPrevPos))) {
            return entity.getPosition();
        }

        return game.getPlayer().getPlayerPrevPos();
    }

    @Override
    public void move(Position nextPosition) {
        entity.setPosition(nextPosition);

    }

    @Override
    public String getType() {return type;}

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
            if (movement_factor == 1) {
                movEnt.setTicksRemaining(0);
            } else {
                movEnt.setTicksRemaining(movement_factor);
            }

            movEnt.setTicksRemaining(movement_factor);
        }
    }

}
