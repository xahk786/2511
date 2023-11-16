package dungeonmania.games;

import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.goals.Goal;
import dungeonmania.goals.GoalFactory;
import dungeonmania.persistence.load.GameFromJSON;
import dungeonmania.persistence.save.GameToJSON;
import dungeonmania.persistence.save.ItemsToJSON;
import dungeonmania.response.models.AnimationQueue;
import dungeonmania.response.models.BattleResponse;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.response.models.EntityResponse;
import dungeonmania.response.models.ItemResponse;
import dungeonmania.util.Direction;
import dungeonmania.util.FileLoader;
import dungeonmania.util.Position;
import dungeonmania.DungeonManiaController;
import dungeonmania.Entity;
import dungeonmania.EntityFactory;
import dungeonmania.Helper;
import dungeonmania.Item;
import dungeonmania.tickable;
import dungeonmania.CollectableEntities.Bomb;
import dungeonmania.CollectableEntities.Consumable;
import dungeonmania.MovingEntities.OldPlayer;
import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.MovingEntities.ObserverEnemy;
import dungeonmania.MovingEntities.Player;
import dungeonmania.MovingEntities.Spider;
import dungeonmania.StaticEntities.FloorSwitch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

public class Game {
    private boolean rewinded = false;
    public boolean isRewinded() {
        return rewinded;
    }
    public void setRewinded(boolean rewinded) {
        this.rewinded = rewinded;
    }

    private DungeonManiaController dungeon;
    private String gameId;
    private OldPlayer impostPlayer = null;
    private String dungeonName;
    private String configName;
    private int tickCounter = 0;
    private Goal rootGoal;
    private Player playerEntity;
    private ArrayList<Entity> entities = new ArrayList<Entity>();
    private List<JSONObject> gameStateHistory = new ArrayList<>(); // JSON Object of game history (all ticks from 0)
    private JSONObject configFile = new JSONObject(); //gloabl config file to parse in
    //animation stuff


    private List<AnimationQueue> animations = new ArrayList<AnimationQueue>();

    // this is probablbty the worst way to do it but
    private HashMap<Integer, Direction> playerMovesHistory = new HashMap<>();
    private HashMap<Integer, String> tickItemHistory =new HashMap<>();
    private HashMap<Integer, List<String>> buildHistory = new HashMap<>();
    private HashMap<Integer, List<String>> interactHistory = new HashMap<>();


    public Game() {}

