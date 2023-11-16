package dungeonmania.CollectableEntities;

import java.util.List;

import dungeonmania.Item;
import dungeonmania.util.Position;

public class Sword extends CollectableEntity {

    private int attack;
    private int durability;

    public Sword(String id, Position position, int attack, int durability){
        super(id, "sword", position);
        this.attack = attack;
        this.durability = durability;
    }
    
    public void setAttack(int attack){
        this.attack = attack;
    }

    public void setDurability(int durability){
        this.durability = durability;
    }

    public int getAttack(){
        return this.attack;
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
