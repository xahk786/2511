package dungeonmania.Battles;

import java.util.ArrayList;
import java.util.List;

import dungeonmania.Item;
import dungeonmania.BuildableEntities.Bow;
import dungeonmania.BuildableEntities.MidnightArmour;
import dungeonmania.BuildableEntities.Shield;
import dungeonmania.CollectableEntities.Sword;
import dungeonmania.MovingEntities.Hydra;
import dungeonmania.MovingEntities.Mercenary;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.OldPlayer;
import dungeonmania.MovingEntities.Player;

public class Round {
    private double deltaPlayerHealth;
    private double deltaEnemyHealth;
    private List<Item> weaponryUsed = new ArrayList<Item>();

    public Round (double deltaPlayerHealth, double deltaEnemyHealth, List<Item> weaponryUsed) {
        this.deltaPlayerHealth = deltaPlayerHealth;
        this.deltaEnemyHealth = deltaEnemyHealth;
        this.weaponryUsed = weaponryUsed;
    }

    public Round (Player player, MovingEntity enemy) {
        if (player.getIsInvincible()) {
            this.deltaPlayerHealth = 0;
            this.deltaEnemyHealth = -enemy.getHealth();
            this.weaponryUsed.add((Item) player.getPotionQueue().get(0));
        } else if (enemy instanceof OldPlayer && ((OldPlayer) enemy).getIsInvincible()) {
            this.deltaEnemyHealth = 0;
            this.deltaPlayerHealth = -player.getHealth();
            player.setHealth(0); // we instantly die
            return;
        } else {
            this.deltaEnemyHealth = calculatePlayerAttack(player, enemy);
            this.deltaPlayerHealth = calculateEnemyAttack(player, enemy);
        }

        // special enemies
        if (enemy instanceof Hydra) {
            attackHydra(player, enemy);
            return;
        }
        

        enemy.setHealth(enemy.getHealth() + this.deltaEnemyHealth);
        player.setHealth(player.getHealth() + this.deltaPlayerHealth);
    }


    public void attackHydra(Player player, MovingEntity enemy) {
        // if health increase chance fails, then take away health normally
        if (enemy.actionSucceeds(((Hydra) enemy).getHealthIncreaseChance())) {
            ((Hydra) enemy).spawnHead();
            this.deltaEnemyHealth = ((Hydra) enemy).getHealthIncreaseAmount();
        } else {
            enemy.setHealth(enemy.getHealth() + this.deltaEnemyHealth);
        }
        player.setHealth(player.getHealth() + this.deltaPlayerHealth);
    }



    

    /**
     *
     * @param player
     * @param enemy
     * @return the damage dealt by player
     */
    private double calculatePlayerAttack(Player player, MovingEntity enemy) {
        double base = player.getAttackDamage();
        double bow = 1;
        double sword = 0;
        double ally = 0;
        double mArmourAtk = 0;

        // get bow multiplier
        if (player.getInventory().stream().anyMatch(i -> i.getType().equals("bow"))) {
            bow = 2;
            Bow bowItem = (Bow) player.getInventory().stream().filter(i -> i.getType().equals("bow")).findFirst().orElse(null);
            weaponryUsed.add(bowItem);

            bowItem.reduceDurability(player.getInventory());
        }

        // get sword damage
        if (player.getInventory().stream().anyMatch(i -> i.getType().equals("sword"))) {
            Sword swordItem = (Sword) player.getInventory().stream().filter(i -> i.getType().equals("sword")).findFirst().orElse(null);
            sword = swordItem.getAttack();
            weaponryUsed.add(swordItem);

            swordItem.reduceDurability(player.getInventory());
        }

        // get ally damage
        if (player.getHasAlly()) {
            Mercenary merc = (Mercenary) player.getObservers().stream().filter(o -> ((MovingEntity) o) instanceof Mercenary).findFirst().orElse(null);
            ally = merc.getAllyAttack();
        }

        // get midnight armour damage
        if (player.getInventory().stream().anyMatch(i -> i.getType().equals("midnight_armour"))) {
            MidnightArmour mArmourItem = (MidnightArmour) player.getInventory().stream().filter(i -> i.getType().equals("midnight_armour")).findFirst().orElse(null);
            mArmourAtk = mArmourItem.getAttack();
            weaponryUsed.add(mArmourItem);
        }

        return -((bow * (base + sword + ally + mArmourAtk)) / 5);
    }

    /**
     *
     * @param player
     * @param enemy
     * @return the damage dealt by enemy
     */
    private double calculateEnemyAttack(Player player, MovingEntity enemy) {
        double base = enemy.getAttackDamage();
        double shield = 0;
        double ally = 0;
        double mArmourDef = 0;

        // get shield defence
        if (player.getInventory().stream().anyMatch(i -> i.getType().equals("shield"))) {
            Shield shieldItem = (Shield) player.getInventory().stream().filter(i -> i.getType().equals("shield")).findFirst().orElse(null);
            shield = shieldItem.getDefence();
            weaponryUsed.add(shieldItem);

            shieldItem.reduceDurability(player.getInventory());
        }

        // get ally defence
        if (player.getHasAlly()) {
            Mercenary merc = (Mercenary) player.getObservers().stream().filter(o -> ((MovingEntity) o).getType().equals("mercenary")).findFirst().orElse(null);
            ally = merc.getAllyDefence();
        }

        // get midnight armour defence
        if (player.getInventory().stream().anyMatch(i -> i.getType().equals("midnight_armour"))) {
            MidnightArmour mArmourItem = (MidnightArmour) player.getInventory().stream().filter(i -> i.getType().equals("midnight_armour")).findFirst().orElse(null);
            mArmourDef = mArmourItem.getDefence();
            weaponryUsed.add(mArmourItem);
        }

        if ((base - shield - ally - mArmourDef) <= 0) {
            return 0;
        } else {
            return -((base - shield - ally - mArmourDef) / 10);
        }
    }

    // Getters
    public double getDeltaPlayerHealth() { return deltaPlayerHealth;}
    public double getDeltaEnemyHealth()  { return deltaEnemyHealth;}
    public List<Item> getWeaponryUsed()  { return weaponryUsed;}
}
