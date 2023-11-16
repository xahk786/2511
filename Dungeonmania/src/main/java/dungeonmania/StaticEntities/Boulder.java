package dungeonmania.StaticEntities;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Entity;
import dungeonmania.games.Game;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class Boulder extends StaticEntity {
    public Boulder(String id, Position position) {
        super(id, "boulder", position, false, false);
    }

    // return switch if next position has switch
    public FloorSwitch isNextSwitch(Game game, Direction direction) {
        Position offset = direction.getOffset();
        int offsetX = offset.getX();
        int offsetY = offset.getY();
        Position newPosition = new Position(this.getPosition().getX() + offsetX, this.getPosition().getY() + offsetY);

        // get entities in new pos
        List<Entity> entities = game.getEntityList();
        List<Entity> entitiesAtNewPos = entities.stream().filter(i -> i.getPosition().equals(newPosition))
                                        .collect(Collectors.toList());
        for (Entity entity : entitiesAtNewPos) {
                if (entity instanceof FloorSwitch) {
                    return (FloorSwitch) entity;
                }
            }
        return null;                                   
    }

    public FloorSwitch isOnSwitch(Game game) {
        List<Entity> entities = game.getEntitiesAtPosition(this.getPosition());
        if (entities.stream().anyMatch(e -> e.getType().equals("switch"))) {
            return (FloorSwitch) entities.stream().filter(e -> e.getType().equals("switch")).findFirst().get();
        }
        return null;
    }

    // check if boulder can move
    public boolean canMove(Game game, Direction direction) {
        // get newPos
        Position offset = direction.getOffset();
        int offsetX = offset.getX();
        int offsetY = offset.getY();
        Position newPosition = new Position(this.getPosition().getX() + offsetX, this.getPosition().getY() + offsetY);

        // get entities in new pos
        List<Entity> entities = game.getEntityList();
        List<Entity> entitiesAtNewPos = entities.stream().filter(i -> i.getPosition().equals(newPosition))
                                        .collect(Collectors.toList());

        // loop through entities and check if all are passable
        for (Entity entity : entitiesAtNewPos) {
            if (entity.getIsPassable() == false) {
                return false;
            }
        }
        return true;
    }

    public void move(Direction direction) {
        Position offset = direction.getOffset();
        int offsetX = offset.getX();
        int offsetY = offset.getY();
        Position newPosition = new Position(this.getPosition().getX() + offsetX, this.getPosition().getY() + offsetY);
        // set new position
        this.setPosition(newPosition);
    }
}
