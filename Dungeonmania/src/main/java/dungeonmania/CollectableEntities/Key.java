package dungeonmania.CollectableEntities;

import dungeonmania.util.Position;

public class Key extends CollectableEntity {
    private int keyId;
    public Key(String id, Position position, int keyId){
        super(id, "key", position);
        this.keyId = keyId;
    }

    public int getKey(){
        return this.keyId;
    }
    
}