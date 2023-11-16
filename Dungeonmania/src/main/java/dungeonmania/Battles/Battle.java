package dungeonmania.Battles;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.Entity;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.ObserverEnemy;
import dungeonmania.MovingEntities.Player;

public class Battle {
    private String enemyType;
    private List<Round> rounds = new ArrayList<Round>();
    private double initialPlayerHealth;
    private double initialEnemyHealth;

    public Battle(List<Entity> entities, Player player, MovingEntity enemy) {
        this.enemyType = enemy.getType();
        this.initialPlayerHealth = player.getHealth();
        this.initialEnemyHealth = enemy.getHealth();
        fight(entities, player, enemy);
    }

    public Battle(String enemyType, List<Round> rounds, double initialPlayerHealth, double initialEnemyHealth) {
        this.enemyType = enemyType;
        this.rounds = rounds;
        this.initialPlayerHealth = initialPlayerHealth;
        this.initialEnemyHealth = initialEnemyHealth;
    }

    /**
     * Function that simulates each round of a battle
     * @param entities  List of entities in dungeon map
     * @param player    Player Entity
     * @param enemy     Enemy Entity
     */
    private void fight(List<Entity> entities, Player player, MovingEntity enemy) {
        while (player.getHealth() > 0 && enemy.getHealth() > 0) {
            rounds.add(new Round(player, enemy));
        }

        if (enemy.getHealth() <= 0) {
            entities.remove(enemy);
            player.addKillCount();
            if (enemy instanceof ObserverEnemy) {
                player.dettach( (ObserverEnemy) enemy);
            }
        }
        if (player.getHealth() <= 0) {
            entities.remove(player);
        }
    }

    // Getters
    public String getEnemyType()            { return enemyType;}
    public List<Round> getRounds()          { return rounds;}
    public double getInitialPlayerHealth()  { return initialPlayerHealth;}
    public double getInitialEnemyHealth()   { return initialEnemyHealth;}
}
