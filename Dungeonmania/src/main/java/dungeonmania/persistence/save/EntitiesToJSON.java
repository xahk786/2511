package dungeonmania.persistence.save;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Entity;
import dungeonmania.Item;
import dungeonmania.BuildableEntities.Bow;
import dungeonmania.BuildableEntities.MidnightArmour;
import dungeonmania.BuildableEntities.Sceptre;
import dungeonmania.BuildableEntities.Shield;
import dungeonmania.CollectableEntities.Arrows;
import dungeonmania.CollectableEntities.Bomb;
import dungeonmania.CollectableEntities.Consumable;
import dungeonmania.CollectableEntities.InvincibilityPotion;
import dungeonmania.CollectableEntities.InvisibilityPotion;
import dungeonmania.CollectableEntities.Key;
import dungeonmania.CollectableEntities.SunStone;
import dungeonmania.CollectableEntities.Sword;
import dungeonmania.CollectableEntities.TimeTurner;
import dungeonmania.CollectableEntities.Treasure;
import dungeonmania.CollectableEntities.Wood;
import dungeonmania.MovingEntities.Mercenary;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.ObserverEnemy;
import dungeonmania.MovingEntities.Player;
import dungeonmania.MovingEntities.Spider;
import dungeonmania.MovingEntities.ZombieToast;
import dungeonmania.StaticEntities.Boulder;
import dungeonmania.StaticEntities.Door;
import dungeonmania.StaticEntities.Exit;
import dungeonmania.StaticEntities.FloorSwitch;
import dungeonmania.StaticEntities.LightBulb;
import dungeonmania.StaticEntities.Portal;
import dungeonmania.StaticEntities.SwampTile;
import dungeonmania.StaticEntities.SwitchDoor;
import dungeonmania.StaticEntities.TimeTravellingPortal;
import dungeonmania.StaticEntities.Wall;
import dungeonmania.StaticEntities.Wire;
import dungeonmania.StaticEntities.ZombieToastSpawner;
import dungeonmania.util.Position;

public class EntitiesToJSON {
    public static JSONArray createEntitiestoJSON(List<Entity> entities) {
        JSONArray entitiesJSON = new JSONArray();
        for (Entity entity : entities) {
            entitiesJSON.put(createEntitytoJSON(entity));
        }
        return entitiesJSON;
    }

