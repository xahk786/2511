package dungeonmania.StaticEntities;

import java.util.List;

import dungeonmania.Item;
import dungeonmania.CollectableEntities.Key;
import dungeonmania.CollectableEntities.SunStone;
import dungeonmania.util.Position;

public class Door extends StaticEntity {
    private int keyId;
    private boolean isLocked = true;

    public Door(String id, Position position, int keyId) {
        super(id, "door", position, false, false);
        this.keyId = keyId;
    }

    public boolean getIsLocked() {
        return this.isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public int getKeyId() {
        return this.keyId;
    }

    public Boolean canOpen(List<Item> inventory) {
        // find first key in player inventory (only one key in inventory at a time)
        Key key = (Key) inventory.stream().filter(i-> i.getType().equals("key")).findFirst().orElse(null);
        SunStone ss = (SunStone) inventory.stream().filter(i-> i.getType().equals("sun_stone")).findFirst().orElse(null);
        if (key == null && ss == null) {
            return false;
        }

        // if sunstone
        if (key == null) {
            return true;
        }
        // if key in inventory
        else if (key.getKey() == keyId) {
            return true;
        }
        return false;
    }

    // try open door
    public void openDoor(List<Item> inventory) {
        Item key = inventory.stream().filter(i-> i.getType().equals("key")).findFirst().orElse(null);
        // if key in inv == door.keyid, unlock door
        // if sunstone, unlock door and do not remove sunstone
        if (canOpen(inventory)) {
            isLocked = false;
            this.setIsPassable(true);
            if (key != null) {
                inventory.remove(key);
            }
        }
    }
}
    

