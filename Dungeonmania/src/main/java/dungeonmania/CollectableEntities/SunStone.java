package dungeonmania.CollectableEntities;

import dungeonmania.util.Position;

public class SunStone extends CollectableEntity {
    // Can be used to open doors, and can be used interchangeably with treasure when building entities. But it cannot be used to bribe mercenaries or assassins. Since it is classed as treasure it counts towards the treasure goal. When used for opening doors, or when replacing another material such as a key or treasure in building entities, it is retained after use.
    
    public SunStone(String id, Position position){
        super(id, "sun_stone", position);
    }

}