package dungeonmania;

import dungeonmania.util.Position;

public class Item extends Entity {
    public Item(String id, String type, Position position){
        super(id, type, position, false, true);
    }

    public String getType() {
        return super.getType();
    }
}
