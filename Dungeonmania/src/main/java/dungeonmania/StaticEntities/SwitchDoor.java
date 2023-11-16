package dungeonmania.StaticEntities;

import dungeonmania.util.Position;

public class SwitchDoor extends StaticEntity {
    private String logic;
    private boolean isLocked = true;
    public SwitchDoor(String id, Position position, String logic) {
        super(id, "switch_door", position, false, true);
        this.logic = logic;
    }

    public String getLogic() {
        return this.logic;
    }

    public boolean getIsLocked() { return this.isLocked;}

    public boolean setIsLocked(boolean bool) {
        return this.isLocked = bool;
    }
}
