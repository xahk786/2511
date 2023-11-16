package dungeonmania;


import org.json.JSONObject;
import dungeonmania.util.Position;
import dungeonmania.CollectableEntities.*;
import dungeonmania.BuildableEntities.*;
import dungeonmania.MovingEntities.*;
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
import dungeonmania.games.Game;

public class EntityFactory {
    private static int id = 0;

    public static Entity createEntity(JSONObject entity, JSONObject configs, Game game) {
        return useEntityFactory(entity, configs, game, EntityFactory.id += 1);
    }

    public static Entity createEntity(JSONObject entity, JSONObject configs, Game game, int id) {
        return useEntityFactory(entity, configs, game, id);
    }


    public static Entity useEntityFactory(JSONObject entity, JSONObject configs, Game game, int numId) {

        String type = entity.getString("type");
        int xCoord = entity.getInt("x");
        int yCoord = entity.getInt("y");

        String id = String.valueOf(numId);
        Position pos = new Position(xCoord, yCoord);

        switch (type) {

            case "old_player": {
                OldPlayer player = new OldPlayer(id, "old_player", pos, game);
                player.setHealth( configs.getDouble("player_health"));
                player.setMaxHealth( configs.getDouble("player_health"));
                player.setAttackDamage(configs.getDouble("player_attack"));
                return player;

            }
            case "player": {
                Player player =  new Player(id,"player", pos, game);
                player.setMaxHealth( configs.getDouble("player_health"));
                player.setHealth( configs.getDouble("player_health"));
                player.setAttackDamage(configs.getDouble("player_attack"));
                game.setPlayerEntity(player);
                return player;
            }

            case "wall": {
                return new Wall(id, pos);
            }

            case "exit": {
                return new Exit(id, pos);
            }
            case "boulder": {
                return new Boulder(id, pos);
            }

            case "switch": {
                return new FloorSwitch(id, pos);
            }

            case "door": {
                int keyId = entity.getInt("key");
                return new Door(id, pos, keyId);
            }

            case "portal": {
                String colour = entity.getString("colour");
                return new Portal(id, pos, colour);
            }

            case "zombie_toast_spawner": {
                ZombieToastSpawner spawner = new ZombieToastSpawner(id, pos);
                spawner.setSpawnRate(configs.getInt("zombie_spawn_rate"));
                return spawner;
            }

            case "spider": {
                // spiders dont care about player states. no need to add to observers list
                Spider spider =  new Spider(id, pos,game);
                spider.setHealth( configs.getDouble("spider_health"));
                spider.setMaxHealth( configs.getDouble("spider_health"));
                spider.setAttackDamage(configs.getDouble("spider_attack"));
                spider.setSpawnRate(configs.getInt("spider_spawn_rate"));
                return spider;
            }
            case "zombie_toast": {
                ZombieToast zombie = new ZombieToast(id,"zombie_toast", pos,game);
                zombie.setHealth(configs.getDouble("zombie_health"));
                zombie.setMaxHealth(configs.getDouble("zombie_health"));
                zombie.setAttackDamage(configs.getDouble("zombie_attack"));
                if (game.getPlayer() == null) {
                    return zombie;
                }
                game.getPlayer().attach(zombie); // add zombie to observers list
                game.getPlayer().notifyObserver();
                return zombie;
            }

            case "mercenary": {
                Mercenary merc = new Mercenary(id, "mercenary", pos,game);
                merc.setHealth( configs.getDouble("mercenary_health"));
                merc.setMaxHealth( configs.getDouble("mercenary_health"));
                merc.setAttackDamage(configs.getDouble("mercenary_attack"));
                merc.setBribeAmount(configs.getInt("bribe_amount"));
                merc.setBribeRadius(configs.getInt("bribe_radius"));

                if (configs.has("mind_control_duration")) {
                    merc.setMindControlDuration(configs.getInt("mind_control_duration"));
                } else {
                    merc.setMindControlDuration(0);
                }

                merc.setSpawnRate(0);

                merc.setAllyAttack(configs.getDouble("ally_attack"));
                merc.setAllyDefence(configs.getDouble("ally_defence"));
                if (game.getPlayer() == null) {
                    return merc;
                }
                game.getPlayer().attach(merc);
                game.getPlayer().notifyObserver();
                return merc;
            }

            case "assassin": {
                Assassin ass = new Assassin(id, pos, game);
                ass.setHealth(configs.getDouble("assassin_health"));
                ass.setMaxHealth(configs.getDouble("assassin_health"));
                ass.setAttackDamage(configs.getDouble("assassin_attack"));
                ass.setBribeAmount(configs.getInt("assassin_bribe_amount"));
                ass.setBribeRadius(configs.getInt("bribe_radius"));
                ass.setReconRadius(configs.getInt("assassin_recon_radius"));
                ass.setBribeFailRate(configs.getDouble("assassin_bribe_fail_rate"));
                ass.setAllyAttack(configs.getDouble("ally_attack"));
                ass.setAllyDefence(configs.getDouble("ally_defence"));
                ass.setMindControlDuration(configs.getInt("mind_control_duration"));
                ass.setSpawnRate(0);
                if (game.getPlayer() == null) {
                    return ass;
                }
                game.getPlayer().attach(ass);
                game.getPlayer().notifyObserver();
                return ass;
            }

            case "hydra": {
                Hydra hyd = new Hydra(id, pos, game);
                hyd.setHealth(configs.getDouble("hydra_health"));
                hyd.setMaxHealth(configs.getDouble("hydra_health"));
                hyd.setAttackDamage(configs.getDouble("hydra_attack"));
                hyd.setHealthIncreaseAmount(configs.getDouble("hydra_health_increase_amount"));
                hyd.setHealthIncreaseChance(configs.getDouble("hydra_health_increase_rate"));
                if (game.getPlayer() == null) {
                    return hyd;
                }
                game.getPlayer().attach(hyd); // add zombie to observers list
                game.getPlayer().notifyObserver();
                return hyd;
            }
            case "treasure": return new Treasure(id, pos);

            case "key": {
                int key = entity.getInt("key");
                return new Key(id, pos, key);
            }
            case "invincibility_potion": {
                int duration = configs.getInt("invincibility_potion_duration");
                return new InvincibilityPotion(id, pos, duration);
            }
            case "invisibility_potion": {
                int duration = configs.getInt("invisibility_potion_duration");
                return new InvisibilityPotion(id, pos, duration);
            }
            case "wood": return new Wood(id, pos);

            case "arrow": return new Arrows(id, pos);

            case "bomb": {
                int bomb_radius = configs.getInt("bomb_radius");
                return new Bomb(id, pos, bomb_radius);
            }
            case "sword": {
                int attack = configs.getInt("sword_attack");
                int durability = configs.getInt("sword_durability");
                return new Sword(id, pos, attack, durability);
            }
            case "bow": {
                int durability = configs.getInt("bow_durability");
                return new Bow(id, pos, durability);
            }
            case "shield": {
                int shield_defence = configs.getInt("shield_defence");
                int shield_durability = configs.getInt("shield_durability");
                return new Shield(id, pos, shield_defence, shield_durability);
            }

            case "sceptre": {
                int duration = configs.getInt("mind_control_duration");
                return new Sceptre(id, pos, duration);
            }

            case "sun_stone": {
                return new SunStone(id, pos);
            }

            case "midnight_armour": {
                int attack = configs.getInt("midnight_armour_attack");
                int defence = configs.getInt("midnight_armour_defence");
                return new MidnightArmour(id, pos, attack, defence);
            }

            case "time_turner": {
                return new TimeTurner(id, pos);
            }

            case "time_travelling_portal": {
                return new TimeTravellingPortal(id, type, pos, false, true);
            }
            case "swamp_tile": {
                return new SwampTile(id, pos, entity.getInt("movement_factor"));
            }

            case "light_bulb_on": {
                return new LightBulb(id, type, pos, entity.getString("logic"));
            }

            case "light_bulb_off": {
                return new LightBulb(id, type, pos, entity.getString("logic"));
            }

            case "switch_door": {
                return new SwitchDoor(id, pos, entity.getString("logic"));
            }

            case "wire": {
                return new Wire(id, pos);
            }

            default:
                return null;
        }
    }

    public static void resetEntityIds() {
            id = 0;
    }
}
