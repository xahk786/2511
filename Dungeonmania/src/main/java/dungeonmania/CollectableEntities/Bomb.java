package dungeonmania.CollectableEntities;

import java.util.ArrayList;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class Bomb extends CollectableEntity {
    private int bombRadius;

    public Bomb(String id, Position position, int bombRadius){
        super(id, "bomb", position);
        this.bombRadius = bombRadius;
    }

    public int getBombRadius(){
        return this.bombRadius;
    }
    
    public void plantBomb(Game game){
        if (game.getPlayer().getInventory().stream().anyMatch(x -> x.getId().equals(this.getId()))){
            this.setPosition(game.getPlayer().getPosition());
            game.getPlayer().removeItem(this.getId());
            game.getEntityList().add(this);
        }
    }

    public void blowUp(ArrayList <Entity> entities){ 
        Distance d = new Distance();
        ArrayList<Position> tiles = (ArrayList<Position>) d.getRadiusSquare(bombRadius, this.getPosition());

        for (int i = 0; i < entities.size(); i++){
            Entity e = entities.get(i);
            if (tiles.stream().anyMatch(x -> (e.getPosition().getX() == x.getX()) 
                && (e.getPosition().getY() == x.getY())
                && !e.getType().equals("player"))){
                entities.remove(e);
                i--;
            } 
        }
        entities.remove(this);
    }
    
}
