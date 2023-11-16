package dungeonmania;

import java.io.Serializable;

import dungeonmania.util.Position;

public class Entity implements Serializable {
    private String id;
    private String type;
    private Position position;
    private Position spawnPosition;
    private boolean isInteractable;
    private boolean isPassable;


    public Entity(String id, String type, Position position, boolean isInteractable, boolean isPassable) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.isInteractable = isInteractable;
        this.spawnPosition = position;
        this.isPassable = isPassable;
    }

    //getters
    public String getId() {
        return this.id;
    }

    public String getType() {
        return this.type;
    }

    public Position getPosition() {
        return this.position;
    }
    public Position getSpawnPosition() {
        return this.spawnPosition;
    }

    public boolean getIsInteractable() {
        return this.isInteractable;
    }

    public boolean getIsPassable () {
        return this.isPassable;
    }


    // setters
    public void setPosition(Position position) {this.position = position;}
    public void setSpawnPosition(Position pos) {this.spawnPosition = pos;}
    public void setIsPassable(Boolean isPassable) {this.isPassable = isPassable;}
    public void setId(String id) {this.id = id;}
    public void setIsInteractable (boolean isInteractable) {this.isInteractable = isInteractable;}

    public void setType(String type) {
        this.type = type;
    }
}
