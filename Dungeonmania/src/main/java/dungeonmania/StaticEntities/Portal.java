package dungeonmania.StaticEntities;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.games.Game;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class Portal extends StaticEntity {
    private String colour;
    public Portal(String id, Position position, String colour) {
        super(id, "portal", position, false, true);
        this.colour = colour;
    }

    public void setColour(String colour) {
        this.colour = colour;
    }

    public String getColour() {
        return colour;
    }
    
    // teleport() args dungeon, direction - return new position
    public Position teleport(Game game, Direction direction) {
        // get game.entities and loop search for instanceof portal
        List<Entity> entities = game.getEntityList();
        // get all portals
        List<Portal> portals = entities.stream().filter(i -> i.getType().equals("portal")).map(i -> (Portal) i).collect(Collectors.toList());
        // if this.colour == portal.colour && this.id != portal.id 
        Portal linkedPortal = portals.stream().filter(i -> i.getColour().equals(getColour()))
                                        .filter(i -> i.getId() != getId())
                                        .findFirst()
                                        .orElse(null);
        // get position of portal
        Boolean searchForAdj = false;
        Position linkedPortalPos = linkedPortal.getPosition();
        int offsetX = direction.getOffset().getX();
        int offsetY = direction.getOffset().getY();
        Position newPlayerPos = new Position(linkedPortalPos.getX() + offsetX, linkedPortalPos.getY() + offsetY);
        List<Entity> entAtNewPos = entities.stream().filter(i -> i.getPosition().equals(newPlayerPos)).collect(Collectors.toList());
        for (Entity entity : entAtNewPos) {
            if (entity.getIsPassable() == false) {
                searchForAdj = true;
            }
        }
        if (searchForAdj == true) {
            // Get cardinal positions of portal
            List<Position> cardinals = new Distance().getCardinals(linkedPortalPos);
            // check Entities isPassable at each cardinal, if not return position of this portal
            for (Position cardinal : cardinals) {
                List<Entity> entitiesAtNewPos = entities.stream().filter(i -> i.getPosition().equals(cardinal)).collect(Collectors.toList());
                Boolean passable = true;
                // if not passable, check next cardinal
                for (Entity entity : entitiesAtNewPos) {
                    if (!entity.getIsPassable()) {
                        passable = false;
                        break;
                    }
                }
                if (passable) { return cardinal;}
            }
            return this.getPosition();
        }
        return newPlayerPos;
        
    }
}
