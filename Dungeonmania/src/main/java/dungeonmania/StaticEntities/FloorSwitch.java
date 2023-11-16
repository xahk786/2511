package dungeonmania.StaticEntities;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.CollectableEntities.Bomb;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class FloorSwitch extends StaticEntity {
    private boolean isBoulderonSwitch;
    private String logic = null;

    public FloorSwitch(String id, Position position) {
        super(id, "switch", position, false, true);
    }

    public FloorSwitch(String id, Position position, String logic) {
        super(id, "switch", position, false, true);
        this.logic = logic;
    }

    public boolean isBoulderonSwitch(){
        return this.isBoulderonSwitch;
    }

    public void setBoulderOnSwitch(boolean bool){
        this.isBoulderonSwitch = bool;
    }

    public void activateConnectedEntities(Game game, Entity entity) {
        List<Position> cardinals = new Distance().getCardinals(entity.getPosition());
        for (Position cardinal : cardinals) {
            List<Entity> entsAtPosition = game.getEntitiesAtPosition(cardinal);
            for (Entity ent : entsAtPosition) {
                if (ent instanceof Bomb) {
                    ((Bomb) ent).blowUp(game.getEntityList());
                }
                else if (ent instanceof LightBulb) {
                    ((LightBulb) ent).setLightBulbOn();
                }
                else if (ent instanceof SwitchDoor) {
                    ((SwitchDoor) ent).setIsLocked(false);
                }
                else if (ent instanceof Wire) {
                    Wire wire = (Wire) ent;
                    if (wire.getVisited() == false) {
                        wire.setState(true);
                        wire.setVisited(true);
                        activateConnectedEntities(game, wire);
                        wire.setVisited(false);
                    }
                }
            }
        }
    }

    public void deactivateConnectedEntities(Game game, Entity entity) {
        List<Position> cardinals = new Distance().getCardinals(entity.getPosition());
        for (Position cardinal : cardinals) {
            List<Entity> entsAtPosition = game.getEntitiesAtPosition(cardinal);
            for (Entity ent : entsAtPosition) {
                if (ent instanceof LightBulb) {
                    ((LightBulb) ent).setLightBulbOff();
                }
                else if (ent instanceof SwitchDoor) {
                    ((SwitchDoor) ent).setIsLocked(true);
                }
                else if (ent instanceof Wire) {
                    Wire wire = (Wire) ent;
                    if (wire.getVisited() == false) {
                        wire.setState(false);
                        wire.setVisited(true);
                        deactivateConnectedEntities(game, wire);
                        wire.setVisited(false);
                    }
                }
            }
        }
    }

    // boolean isTriggered - check if boulder on switch
    public boolean isTriggered(Game game) {
    // if getPosition() == entities.getPosition() && entities instanceof boulder ret true
        List<Entity> entities = game.getEntitiesAtPosition(this.getPosition());
        for (Entity entity : entities) {
            if (entity instanceof Boulder) {
                return true;
            }
        }
        return false;
    }

    // detonate - detonate any cardinal bombs
    public void detonate(Game game) {
        List <Entity> entities = game.getEntityList();

        // get cardinally adjacent entities
        // check if adjacnet cardinal is bomb
        int x = getPosition().getX();
        int y = getPosition().getY();

        List<Position> positions = Arrays.asList(
            new Position(x, y + 1),
            new Position(x, y - 1),
            new Position(x - 1, y),
            new Position(x + 1, y)
        );

        for (Position pos : positions) {
            // there could be more than one entity on tile
            List<Entity> entitiesOnPos = entities.stream().filter(i -> i.getPosition().equals(pos)).collect(Collectors.toList());
            // if any is bomb, explode bomb
            for (Entity ent : entitiesOnPos) {
                if (ent instanceof Bomb) {
                    //((Bomb) entity).explode();
                    ((Bomb) ent).blowUp(game.getEntityList());;
                }
            }
        }
    }
}
