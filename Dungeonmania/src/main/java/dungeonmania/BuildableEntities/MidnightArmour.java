package dungeonmania.BuildableEntities;

import dungeonmania.util.Position;

public class MidnightArmour extends BuildableEntity {

    private int midnight_armour_attack;
    private int midnight_armour_defence;

    public MidnightArmour(String id, Position position, int attack, int defence){
        super (id, "midnight_armour", position);
        this.midnight_armour_attack = attack;
        this.midnight_armour_defence = defence;
    }

    //getters 
    public int getAttack(){ return this.midnight_armour_attack;}
    public int getDefence(){ return this.midnight_armour_defence;}

    //setters
    public void setAttack(int attack){this.midnight_armour_attack = attack;}
    public void setDefence(int def){this.midnight_armour_defence = def;}
    
    
}