    public static JSONObject createEntitytoJSON(Entity entity) {
        JSONObject entityJSON = new JSONObject();
        entityJSON.put("id", entity.getId());
        entityJSON.put("type", entity.getType());
        entityJSON.put("x", entity.getPosition().getX());
        entityJSON.put("y", entity.getPosition().getY());
        entityJSON.put("z", entity.getPosition().getLayer());
        entityJSON.put("spawnPosition", createPositionToJSON(entity.getSpawnPosition()));
        entityJSON.put("isPassable", entity.getIsPassable());

        // Static Entities
        if (entity instanceof Wall) {
            return entityJSON;
        }
        if (entity instanceof Exit) {
            return entityJSON;
        }
        if (entity instanceof Boulder) {
            return entityJSON;
        }
        if (entity instanceof FloorSwitch) {
            entityJSON.put("isBoulderonSwitch", ((FloorSwitch) entity).isBoulderonSwitch());
            return entityJSON;
        }
        if (entity instanceof Door) {
            Door door = (Door) entity;
            entityJSON.put("key", door.getKeyId());
            entityJSON.put("isLocked", door.getIsLocked());
            return entityJSON;
        }
        if (entity instanceof Portal) {
            entityJSON.put("colour", ((Portal) entity).getColour());
            return entityJSON;
        }
        if (entity instanceof ZombieToastSpawner) {
            entityJSON.put("zombie_spawn_rate", ((ZombieToastSpawner) entity).getSpawnRate());
            return entityJSON;
        }
        // Do for Time Travelling Portal
        if (entity instanceof TimeTravellingPortal) {
            return entityJSON;
        }
        // Do for Swamp Tile
        if (entity instanceof SwampTile) {
            entityJSON.put("movement_factor", ((SwampTile) entity).getMovement_factor());
            return entityJSON;
        }
        // Wire
        if (entity instanceof Wire) {
            entityJSON.put("state", ((Wire) entity).getState());
            entityJSON.put("visited", ((Wire) entity).getVisited());
            return entityJSON;
        }
        // Switch Door
        if (entity instanceof SwitchDoor) {
            SwitchDoor door = (SwitchDoor) entity;
            entityJSON.put("isLocked", door.getIsLocked());
            return entityJSON;
        }
        // Light bulb
        if (entity instanceof LightBulb) {
            return entityJSON;
        }

        // Moving Entities
        if (entity instanceof MovingEntity) {
            MovingEntity movingEntity = (MovingEntity) entity;
            entityJSON.put("health", movingEntity.getHealth());
            entityJSON.put("attackDamage", movingEntity.getAttackDamage());
            entityJSON.put("isReversedDirection", movingEntity.getIsReversedDirection());
            if (movingEntity.getMovementState() == null) {
                entityJSON.put("movementState", "");
            } else {
                entityJSON.put("movementState", movingEntity.getMovementState().getType());
            }
            if (entity instanceof Player) {
                Player player = (Player) entity;
                entityJSON.put("isPlayerIdle", player.getIsplayerIdle());
                entityJSON.put("playerPrevPos", createPositionToJSON(player.getPlayerPrevPos()));
                entityJSON.put("killCount", player.getKillCount());
                entityJSON.put("battles", BattlesToJSON.createBattlesToJSON(player.getBattles()));
                entityJSON.put("inventory", ItemsToJSON.createItemsToJSON(player.getInventory()));
                entityJSON.put("listObservers", createObserversToJSON(player.getObservers()));
                entityJSON.put("potionQueue", createPotionQueueToJSON(player.getPotionQueue()));
                entityJSON.put("hasAlly", player.getHasAlly());
                entityJSON.put("isInvincible", player.getIsInvincible());
                entityJSON.put("isInvisible", player.getIsInvisible());
                if (player.getPlayerDirection() == null) {
                    entityJSON.put("playerDirection", "");
                } else {
                    entityJSON.put("playerDirection", player.getPlayerDirection().toString());
                }
                return entityJSON;
            }

            if (entity instanceof Spider) {
                return entityJSON;
            }
            if (entity instanceof ZombieToast) {
                return entityJSON;
            }
            if (entity instanceof Mercenary) {
                Mercenary merc = (Mercenary) entity;
                entityJSON.put("isBribed", merc.getIsBribed());

                return entityJSON;
            }
            // Assassin
            //if (entity instanceof Wall) {
            //
            //}
            // Hydra
            //if (entity instanceof Wall) {
            //
            //}
        }

        // Collectable Entities
        if (entity instanceof Treasure) {
            //nothing to do
            return entityJSON;
        }
        if (entity instanceof Key) {
            entityJSON.put("key", ((Key) entity).getKey());
            return entityJSON;
        }
        if (entity instanceof InvincibilityPotion){
            entityJSON.put("duration", ((InvincibilityPotion) entity).getDuration());
            return entityJSON;
        }

        if (entity instanceof InvisibilityPotion){
            entityJSON.put("duration", ((InvisibilityPotion) entity).getDuration());
            return entityJSON;
        }

        if (entity instanceof Wood) {
            //nothing to do
            return entityJSON;
        }
        if (entity instanceof Arrows) {
            //nothing to do
            return entityJSON;
        }
        if (entity instanceof Bomb) {
            entityJSON.put("bombRadius", ((Bomb) entity).getBombRadius());
            return entityJSON;
        }
        if (entity instanceof Sword) {
            entityJSON.put("attack", ((Sword) entity).getAttack());
            entityJSON.put("durability", ((Sword) entity).getDurability());
            return entityJSON;
        }

        if (entity instanceof SunStone) {
            //nothing to do
            return entityJSON;
        }

        if (entity instanceof TimeTurner) {
            //nothing to do
            return entityJSON;
        }


        // Buildable Entities
        if (entity instanceof Bow) {
            entityJSON.put("durability", ((Bow) entity).getDurability());
            return entityJSON;
        }
        if (entity instanceof Shield) {
            entityJSON.put("durability", ((Shield) entity).getDurability());
            entityJSON.put("defence", ((Shield) entity).getDefence());
            return entityJSON;
        }

        if (entity instanceof Sceptre) {
            entityJSON.put("mindControlDuration", ((Sceptre) entity).getDuration());
            return entityJSON;
        }
        // Midnight Armour
        if (entity instanceof MidnightArmour) {
            entityJSON.put("midnight_armour_attack", ((MidnightArmour) entity).getAttack());
            entityJSON.put("midnight_armour_defence", ((MidnightArmour) entity).getDefence());
            return entityJSON;
        }


        return entityJSON;
    }

    public static JSONObject createPositionToJSON(Position pos) {
        JSONObject posJSON = new JSONObject();
        posJSON.put("x", pos.getX());
        posJSON.put("y", pos.getY());
        posJSON.put("z", pos.getLayer());

        return posJSON;
    }

    public static JSONArray createObserversToJSON(List<ObserverEnemy> observers) {
        JSONArray observersJSON = new JSONArray();
        for (ObserverEnemy observerEnemy : observers) {
            observersJSON.put(new JSONObject("id", observerEnemy.getId()));
        }
        return observersJSON;
    }

    public static JSONArray createPotionQueueToJSON(List<Consumable> potionQueue) {
        JSONArray queueJSON = new JSONArray();
        for (Consumable potion : potionQueue) {
            Item item = (Item) potion;
            queueJSON.put(ItemsToJSON.createItemToJSON(item));
        }
        return queueJSON;
    }
}
