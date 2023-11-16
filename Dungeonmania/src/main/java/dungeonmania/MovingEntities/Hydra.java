package dungeonmania.MovingEntities;

import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class Hydra extends ZombieToast { // hydra is probably not a zombie toast but since the movements are the same
    double healthIncreaseAmount;
    double healthIncreaseChance;


    public Hydra(String id, Position position, Game game) {
        super(id, "hydra", position, game);
    }

    // when a hydra is attacked, there is a chance it will spawn a head
    public void spawnHead() {
        setHealth(getHealth() + healthIncreaseAmount);
    }


    public double getHealthIncreaseChance() {return healthIncreaseChance;}
    public void setHealthIncreaseChance(double healthIncreaseChance) {this.healthIncreaseChance = healthIncreaseChance;}

    public double getHealthIncreaseAmount() {return healthIncreaseAmount;}
    public void setHealthIncreaseAmount(double healthIncreaseAmount) {this.healthIncreaseAmount = healthIncreaseAmount;}

}
