package dungeonmania.persistence.load;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Entity;
import dungeonmania.EntityFactory;
import dungeonmania.CollectableEntities.Consumable;
import dungeonmania.CollectableEntities.InvincibilityPotion;
import dungeonmania.CollectableEntities.Sword;
import dungeonmania.CollectableEntities.*;
import dungeonmania.BuildableEntities.Bow;
import dungeonmania.BuildableEntities.Sceptre;
import dungeonmania.BuildableEntities.Shield;
import dungeonmania.BuildableEntities.MidnightArmour;
import dungeonmania.MoveStates.CircularState;
import dungeonmania.MoveStates.FollowPlayerState;
import dungeonmania.MoveStates.RandomMovementState;
import dungeonmania.MoveStates.RunAwayState;
import dungeonmania.MoveStates.TailPlayerState;
import dungeonmania.MovingEntities.Mercenary;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.Player;
import dungeonmania.StaticEntities.Door;
import dungeonmania.StaticEntities.FloorSwitch;
import dungeonmania.games.Game;
import dungeonmania.util.Direction;
import dungeonmania.util.Position;

public class EntitiesFromJSON {
    public static ArrayList<Entity> createEntitiesFromJSON(JSONArray entitiesJSON, JSONObject configFile, Game game) {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for (int i = 0; i < entitiesJSON.length(); i++) {
            entities.add(createEntityFromJSON(entitiesJSON.getJSONObject(i), configFile, game));
        }
        return entities;
    }

