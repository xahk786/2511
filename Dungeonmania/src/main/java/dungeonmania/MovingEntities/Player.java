package dungeonmania.MovingEntities;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONObject;

import dungeonmania.util.Direction;
import dungeonmania.util.Position;
import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.EntityFactory;
import dungeonmania.Item;
import dungeonmania.CollectableEntities.Consumable;
import dungeonmania.CollectableEntities.Treasure;
import dungeonmania.StaticEntities.Boulder;
import dungeonmania.StaticEntities.Door;
import dungeonmania.StaticEntities.Portal;
import dungeonmania.StaticEntities.SwitchDoor;
import dungeonmania.StaticEntities.TimeTravellingPortal;
import dungeonmania.StaticEntities.Wall;
import dungeonmania.StaticEntities.ZombieToastSpawner;
import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.games.Game;
import dungeonmania.StaticEntities.FloorSwitch;
import dungeonmania.Battles.Battle;
import dungeonmania.BuildableEntities.Sceptre;

public class Player extends MovingEntity implements SubjectPlayer {
    private boolean isPlayerIdle;
    private Position playerPrevPos;

    private int killCount;
    private List<Battle> battles = new ArrayList<Battle>();
    private List<Item> inventory = new ArrayList<Item>();
    private ArrayList<ObserverEnemy> listObservers = new ArrayList<>();
    private ArrayList<Consumable> potionQueue = new ArrayList<Consumable>();
    private boolean hasAlly = false;
    private boolean isInvincible = false;
    private boolean isInvisible = false;
    private Direction playerDirection;


    public Player (String id, String type, Position pos, Game game) {
        super(id, type, pos, false, game);
        this.setPosition(pos);
        this.killCount = 0;
        this.isPlayerIdle = false;
        this.playerPrevPos = pos;
    }


    /** Function to move player and interact with all entities on next tile
     *
     * @param game
     * @param direction
     */
    public void move(Game game, Direction direction) {
        Position offset = direction.getOffset();
        Position playerPos = this.getPosition();
        int playerX = playerPos.getX();
        int playerY = playerPos.getY();
        int offsetX = offset.getX();
        int offsetY = offset.getY();

        Position newPosition = new Position(playerX + offsetX, playerY + offsetY);

        // check if blocked by static entities ( since they cant move at the same time )
        List<Entity> entitiesAtNewPos = game.getEntitiesAtPosition(newPosition);
        List<Entity> entities = game.getEntityList();
        // only one blockble entity can exist at a time on a sqaure
        Entity entityBlockingPlayer = entitiesAtNewPos.stream().filter(i -> isBlocked(i)).findFirst().orElse(null);


        // if the list is empty, means that the position does not have any entities, move there
        if (entitiesAtNewPos.isEmpty()) {
            this.setPosition(newPosition);
            return;
        }
        // if entity is blocking movement and cannot be unblocked, don't move and leave function
        if (entityBlockingPlayer != null) {
            setIsPlayerIdle(true);
            return;
        }

        // boulders are movable and doors are unlockable from here on
        for (Entity entity : entitiesAtNewPos) {
            switch (entity.getType()) {
                case "exit": break;
                case "boulder": {
                    // can be moved if its moveable
                    Boulder boulderEnt = (Boulder) entity;
                    if (boulderEnt.canMove(game, direction)) {
                        // Boulder is on switch, deactivate switch
                        if (boulderEnt.isOnSwitch(game) != null) {
                            FloorSwitch floorSwitch = boulderEnt.isOnSwitch(game);
                            floorSwitch.deactivateConnectedEntities(game, floorSwitch);
                        }
                        // if next position is switch, will detonate any cardinal bombs, move player
                        else if (boulderEnt.isNextSwitch(game, direction) instanceof FloorSwitch) {
                            FloorSwitch floorSwitch = boulderEnt.isNextSwitch(game, direction);
                            floorSwitch.activateConnectedEntities(game, floorSwitch);
                        }
                        boulderEnt.move(direction);
                    } else { setIsPlayerIdle(true);}
                    break;
                }
                case "switch": break;
                case "door": {
                    // can be passed and/or unlocked
                    Door door = (Door) entity;
                    if (door.getIsLocked()) {
                        if (door.canOpen(inventory)) {
                            door.openDoor(inventory);
                        } else { setIsPlayerIdle(true);}
                    }
                    break;
                }
            }
            // if enemy, fight
            if (isEnemy(entity)) {
                // if curr player is invisible, battle is not triggered.
                if (this.isInvisible) {
                    continue;
                }
                battles.add(new Battle(entities, this, (MovingEntity) entity));
            }
            // if collectible, add to inventory and remove from map
            if (isCollectible(entity)) {
                if (entity.getType() == "key" && !canCollectKey()) {
                    continue;
                }
                addInventory((Item) entity);
                entities.remove(entity);
            }
        }

        // If portal is at new next location, action last so all other entities can be actioned.
        if (entitiesAtNewPos.stream().anyMatch(i -> i.getType() == "portal")) {
            Portal portal = (Portal) entitiesAtNewPos.stream().filter(i -> i instanceof Portal).findFirst().orElse(null);
            Position newPos = portal.teleport(game, direction);
            Position latestPosition = newPos;
            List<Entity> entitiesAtNewPosition = game.getEntitiesAtPosition(newPos);
            for (Entity entity : entitiesAtNewPosition) {
                if (entity.getType().equals("portal")) {
                    Portal port = (Portal) entity;
                    latestPosition = port.teleport(game, direction);
                }
            }
            this.setPosition(latestPosition);
        } else if (entitiesAtNewPos.stream().anyMatch(i -> i instanceof TimeTravellingPortal )){
            TimeTravellingPortal TTportal = (TimeTravellingPortal) entitiesAtNewPos.stream().filter(i -> i instanceof TimeTravellingPortal).findFirst().orElse(null);
            time_travel(game, TTportal, newPosition);
            return;
        } else {
            this.setPosition(newPosition);
        }
    }


