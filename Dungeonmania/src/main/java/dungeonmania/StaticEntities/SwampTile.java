package dungeonmania.StaticEntities;

import dungeonmania.util.Position;

public class SwampTile extends StaticEntity {
    private int movement_factor;
    public SwampTile(String id ,Position position, int movement_factor) {
        super(id, "swamp_tile", position, false, true);
        this.movement_factor = movement_factor;
    }
    
    public int getMovement_factor() {
        return movement_factor;
    }
    
}
