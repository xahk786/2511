package dungeonmania.BuildableEntities;

import java.util.List;

import dungeonmania.Item;
import dungeonmania.util.Position;

public class Shield extends BuildableEntity {
    private int defence;
    private int durability;

    public Shield(String id, Position position, int defence, int durability){
        super(id, "shield", position);
        this.defence = defence;
        this.durability = durability;
    }

    public int getDurability(){
        return this.durability;
    }

    public void setDurability(int dur){
        this.durability = dur;
    }

    public void setDefence(int def){
        this.defence = def;
    }

    public int getDefence(){
        return this.defence;
    }

    public void reduceDurability(List<Item> inventory) {
        this.durability--;
        if (this.durability <= 0) {
            inventory.remove(this);
        }
    }
    
}
