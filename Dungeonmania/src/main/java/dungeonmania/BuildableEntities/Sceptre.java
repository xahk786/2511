package dungeonmania.BuildableEntities;

import dungeonmania.util.Position;

public class Sceptre extends BuildableEntity {
    private int mindControlDuration;

    public Sceptre(String id, Position position, int duration){
        super(id, "sceptre", position);
        this.mindControlDuration = duration;
    }

    public void setDuration(int dur){
        this.mindControlDuration = dur;
    }

    public int getDuration(){return this.mindControlDuration;}
}
