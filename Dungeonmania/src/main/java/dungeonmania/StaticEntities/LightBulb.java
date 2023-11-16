package dungeonmania.StaticEntities;

import dungeonmania.util.Position;

public class LightBulb extends StaticEntity {
    private String logic;
    public LightBulb(String id, String type, Position position, String logic) {
        super(id, type, position, false, true);
        this.logic = logic;
    }

    public void setLightBulbOn() {
        super.setType("light_bulb_on");
    }

    public void setLightBulbOff() {
        super.setType("light_bulb_off");
    }

    public String getLogic() {
        return this.logic;
    }
}
