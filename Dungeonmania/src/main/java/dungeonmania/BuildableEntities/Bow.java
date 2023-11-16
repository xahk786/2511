package dungeonmania.BuildableEntities;

import java.util.List;

import dungeonmania.Item;
import dungeonmania.util.Position;

public class Bow extends BuildableEntity {
    private int durability; 

    public Bow(String id, Position position, int durability){
        super(id, "bow", position);
        this.durability = durability;
    }

    public void setDurability(int dur){
        this.durability = dur;
    }

    public int getDurability(){
        return this.durability;
    }

    public void reduceDurability(List<Item> inventory) {
        this.durability--;
        if (this.durability <= 0) {
            inventory.remove(this);
        }
    }
    
}