    public void time_travel(Game game, TimeTravellingPortal portal, Position newPosition) {
        if (this instanceof OldPlayer) {
            game.removeEntity(this);
            return;
        }
        this.setPosition(newPosition);
        portal.timeTravel(game);
        game.setRewinded(true);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          INTERACT                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public void interact (String id, Game game) throws InvalidActionException, IllegalArgumentException {
        if (!game.getEntityList().stream().anyMatch(i -> i.getId().equals(id))) {
            throw new IllegalArgumentException("Entitiy does not exist");}

        Entity entity = game.getEntityList().stream().filter(i -> i.getId().equals(id)).findFirst().orElse(null);

        if (entity instanceof ZombieToastSpawner) {
            // check that is cardinally adjacent
            // for each cardinally adj squre, check for all entities that that toaster exists

            boolean isAdj = false;
            List <Position> cardinals = new Distance(this).getCardinals(this.getPosition());

            for (Position pos : cardinals) {
                List<Entity> entitiesAtCurrPos = game.getEntitiesAtPosition(pos);
                if (entitiesAtCurrPos.stream().anyMatch(i -> i.getId().equals(id))) {
                    isAdj = true;
                }
            }

            if (!isAdj) {
                throw new InvalidActionException("zombie_toast_spawner of id " + id + " not in range");
            } else {
                // proceed to destroy if possible - does NOT reduce durability
                // only swords are used to destroy toasters.
                if (getItem("sword") == null) {
                    throw new InvalidActionException("You dont have a sword");
                }
                game.removeEntity(entity);
            }
       } else if (entity instanceof bribable
                    && !((bribable) entity).getIsBribed()
                    && !((bribable) entity).getIsMindControlled()) { // cant interact wwith ally merc
            // interact meaning bribing the mercenary.
            // check if mercenary within range.

            if ( this.getInventory().stream().anyMatch(k -> k instanceof Sceptre)) {
                mindControl(game);
                return;
            }
            attemptBribe(entity);

       }
    }


    public void mindControl(Game game) {
        // if item is a spectre then all bribable enemies will be temporarily mindcontrolled
        // sceptres behaviour is undefined, assume they dont break for conveneince
        //but mind control effects last for its duration

        // all entities that are bribalye (mercs and assassins) will temporarily be mind controlled.
        // the ticking decreases the effects duration.

        List<Entity> copy = new ArrayList<>(game.getEntityList());
        for (Entity entity : copy) {
            if (entity instanceof bribable) {
                // only mind control if entitiy is not bribed

                if (((bribable) entity).getIsBribed()) {
                    break;
                }
                ((bribable) entity).setIsMindControlled(true);
                // if multiple spectres are used simultaneously, assume ethey work like pottions and they stack.
                ((bribable) entity).increaseMindControlDuration();
            }
        }
    }

    public void attemptBribe(Entity entity) throws InvalidActionException, IllegalArgumentException {
        int radius = ((bribable) entity).getBribeRadius();
        int bribeAmount = ((bribable) entity).getBribeAmount();

        // create square radius around user
        // the player is the center
        Position centre = this.getPosition();
        List<Position> radiusSquareMap = new Distance().getRadiusSquare(radius, centre);

        // see if you can find the mercenary within the radius square attempt to bribe
        if (radiusSquareMap.stream().anyMatch(i -> i.equals(entity.getPosition()))) {

            // if within radius see if you can bribe
            // get list of trasure
            if (inventory.stream().filter(i -> i instanceof Treasure).collect(Collectors.toList()).size() < bribeAmount) {
                throw new InvalidActionException("Not enough gold");
            } else {
                ((bribable) entity).bribe(inventory);
                setHasAlly(true);
            }
        } else {
            throw new InvalidActionException("Not within radius");
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                          BUILD                                                   //
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * returns true if can craft bow, else returns false
     **/
    public boolean canCraftBow(){ //requires 1 wood , 3 arrow

        long count_wood = inventory.stream().filter(x -> x.getType().equals("wood")).count();
        long count_arrow = inventory.stream().filter(x -> x.getType().equals("arrow")).count();

        return (count_wood >= 1) && (count_arrow >= 3);
    }

    /**
     * removes bow recipe items and then adds a bow object to player's inventory
     * if cannot craft bow, return
    **/
    public void craftBow(JSONObject config){

        if (!canCraftBow()){
            return;
        }

        int count_wood = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("wood") && count_wood < 1){
                count_wood++;
                inventory.remove(i);
                i = i - 1;
            }
        }

        int count_arrow = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("arrow") && count_arrow < 3){
                count_arrow++;
                inventory.remove(i);
                i = i-1;
            }
        }

