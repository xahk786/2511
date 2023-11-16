package dungeonmania.StaticEntities;

import dungeonmania.util.Position;

public class Wire extends StaticEntity {
    /**
     *  if state is true, wire is on and 
     *  if state is false, wire if off
     */
    private boolean state;
    private boolean visited = false;

    public Wire(String id, Position position) {
        super(id, "wire", position, false, true);
        this.state = false;
    }

    public void setState(boolean bool)   { this.state = bool;}
    public void setVisited(boolean bool) { this.visited = bool;}

    public boolean getState()   { return this.state;}
    public boolean getVisited() { return this.visited;}

}
