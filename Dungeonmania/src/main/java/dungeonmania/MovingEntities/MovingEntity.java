package dungeonmania.MovingEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dungeonmania.Entity;
import dungeonmania.MoveStates.MovementState;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public abstract class MovingEntity extends Entity {
    private int ticksRemaining;
    private double maxHealth;
    private double health;
    private double attackDamage;
    private int spawnRate;
    private MovementState movementState;
    private boolean isReversedDirection = false;
    private Game game;

    // moving entities are passable
    public MovingEntity (String id, String type, Position position, boolean isInteractable, Game game){
        super(id, type, position, isInteractable, true);
        this.game = game;
    }


    public abstract Boolean isBlocked (Entity entity);

    private static final int FAIL = 0;
    private static final int SUCCESS = 1;

    // returns false if it fails, true if it succeeds
    public boolean actionSucceeds(double failRate) {

        List<Integer> percentage = new ArrayList<>();

        // attempt to bribe, bribe rate is a percentage? like 0.12 ...i think. idk
        int failPerc = (int)(failRate * 100);
        int successPerc = 100 - failPerc;
        for (int i = 0; i < failPerc; i++) {
            percentage.add(FAIL);
        }
        for (int i = 0; i < successPerc; i++) {
            percentage.add(SUCCESS);
        }

        // get a random number from 0 - 99 to simulate percentage chance
        int rand = new Random().nextInt(100);
        return (percentage.get(rand) == SUCCESS) ? true: false;

    }


    // getters + setters

    public double getHealth() {return health;}
    public double getMaxHealth(){return maxHealth;}
    public double getAttackDamage() {return attackDamage;}
    public int getSpawnRate() {return spawnRate;}
    public boolean getIsReversedDirection() {return isReversedDirection;}
    public MovementState getMovementState() {return movementState;}
    public Game getGame() {return game;}

    public void setHealth(double health) {this.health = health;}
    public void setMaxHealth(double health) {this.health = health;}
    public void setAttackDamage(double attackDamage) {this.attackDamage = attackDamage;}
    public void setSpawnRate(int rate) {this.spawnRate = rate;}
    public void setIsReversedDirection(boolean direction) {this.isReversedDirection = direction;}
    public void setMovementState(MovementState state) {this.movementState = state;}
    public void setGame(Game game)  {this.game = game;}
    public void setTicksRemaining(int ticks) {this.ticksRemaining = ticks;}
}