    public static Entity createEntityFromJSON(JSONObject entityJSON, JSONObject configFile, Game game) {
        int id = entityJSON.getInt("id");
        Entity entity = EntityFactory.createEntity(entityJSON, configFile, game, id);
        entity.setSpawnPosition(createPositionFromJSON(entityJSON.getJSONObject("spawnPosition")));
        entity.setIsPassable(entityJSON.getBoolean("isPassable"));

        String type = entityJSON.getString("type");
        switch (type) {
            // static entities
            case "wall": {
                return entity;
            }
            case "exit": {
                return entity;
            }
            case "boulder": {
                return entity;
            }
            case "switch": {
                ((FloorSwitch) entity).setBoulderOnSwitch(entityJSON.getBoolean("isBoulderonSwitch"));
                return entity;
            }
            case "door": {
                ((Door) entity).setIsLocked(entityJSON.getBoolean("isLocked"));
                return entity;
            }
            case "portal": {
                return entity;
            }
            case "zombie_toast_spawner": {
                return entity;
            }
            // to do - below
            case "time_travelling_portal": {
                return entity;
            }
            case "swamp_tile": {
                return entity;
            }
            case "wire": {
                return entity;
            }
            case "switch_door": {
                return entity;
            }
            case "light_bulb_off": {
                return entity;
            }
            case "light_bulb_on": {
                return entity;
            }

            // Collectible Entities
            case "treasure": {
                return entity;
            }
            case "key": {
                //entity factory already sets keyid?
                return entity;
            }
            case "invincibility_potion": {
                ((InvincibilityPotion) entity).setDuration(entityJSON.getInt("duration"));
                //entity factory already sets player?
                return entity;
            }
            case "invisibility_potion": {
                ((InvisibilityPotion) entity).setDuration(entityJSON.getInt("duration"));
                //entity factory already sets player?
                return entity;
            }
            case "wood": {
                return entity;
            }
            case "arrow": {
                return entity;
            }
            case "bomb": {
                //entity factory already sets bomb radius?
                return entity;
            }
            case "sword": {
                ((Sword) entity).setDurability(entityJSON.getInt("durability"));
                ((Sword) entity).setAttack(entityJSON.getInt("attack"));

                return entity;
            }
            case "sun_stone": {
                return entity;
            }
            case "time_turner": {
                return entity;
            }

            // Buildable Entities
            case "bow": {
                ((Bow) entity).setDurability(entityJSON.getInt("durability"));
                return entity;
            }
            case "shield": {
                ((Shield) entity).setDurability(entityJSON.getInt("durability"));
                ((Shield) entity).setDefence(entityJSON.getInt("defence"));
                return entity;
            }
            case "sceptre": {
                ((Sceptre) entity).setDuration(entityJSON.getInt("mindControlDuration"));
                return entity;
            }
            case "midnight_armour": {
                ((MidnightArmour) entity).setAttack(entityJSON.getInt("midnight_armour_attack"));
                ((MidnightArmour) entity).setDefence(entityJSON.getInt("midnight_armour_defence"));
                return entity;
            }
        }

        // Must be Moving Entity remaining after first switch case statement
        MovingEntity movingEntity = (MovingEntity) entity;
        movingEntity.setHealth(entityJSON.getDouble("health"));
        movingEntity.setAttackDamage(entityJSON.getDouble("attackDamage"));
        movingEntity.setIsReversedDirection(entityJSON.getBoolean("isReversedDirection"));
        movingEntity.setGame(game);
        String state = entityJSON.getString("movementState");
        switch (state) {
            case "circular": movingEntity.setMovementState(new CircularState(movingEntity)); break;
            case "follow_player": movingEntity.setMovementState(new FollowPlayerState(movingEntity, game)); break;
            case "random": movingEntity.setMovementState(new RandomMovementState(movingEntity, game)); break;
            case "run_away": movingEntity.setMovementState(new RunAwayState(movingEntity, game)); break;
            case "tail_player": movingEntity.setMovementState(new TailPlayerState(movingEntity, game)); break;
        }

        switch (type) {
            // Moving Entities
            case "player": {
                Player player = (Player) entity;
                player.setIsPlayerIdle(entityJSON.getBoolean("isPlayerIdle"));
                player.setPlayerPrevPos(createPositionFromJSON(entityJSON.getJSONObject("playerPrevPos")));
                player.setKillCount(entityJSON.getInt("killCount"));
                player.setBattles(BattlesFromJSON.createBattlesFromJSON(entityJSON.getJSONArray("battles"), game));
                player.setInventory(ItemsFromJSON.createItemsFromJSON(entityJSON.getJSONArray("inventory"), game));
                //setListObservers(player, entityJSON.getJSONArray("listObservers"), game.getEntityList());
                player.setPotionQueue(createPotionQueueFromJSON(entityJSON.getJSONArray("potionQueue"), game));
                player.setHasAlly(entityJSON.getBoolean("hasAlly"));
                player.setIsInvisible(entityJSON.getBoolean("isInvisible"));
                player.setIsInvincible(entityJSON.getBoolean("isInvincible"));
                setPlayerDirection(player, entityJSON.getString("playerDirection"));

                return player;
            }

            case "spider": {
                return entity;
            }
            case "zombie_toast": {
                return entity;
            }
            case "mercenary": {
                ((Mercenary) entity).setIsBribed(entityJSON.getBoolean("isBribed"));
                return entity;
            }
            // to do - below
            case "assassin": {
                return entity;
            }
            case "hydra": {
                return entity;
            }

            case "older_player": {
                return entity;
            }
        }

        return entity;
    }

    public static Position createPositionFromJSON(JSONObject posJSON) {
        return new Position(posJSON.getInt("x"), posJSON.getInt("y"), posJSON.getInt("z"));
    }

    public static ArrayList<Consumable> createPotionQueueFromJSON(JSONArray queueJSON, Game game) {
        ArrayList<Consumable> queue = new ArrayList<Consumable>();
        for (int i = 0; i < queueJSON.length(); i++) {
            queue.add((Consumable) ItemsFromJSON.createItemFromJSON(queueJSON.getJSONObject(i), game));
        }
        return queue;
    }

    public static void setPlayerDirection(Player player, String direction) {
        switch (direction) {
            case "UP": player.setPlayerDirection(Direction.UP); break;
            case "DOWN": player.setPlayerDirection(Direction.DOWN); break;
            case "LEFT": player.setPlayerDirection(Direction.LEFT); break;
            case "RIGHT": player.setPlayerDirection(Direction.RIGHT); break;
        }
    }

}