    public Game(String gameId, String dungeonName, String configName, int tickCounter,
            JSONObject configFile, JSONObject goals) {
        this.gameId = gameId;
        this.dungeonName = dungeonName;
        this.configName = configName;
        this.tickCounter = tickCounter;
        this.configFile = configFile;
        this.rootGoal = GoalFactory.createGoal(goals, this);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                             TIME TRAVEL                                          //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    // all ticks(gamestates) are record: absolutely everything and stored in an array.
    // index history[0] is the game when its created at tick 0
    // when rewinding, we go to the tick destination and loop to the tick that
    // all the entities should exist as it is and move in the same way
    // the OLD PLAYER also becomes an enemy that you can fight
    // if you kill your old self, the old self in the remaining ticks should also disappear
    // INVENTORY PERSISTS. WHEN TIME TRAVELLING YOU KEEP YOUR CURRENT INVENTORY
    //

    // not saving game for each tick bc no need to save file  locally

    /**
     * time travels backwards only for the requested amount of ticks
     * should only be called once when the rewind itself occurs
     * it stores all the relevant game states into a timetravel array
     *
     * ASSUME THERES NO TIME TRAVEL WITHIN TIME TRAVEL
     * but you can time travel back to the time you time travelled and there will be multiple imposter players
     *
     */

    public void rewind(int ticks) {
        if (ticks <= 0) throw new IllegalArgumentException();

        int tickDestIndex = tickCounter - ticks; // it arrives one tick BEFORE the des tick
        if (tickDestIndex <= 0) tickDestIndex = 0;


        JSONObject revertedGameState = gameStateHistory.get(tickDestIndex);
        Game revertedGame = GameFromJSON.createGameFromJSON(revertedGameState);

        Player oldPlayer = revertedGame.getPlayer();
        // set imposter player to old players stats at the time
        OldPlayer imposterPlayer = (OldPlayer) newEntity("old_player", oldPlayer.getPosition());

        imposterPlayer.setPosition(oldPlayer.getPosition());
        imposterPlayer.setHealth(oldPlayer.getHealth());
        imposterPlayer.setIsInvincible(oldPlayer.getIsInvincible());
        imposterPlayer.setIsInvisible(oldPlayer.getIsInvisible());
        imposterPlayer.setAttackDamage(oldPlayer.getAttackDamage());
        imposterPlayer.setPotionQueue(oldPlayer.getPotionQueue());
        imposterPlayer.setInventory(oldPlayer.getInventory());

        // imposter players future steps and actions as well as death time
        imposterPlayer.setCurrTick(tickDestIndex);
        imposterPlayer.setDeathTick(tickCounter);
        imposterPlayer.setPlayerMovesHistory(playerMovesHistory);
        imposterPlayer.setTickItemHistory(tickItemHistory);
        imposterPlayer.setInteractHistory(interactHistory);
        imposterPlayer.setBuildHistory(buildHistory);

        setImpostPlayer(imposterPlayer);
        Player currentPlayer = this.getPlayer();
        // add current player + new imposter player from current game, end game respectively, to the revertedGame
        revertedGame.addEntitiy(currentPlayer);
        revertedGame.addEntitiy(impostPlayer);
        revertedGame.removeEntity(revertedGame.getPlayer());
        // dont forget to reset reverted games imposter and current player.
        revertedGame.setPlayerEntity(currentPlayer);
        revertedGame.setImpostPlayer(impostPlayer);

        // keep old moves? even though its undefined to travel twice....?
        revertedGame.setPlayerMovesHistory(playerMovesHistory);
        revertedGame.setTickItemHistory(tickItemHistory);
        revertedGame.setInteractHistory(interactHistory);
        revertedGame.setBuildHistory(buildHistory);
        // keep game state history because
        revertedGame.setGameStateHistory(gameStateHistory);
        revertedGame.setTickCounter(tickCounter);
        revertedGame.setDungeon(dungeon);

        // inside tick item. make sure to set the current game to this reverted game.

        dungeon.setCurrentGame(revertedGame);

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                             GAME                                                 //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * /game/new
     */

    /*
     * check dungeonName is in dungeons()
     * load dungeon with FileLoader.loadResourceFile("/dungeons/" + dungeonName + ".json") <-- returns string of "[entities], goal-condition"
     * Turn string into list of entities and into goals
     *
     * types "bow" and "shield" should not be added to entities list (Should be added to inventory list) (Spec 4.1)
     */
    public DungeonResponse newGame(String gameName, String dungeonName, String configName) {
        // clear everything from previous dungeons
        EntityFactory.resetEntityIds();
        this.dungeonName = dungeonName;
        this.gameId = gameName;

        try {
            this.configName = configName;
            String fileString = FileLoader.loadResourceFile("/dungeons/" + dungeonName + ".json");
            String configString = FileLoader.loadResourceFile("/configs/" + configName + ".json");

            JSONObject file = new JSONObject(fileString);
            JSONObject configs = new JSONObject(configString);
            configFile = configs;

            // get entities
            JSONArray entityList = file.getJSONArray("entities");

            for (int i = 0; i < entityList.length(); i++) {
                JSONObject object = entityList.getJSONObject(i);

                Entity entity = EntityFactory.createEntity(object, configs, this);

                //when we load any moving entity in, we set its health to max specified with a green colour.
                if (entity instanceof MovingEntity){
                    //animations.add(Helper.setHealthBar(entity));
                }

                entities.add(entity);
            }

            // attach all observers to player
            for (Entity entity : entities) {
                if (Helper.isObserver(entity)) {
                    playerEntity.attach((ObserverEnemy) entity);
                    playerEntity.notifyObserver();
                }
            }

            // get goals
            JSONObject goals = file.getJSONObject("goal-condition");
            rootGoal = GoalFactory.createGoal(goals, this);

            // return DungeonResponse
            List<EntityResponse> entitiesResponse = Helper.convertEntitiesToResponse(entities);
            List<ItemResponse> inventoryResponse = Helper.convertInventoryToResponse(playerEntity);
            List<BattleResponse> battlesResponse = new ArrayList<BattleResponse>();
            List<String> buildables = Helper.convertBuildablesToResponse(playerEntity);
            String goalString = rootGoal.getStartGoalString();

            //// create a JSON copy of gamestate and store it in history
            gameStateHistory.add(GameToJSON.createGameToJSON(this));

            return new DungeonResponse(gameId, dungeonName, entitiesResponse, inventoryResponse, battlesResponse, buildables, goalString, animations);
        }

        catch(IOException e) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * /game/dungeonResponseModel
     */
    public DungeonResponse getDungeonResponseModel() {
        String dName = this.dungeonName;
        List<EntityResponse> entitiesResponse = Helper.convertEntitiesToResponse(entities);
        List<ItemResponse> inventory = Helper.convertInventoryToResponse(playerEntity);
        List<BattleResponse> battles = Helper.convertBattlesToResponse(playerEntity);
        List<String> buildables = Helper.convertBuildablesToResponse(playerEntity);
        String goals = rootGoal.getGoalString(entities);

        return new DungeonResponse(gameId, dName, entitiesResponse, inventory, battles, buildables, goals, animations);
    }

    public void tickImposter() {
        try {
            impostPlayer.retraceSteps(this);
        }
        catch(Exception e) {
            new InvalidActionException("");
        } finally {
            if (impostPlayer != null && !impostPlayer.getPotionQueue().isEmpty()) {
                impostPlayer.getPotionQueue().get(0).reduceDuration();
            }
        }
    }

    /**
     * /game/tick/item
     */
    public DungeonResponse tick(String itemUsedId) throws IllegalArgumentException, InvalidActionException {

        tickItemHistory.put(tickCounter, itemUsedId);
        tickCounter++;

        this.getPlayer().setPlayerDirection(null); // if not moving and using item/interacting, direction is null

        if (playerEntity.getItemViaId(itemUsedId) == null){
            Helper.doGeneralTick(this, configFile);
            tickImposter();
            throw new InvalidActionException("Cannot use item, not in players inventory");
        }
        if (!Helper.isUsableItem(playerEntity.getItemViaId(itemUsedId))){
            Helper.doGeneralTick(this, configFile);
            tickImposter();
            throw new IllegalArgumentException();
        }

        attemptUseItem(itemUsedId, this.getPlayer());
        Helper.doGeneralTick(this, configFile);
        tickImposter();
        // if Player dies in battle, remove from map
        if (!entities.contains(playerEntity)) { return endGame();}
        Helper.addAnimation(this);
        return getDungeonResponseModel();
    }

    // need to pass in a player cause it might be the old player using an item
    public void attemptUseItem(String itemUsedId, Player player) {
        Item item = player.getItemViaId(itemUsedId);

        // consumes a potion //remove item from player's inventory
        if (item instanceof Consumable) {
            player.removeItem(itemUsedId);
            Consumable potion = (Consumable) item;
            potion.consume(player);
        }

        if (item.getType().equals("bomb")){
            ((Bomb) item).plantBomb(this);

            FloorSwitch fs = Helper.switchNextToBomb(entities, item.getPosition());
            if (fs != null){
                if (fs.isTriggered(this)){
                    ((Bomb) item).blowUp(entities);
                }
            }
        }
    }


    /**
     * /game/tick/movement
     */
    public DungeonResponse tick(Direction movementDirection) {

        playerMovesHistory.put(tickCounter, movementDirection);

        // tick starts at 1
        playerEntity.setIsPlayerIdle(false);
        tickCounter++;
        playerEntity.setPlayerDirection(movementDirection);
        playerEntity.setPlayerPrevPos(playerEntity.getPosition());

        // the player should move first since the merc will "follow" if after it makes it first move
        playerEntity.move(this, movementDirection);

        // if Player dies in battle, remove from map
        if (!entities.contains(playerEntity)) { return endGame();}
        if (rewinded) {
            rewinded = false;
            gameStateHistory.add(GameToJSON.createGameToJSON(dungeon.getCurrentGame()));
            return dungeon.getCurrentGame().getDungeonResponseModel();
        }
        // for all entities, if its tickable, tick.

        tickImposter();

        List<Entity> copy = new ArrayList<>(entities);
        for (Entity entity : copy) {
            if (entity instanceof tickable) {
                ((tickable) entity).tick(this);
            }
        }

        // note spiders that are going to spawn are not in the entities list.
        // thats why we have to spawn manually here as the above this check those already existin gin entity list
        Spider.spawn(this, configFile.getInt("spider_spawn_rate"));

        // if Player dies in battle, remove from map
        if (!entities.contains(playerEntity)) { return endGame();}

        // if player is using a potition, decrease its duration
        if (!playerEntity.getPotionQueue().isEmpty()) {
            playerEntity.getPotionQueue().get(0).reduceDuration();
        }

        gameStateHistory.add(GameToJSON.createGameToJSON(this));

        //Helper.addAnimation(this);
        return getDungeonResponseModel();
    }

    /**
     * Given a string argument (bow or shield), builds the buildable item and adds it to players inventory
     * as well as removes the recipe items
     * throws IllegalArgumentException if argument is not bow or shield
     * throws invalidActionException if player's inventory does not hold recipe items
    **/
    public DungeonResponse build(String buildable) throws IllegalArgumentException, InvalidActionException {


        // if tick counter already exists e.g. building two things on one tick, add it to the build list
        List<String> builds = new ArrayList<>();
        if (buildHistory.containsKey(tickCounter)) {
            builds = buildHistory.get(tickCounter);
        }
        builds.add(buildable);
        buildHistory.put(tickCounter, builds);

        attemptBuild(buildable, this.getPlayer());
        //return dungeon response
        return getDungeonResponseModel();
    }

    public void attemptBuild(String buildable, Player player) throws IllegalArgumentException, InvalidActionException {
        if (!(Helper.isValidBuild(buildable))){
            throw new IllegalArgumentException();
        }

        switch (buildable){
            case "bow":
                if (player.canCraftBow()){
                    player.craftBow(configFile);
                    break;
                } else {
                    throw new InvalidActionException("Cannot build bow");
                }
            case "shield":
                if (!player.canCraftShield().equals("")){
                    player.craftShield(configFile);
                    break;
                } else {
                    throw new InvalidActionException("Cannot build shield");
                }
            case "sceptre":
                if (!player.canCraftSceptre().isEmpty()){
                    player.craftSceptre(configFile);
                    break;
                } else {
                    throw new InvalidActionException("Cannot build sceptre");
                }
            case "midnight_armour":
                if (player.canCraftArmour(this)){
                    player.craftArmour(configFile, this);
                    break;
                } else {
                    throw new InvalidActionException("Cannot build midnight armour");
                }
        }

    }

    /**
     * /game/interact
     */
    //Interacts with a mercenary (where the Player bribes the mercenary) or a zombie spawner, where the Player destroys the spawner.
    public DungeonResponse interact(String entityId) throws IllegalArgumentException, InvalidActionException {

        List<String> builds = new ArrayList<>();
        if (interactHistory.containsKey(tickCounter)) {
            builds = interactHistory.get(tickCounter);
        }
        builds.add(entityId);
        interactHistory.put(tickCounter, builds);

        ((Player)playerEntity).interact(entityId, this);

        return getDungeonResponseModel();
    }

    /**
     * /games/all
     */
    public List<String> allGames() {
        return new ArrayList<>();
    }

    public DungeonResponse endGame() {
        String dName = this.dungeonName;
        List<EntityResponse> entitiesResponse = Helper.convertEntitiesToResponse(entities);
        List<ItemResponse> inventory = Helper.convertInventoryToResponse(playerEntity);
        List<BattleResponse> battles = Helper.convertBattlesToResponse(playerEntity);
        List<String> buildables = Helper.convertBuildablesToResponse(playerEntity);

        return new DungeonResponse(gameId, dName, entitiesResponse, inventory, battles, buildables, "GAMEOVER");
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                       ENTITIES FUNCTIONALITY                                     //
    //////////////////////////////////////////////////////////////////////////////////////////////////////


    public void addEntitiy (Entity entity) {
        entities.add(entity);
    }


    // given a position, returns the list of all entities there
    public List<Entity> getEntitiesAtPosition (Position pos) {
        List<Entity> list = new ArrayList<>();
        list = entities.stream().filter(i -> i.getPosition().equals(pos))
                                .collect(Collectors.toList());
        return list;
    }


    // when adding or spawning new entities, makes sure it goes through the factory
    public Entity newEntity (String type, Position pos) {
        JSONObject json = new JSONObject();
        json.put("type", type);
        json.put("x", pos.getX());
        json.put("y", pos.getY());
        try {
            String configString = FileLoader.loadResourceFile("/configs/" + configName + ".json");
            JSONObject configs = new JSONObject(configString);
            return EntityFactory.createEntity(json, configs, this);

        }
        catch(IOException e) {
            throw new IllegalArgumentException();
        }

    }

    public void removeEntity(Entity entity) {
        entities.remove(entity);
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                        GETTERS+ SETTERS                                           //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public DungeonManiaController getDungeon() {return this.dungeon;}
    public void setEntitiesList(ArrayList<Entity> entities) {this.entities = entities;}
    public void setPlayerEntity(Player playerEntity) {this.playerEntity = playerEntity;}
    public OldPlayer getImpostPlayer() {return impostPlayer;}
    public void setImpostPlayer(OldPlayer impostPlayer) {this.impostPlayer = impostPlayer;}
    public ArrayList<Entity> getEntityList(){return entities;}
    public int getTickCounter () {return tickCounter;}
    public String getGameId() {return gameId;}
    public String getDungeonName() {return dungeonName;}
    public String getConfigName() {return configName;}
    public Goal getRootGoal() {return rootGoal;}
    public String getSkin() {return "default";}
    public String getLocalisation() {return "en_US";}
    public Player getPlayer () {return playerEntity;}
    public JSONObject getConfigFile () {return configFile;}
    public void setTickCounter(int tickCounter) {this.tickCounter = tickCounter;}
    public List<AnimationQueue> getAnimations (){ return animations;}

    public List<JSONObject> getGameStateHistory() {
        return gameStateHistory;
    }

    public void setDungeon(DungeonManiaController dungeon) {
        this.dungeon = dungeon;
    }

    public void setGameStateHistory(List<JSONObject> gameStateHistory) {
        this.gameStateHistory = gameStateHistory;
    }

    public void setPlayerMovesHistory(HashMap<Integer, Direction> playerMovesHistory) {
        this.playerMovesHistory = playerMovesHistory;
    }

    public void setTickItemHistory(HashMap<Integer, String> tickItemHistory) {
        this.tickItemHistory = tickItemHistory;
    }

    public void setBuildHistory(HashMap<Integer, List<String>> buildHistory) {
        this.buildHistory = buildHistory;
    }

    public void setInteractHistory(HashMap<Integer, List<String>> interactHistory) {
        this.interactHistory = interactHistory;
    }
}

