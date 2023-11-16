package dungeonmania;

import java.util.ArrayList;
import java.util.Arrays;
// import java.util.Arrays;
import java.util.List;

import org.json.JSONObject;

import dungeonmania.response.models.AnimationQueue;
import dungeonmania.response.models.BattleResponse;
import dungeonmania.response.models.EntityResponse;
import dungeonmania.response.models.ItemResponse;
import dungeonmania.util.Position;
import dungeonmania.response.models.RoundResponse;
import dungeonmania.Battles.Battle;
import dungeonmania.Battles.Round;
import dungeonmania.MovingEntities.Assassin;
import dungeonmania.MovingEntities.Hydra;
import dungeonmania.MovingEntities.Mercenary;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.Player;
import dungeonmania.MovingEntities.Spider;
import dungeonmania.MovingEntities.ZombieToast;
import dungeonmania.StaticEntities.FloorSwitch;
import dungeonmania.games.Game;

public class Helper {

    public static boolean isValidBuild(String buildable){
        return buildable.equals("bow")
            || buildable.equals("shield")
            || buildable.equals("sceptre")
            || buildable.equals("midnight_armour");
    }

    public static Player getPlayer(ArrayList<Entity> entities){
        return (Player) entities.stream().filter(e -> e.getType().equals("player")).findFirst().orElse(null);
    }

    public static List<EntityResponse> convertEntitiesToResponse(ArrayList<Entity> entities) {
        List<EntityResponse> entityResponses = new ArrayList<EntityResponse>();

        for (Entity entity : entities) {
            entityResponses.add(new EntityResponse(entity.getId(), entity.getType(), entity.getPosition(), entity.getIsInteractable()));
        }

        return entityResponses;
    }

    public static List<ItemResponse> convertInventoryToResponse(Entity player){
        List<ItemResponse> inventoryResponse = new ArrayList<ItemResponse>();

        List<Item> inventory = ((Player) player).getInventory();
        for (Item i : inventory){
            inventoryResponse.add(new ItemResponse(i.getId(), i.getType()));
        }

        return inventoryResponse;
    }

    public static List<String> convertBuildablesToResponse(Entity player){
        List<String> buildables = new ArrayList<String>();

        if (((Player) player).canCraftBow()){
            buildables.add("bow");
        }

        if (!((Player) player).canCraftShield().equals("")){
            buildables.add("shield");
        }

        return buildables;
    }

    public static boolean isUsableItem(Item item){
        return item.getType().equals("bomb")
            || item.getType().equals("invincibility_potion")
            || item.getType().equals("invisibility_potion");
    }

    /*
     * returns the floor switch next to the given bomb position
     */
    public static FloorSwitch switchNextToBomb(ArrayList<Entity> entities, Position bombPos){
        FloorSwitch fs = null;

        Object[] arr = entities.stream().filter(x -> x.getType().equals("switch")).toArray();

        for (Object i : arr){
            Position switchPos = ((FloorSwitch) i).getPosition();
            if (isCardinal(switchPos, bombPos)){
                return ((FloorSwitch) i);
            }

        }
        return fs;
    }

    public static boolean isCardinal(Position pos1, Position pos2){

        //up or down
        if (Math.abs(pos1.getX() - pos2.getX()) == 1 && pos1.getY() - pos2.getY() == 0){
            return true;
        }

        //left or right
        if (pos1.getX() - pos2.getX() == 0 && Math.abs(pos1.getY() - pos2.getY()) == 1){
            return true;
        }

        return false;
    }

    public static List<BattleResponse> convertBattlesToResponse(Entity player) {
        List<BattleResponse> battlesResponse = new ArrayList<BattleResponse>();

        List<Battle> battles = ((Player) player).getBattles();
        for (Battle battle : battles) {
            List<RoundResponse> roundsResponse = getRoundsResponse(battle.getRounds());
            battlesResponse.add(new BattleResponse(battle.getEnemyType(), roundsResponse, battle.getInitialPlayerHealth(), battle.getInitialEnemyHealth()));
        }

        return battlesResponse;
    }

    private static List<RoundResponse> getRoundsResponse (List<Round> rounds) {
        List<RoundResponse> roundsResponse = new ArrayList<RoundResponse>();

        for (Round round : rounds) {
            List<ItemResponse> itemsResponse = getItemsResponse(round.getWeaponryUsed());
            roundsResponse.add(new RoundResponse(round.getDeltaPlayerHealth(), round.getDeltaEnemyHealth(), itemsResponse));
        }

        return roundsResponse;
    }

    private static List<ItemResponse> getItemsResponse (List<Item> weaponryUsed) {
        List<ItemResponse> itemsResponse = new ArrayList<ItemResponse>();

        for (Item item : weaponryUsed) {
            itemsResponse.add(new ItemResponse(item.getId(), item.getType()));
        }
        return itemsResponse;
    }

    /*
     * generate a tick that would occur in cases were nothing would occur:
     * e.g movement block, itemused exception
     */
    public static void doGeneralTick(Game game, JSONObject configFile){
        List<Entity> copy = new ArrayList<>(game.getEntityList());
        for (Entity entity : copy) {
            if (entity instanceof tickable) {
                ((tickable) entity).tick(game);
            }
        }

        Spider.spawn(game, configFile.getInt("spider_spawn_rate"));

        // if player is using a potion, decrease its duration
        if (!game.getPlayer().getPotionQueue().isEmpty()) {
            Helper.getPlayer(game.getEntityList()).getPotionQueue().get(0).reduceDuration();
        }
    }

    public static boolean isObserver(Entity entity) {
        return (entity instanceof Mercenary
             || entity instanceof ZombieToast
             || entity instanceof Hydra
             || entity instanceof Assassin) ? true : false;
    }

    //Animation Helpers
    public static dungeonmania.response.models.AnimationQueue setHealthBar(Entity entity){
        double curr_hp = ((MovingEntity) entity).getHealth();
        double max_hp = ((MovingEntity) entity).getMaxHealth();

        System.out.println(curr_hp);
        System.out.println(max_hp);

        if (curr_hp == max_hp){
            return new AnimationQueue("PostTick", entity.getId(), Arrays.asList(
                "healthbar set 1", "healthbar tint 0x00ff00"), true, -1);
        }
        else {
            String set = "healthbar set " + Double.toString(curr_hp/max_hp) + ", "  + "over 1.5s";
            return new AnimationQueue("PostTick", entity.getId(), Arrays.asList(
                "healthbar set 1", "healthbar tint 0x00ff00", set, "healthbar tint 0xff0000, over 0.5s"
            ), false, -1);
        }

    }

    public static void addAnimation(Game game){
        game.getEntityList().stream().filter(x -> x instanceof MovingEntity).forEach(x -> game.getAnimations().add(Helper.setHealthBar(x)));
    }

}
