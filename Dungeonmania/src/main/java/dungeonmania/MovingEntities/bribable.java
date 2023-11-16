package dungeonmania.MovingEntities;

import java.util.List;
import dungeonmania.Item;

public interface bribable {

    public boolean getIsBribed();
    public void bribe(List<Item> inventory);
    public void setIsMindControlled(boolean bool);
    public boolean getIsMindControlled();
    public void increaseMindControlDuration();
    public int getBribeRadius();
    public int getBribeAmount();
}