        JSONObject bow = new JSONObject().put("type", "bow");
        bow.put("x", 0);
        bow.put("y", 0);
        addInventory((Item) EntityFactory.createEntity(bow, config,this.getGame()));

    }

    /**
     * returns "" if cannot craft shield, else returns a string "treasure" or "key"
     * depending on what is used to craft shield
     * prioritises crafting using what you have more of
    **/
    public String canCraftShield(){ //requires 2 wood and (1 treasure or 1 key)
        long count_wood = inventory.stream().filter(x -> x.getType().equals("wood")).count();
        long count_treasure = inventory.stream().filter(x -> x.getType().equals("treasure")).count();
        long count_key = inventory.stream().filter(x -> x.getType().equals("key")).count();
        long count_ss = inventory.stream().filter(x -> x.getType().equals("sun_stone")).count();


        boolean retainSunstone = false;
        if ((count_wood >= 2 && (count_treasure == 0 && count_key == 0) && count_ss >= 1)){
            retainSunstone = true;
        }

        if (!(count_wood >= 2 && (count_treasure >= 1 || count_key >= 1 || count_ss >= 1))){
            if (retainSunstone == false){
               return "";
            }
        }

        if (count_treasure >= 1){
            return "treasure";
        }

        if (count_key >= 1){
            return "key";
        }

        if (retainSunstone == true){
            return "sunstone";
        }

        return "";
    }

    /**
     * removes 2 wood. Checks which shield recipe item to remove and removes it
     * adds shield object to players inventory
     * if cannot craft bow, return
    **/
    public void craftShield(JSONObject config){
        String secondRecipe = canCraftShield();
        if (secondRecipe.equals("")){
            return;
        }

        int count_wood = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("wood") && count_wood < 2){
                count_wood++;
                inventory.remove(i);
                i = i -1;
            }
        }

        int count_secondRecipe = 0;
        if (!secondRecipe.equals("sun_stone")){
            for (int i = 0; i < inventory.size(); i++){
                if (inventory.get(i).getType().equals(secondRecipe) && count_secondRecipe < 1){
                    count_secondRecipe++;
                    if (secondRecipe == "sun_stone") {
                        continue;
                    }
                    inventory.remove(i);
                    i = i -1;
                }
            }
        }
        //else if it is a sunstone, do nothing, it is retained.


        JSONObject shield = new JSONObject().put("type", "shield");
        shield.put("x", 0);
        shield.put("y", 0);
        addInventory((Item) EntityFactory.createEntity(shield, config, this.getGame()));

    }

    /**
     * returns "" if cannot craft sceptre, else builds a string which decides what items to use
     * depending on what is used to craft sceptre
     * prioritises crafting using wood over arrows, and treasure over key
     * if no key AND no treasure, check for a sunstone , and if sunstone, can use that but it is retained
    **/
    public ArrayList<String> canCraftSceptre(){
        long count_wood = inventory.stream().filter(x -> x.getType().equals("wood")).count();
        long count_arrow = inventory.stream().filter(x -> x.getType().equals("arrow")).count();
        long count_key = inventory.stream().filter(x -> x.getType().equals("key")).count();
        long count_treasure = inventory.stream().filter(x -> x.getType().equals("treasure")).count();
        long count_ss = inventory.stream().filter(x -> x.getType().equals("sun_stone")).count();

        ArrayList<String> build = new ArrayList<String>();

        boolean retainSunstone = false;
        if ((count_wood >= 1 || count_arrow >= 2) && (count_key == 0 && count_treasure == 0) && (count_ss >= 2)){
            retainSunstone = true;
        }

        if (!((count_wood >= 1 || count_arrow >= 2) && (count_key >= 1 || count_treasure >= 1) && (count_ss >= 1))){
            if (retainSunstone == false){
                return build;
            }
        }


        if (count_wood >= 1){
            build.add("wood");
        }
        else if (count_arrow >= 2){
            build.add("arrow");
        }

        if (count_treasure >= 1){
            build.add("treasure");
        }
        else if (count_key >= 1){
            build.add("key");
        }

        if (retainSunstone){
            build.add("sun_stone");
        }

        return build;
    }

    public void craftSceptre(JSONObject config){
        ArrayList<String> build = canCraftSceptre();
        if (build.isEmpty()){
            return;
        }

        //uses one suns_stone, used as origin so does not retain
        int count_ss = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("sun_stone") && count_ss < 1){
                count_ss++;
                inventory.remove(i); // <-- Sun stone is retained after use
                i = i -1;
            }
        }

        //uses 1 wood OR 2 arrows
        String secondRecipe = build.get(0);
        int countSecondRecipe = 0;
        int secondRecipeMax = (secondRecipe.equals("wood") ? 1 : 2);

        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals(secondRecipe) && countSecondRecipe < secondRecipeMax){
                countSecondRecipe++;
                inventory.remove(i);
                i = i - 1;
            }
        }

        //uses (1 key or 1 treasure) OR 1 sunstone that ends up being retained
        String thirdRecipe = build.get(1);
        int countThirdRecipe = 0;
        int thirdRecipeMax = 1;
        if (!thirdRecipe.equals("sun_stone")){
            for (int i = 0; i < inventory.size(); i++){
                if (inventory.get(i).getType().equals(thirdRecipe) && countThirdRecipe < thirdRecipeMax){
                    countThirdRecipe++;
                    inventory.remove(i);
                    i = i -1;
                }
            }
        }
        //else if it is a sunstone, do nothing as it is retained.

        JSONObject sceptre = new JSONObject().put("type", "sceptre");
        sceptre.put("x", 0);
        sceptre.put("y", 0);

        addInventory((Item) EntityFactory.createEntity(sceptre, config, this.getGame()));
    }

    public boolean canCraftArmour(Game game){

        boolean zombieCheck = game.getEntityList().stream().anyMatch(x -> x.getType().equals("zombie_toast"));

        long count_sword = inventory.stream().filter(x -> x.getType().equals("sword")).count();
        long count_ss = inventory.stream().filter(x -> x.getType().equals("sun_stone")).count();

        return !(zombieCheck) && ((count_sword >= 1) && (count_ss >= 1));
    }

    public void craftArmour(JSONObject config, Game game){
        if (!canCraftArmour(game)){
            return;
        }

        int count_sword = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("sword") && count_sword < 1){
                count_sword++;
                inventory.remove(i);
                i = i - 1;
            }
        }

        int count_ss = 0;
        for (int i = 0; i < inventory.size(); i++){
            if (inventory.get(i).getType().equals("sun_stone") && count_ss < 1){
                count_ss++;
                inventory.remove(i); //sun stone is not retained here
                i = i-1;
            }
        }

        JSONObject armour = new JSONObject().put("type", "midnight_armour");
        armour.put("x", 0);
        armour.put("y", 0);

        addInventory((Item) EntityFactory.createEntity(armour, config, this.getGame()));

    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           OBSERVERS                                              //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void attach(ObserverEnemy enemy) {
        listObservers.add(enemy);
    }

    @Override
    public void dettach(ObserverEnemy enemy) {
        listObservers.remove(enemy);
    }

    @Override
    public void notifyObserver() {
        for (ObserverEnemy observers : listObservers) {
            // just incase a dead enemy is not removed properly.
            if (observers == null) {
                continue;
            }
            observers.update(this);
        }
    }

    public void setIsInvincible(boolean bool) {
        this.isInvincible = bool;
        notifyObserver();
    }
    public void setIsInvisible(boolean bool) {
        this.isInvisible = bool;
        notifyObserver();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           HELPERS                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public Boolean isCollectible(Entity entity) {
        return (entity instanceof Item);
    }

    public boolean canCollectKey() {
        // check if there is already a key in inventory
        // if key found is true: return false
        // if key found is false: return true:
        return !(inventory.stream().anyMatch(i -> i.getType().equals("key")));
    }

    // returns an item based on its type. just the first one it finds...
    public Item getItem (String type) {
        Item findItem = inventory.stream()
                               .filter(i -> i.getType().equals(type))
                               .findFirst()
                               .orElse(null);
        return findItem;
    }

    //returns an item based on its id
    public Item getItemViaId(String id){
        Item finditem = inventory.stream()
                                .filter(x -> x.getId().equals(id))
                                .findFirst()
                                .orElse(null);
        return finditem;
    }


    // removes an item based on the id.
    public void removeItem (String itemId) {
        for (Item find : inventory) {
            if (find.getId().equals(itemId)) {
                inventory.remove(find);
                break; // only removing one item
            }
        }
    }

    // players can be blocked by locked doors, boulders, walls
    @Override
    public Boolean isBlocked(Entity entity) {
        return ((entity instanceof Boulder && !((Boulder) entity).canMove(this.getGame(), playerDirection))
        || entity instanceof Wall
        || ((entity instanceof Door && ((Door) entity).getIsLocked())
        && (entity instanceof Door && !((Door) entity).canOpen(this.getGame().getPlayer().getInventory())))
        || (entity instanceof SwitchDoor && ((SwitchDoor) entity).getIsLocked())) ? true : false;
    }


    public Boolean isEnemy (Entity entity) {
        return (entity instanceof Spider
             || entity instanceof ZombieToast
             || (entity instanceof Mercenary && !((Mercenary) entity).getIsBribed() && !((Mercenary) entity).getIsMindControlled())
             || (entity instanceof OldPlayer && ((OldPlayer) entity).isBattleable())) ? true : false;
             // cant fight invisible Old_player.-> midnight armous or has sunstone
             // can fight invincible player, but we will die.
    }



    //////////////////////////////////////////////////////////////////////////////////////////////////////
    //                                           POTIONS                                                //
    //////////////////////////////////////////////////////////////////////////////////////////////////////
    public boolean isPotionQueueEmpty(){
        return this.potionQueue.isEmpty();
    }

    public void addPotionToQueue(Consumable potion){
        this.potionQueue.add(potion);
    }

    public void removePotionQueue(){
        if (isPotionQueueEmpty()){
            return;
        }
        this.potionQueue.remove(0);
    }

    public ArrayList<Consumable> getPotionQueue() {
        return this.potionQueue;
    }

    public Item getPotionInUse()          {
        if (potionQueue.isEmpty()) { return null;} //changed from potionqueue == null
            else                   { return (Item) potionQueue.get(0);}
    }


    //getters
    public List<ObserverEnemy> getObservers() { return listObservers;}
    public boolean getHasAlly()           { return hasAlly;}
    public boolean getIsInvincible()      { return isInvincible;}
    public boolean getIsInvisible()       { return isInvisible;}
    public int getKillCount()             { return killCount;}
    public List<Item> getInventory() { return inventory;}
    public List<Battle> getBattles() { return battles;}
    public Boolean getIsplayerIdle ()     { return isPlayerIdle;}
    public Position getPlayerPrevPos ()   { return playerPrevPos;}
    public Direction getPlayerDirection () {return playerDirection;}



    // setters
    public void addInventory(Item item)    { inventory.add(item);}
    public void addKillCount()             { killCount++;}
    public void setHasAlly(boolean bool)   { hasAlly = bool;}
    public void setIsPlayerIdle (boolean bool)      { isPlayerIdle = bool;}
    public void setPlayerPrevPos (Position pos)     { playerPrevPos = pos;}
    public void setPlayerDirection (Direction dir)  { playerDirection = dir;}
    public void setKillCount (int count)            { this.killCount = count;}
    public void setBattles (List<Battle> battles)   { this.battles = battles;}
    public void setInventory (List<Item> items)     { this.inventory = items;}
    public void setPotionQueue (ArrayList<Consumable> queue) { this.potionQueue = queue;}
}
