package dungeonmania.StaticEntities;

import dungeonmania.Entity;
import dungeonmania.util.Position;

public class StaticEntity extends Entity {
    public StaticEntity(String id, String type, Position position, boolean isInteractable, boolean isPassable) {
        super(id, type, position, isInteractable, isPassable);
    }
}
