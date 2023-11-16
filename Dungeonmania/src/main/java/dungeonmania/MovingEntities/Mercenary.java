package dungeonmania.MovingEntities;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.Item;
import dungeonmania.tickable;
import dungeonmania.Battles.Battle;
import dungeonmania.CollectableEntities.Treasure;
import dungeonmania.MoveStates.FollowPlayerState;
import dungeonmania.MoveStates.RunAwayState;
import dungeonmania.MoveStates.TailPlayerState;
import dungeonmania.StaticEntities.Boulder;
import dungeonmania.StaticEntities.Door;
import dungeonmania.StaticEntities.Wall;
import dungeonmania.StaticEntities.ZombieToastSpawner;
import dungeonmania.games.Game;
import dungeonmania.MoveStates.RandomMovementState;
import dungeonmania.util.Position;

public class Mercenary extends MovingEntity implements tickable, ObserverEnemy, bribable {

    private int ticksRemaining;
    private int bribeRadius;
    private int bribeAmount;
    private double allyAttack;
    private double allyDefence;
    private boolean isBribed = false;
    private boolean isMindControlled = false;
    private int mindControlDuration; // IS FINAL; how long mind control lasts when a sceptre used
    private int remainingMindControlDuration = 0; // the remainin effects of the mind control.


    public Mercenary (String id, String type, Position position, Game game){
        super(id, type, position, true, game);
        this.setMovementState(new FollowPlayerState(this, game));
    }

    // mercs dont spawn.
    @Override
    public void tick(Game game) {
        this.move(game);

        if (isMindControlled) {
            remainingMindControlDuration--;
            if (remainingMindControlDuration == 0) {
                isMindControlled = false;
            }
        }
    }

    public void increaseMindControlDuration() {
        remainingMindControlDuration += mindControlDuration;

    }

    // changes movement state if the player is invisible or invincible.
    @Override
    public void update(SubjectPlayer obj) {

        // go into potion induced movement state is player drinks a potion
        Player player = (Player) obj;
        if (player.getIsInvincible() && !this.isBribed) {
            this.setMovementState(new RunAwayState(this, this.getGame()));

        }
        if (player.getIsInvisible()) {
            this.setMovementState((new RandomMovementState(this, this.getGame())));
        }

        // return to orginal movement state if from invincible/invisible state :
        if (!player.getIsInvisible() && this.getMovementState() instanceof RandomMovementState) {
            this.setMovementState(new FollowPlayerState(this, this.getGame()));
        }

        if (!player.getIsInvincible() && this.getMovementState() instanceof RunAwayState) {
            this.setMovementState(new FollowPlayerState(this, this.getGame()));
        }
    }


    public void move(Game game) {

        // Check how many ticks remaining
        // if ticks remaining: reduce tick by one and return
        if (ticksRemaining > 0) {
            this.ticksRemaining -= 1;
            return;
        }

        Player player = game.getPlayer();
        boolean underwentInvisBypass = false;

        List <Position> cardinals = new Distance(this).getValidCardinalNeighbour(game, this.getPosition());
        cardinals.add(this.getPosition());

        // check if mercenary has a recon range for invisibility
        if (player.getIsInvisible() && canBypassInvisibility()) {
            bypassPlayerInvisibility(game);
            underwentInvisBypass = true;
        }

        // the actual moving:
        // if merc is in the same position/adjacent position to as the payer after its move,
        if (cardinals.stream().anyMatch(i -> i.equals(game.getPlayer().getPosition())
                                        &&!(this.getMovementState() instanceof TailPlayerState))
                                        && !player.getIsInvincible()) {
            if (!player.getIsInvisible() || (player.getIsInvisible() && canBypassInvisibility())) {
                if (!getIsBribed() && !getIsMindControlled()) {
                    // if next tile is swamp tile
                    this.getMovementState().setSwampTile(game.getPlayer().getPosition(), game);
                    // if not bribed/mindcontrolled move to the players position.
                    this.getMovementState().move(game.getPlayer().getPosition());
                } else {
                    // if bribed: move to players previous spot
                    this.setMovementState(new TailPlayerState(this, game));
                    Position nextPos = this.getMovementState().findNextPosition();
                    // if next tile is swamp tile
                    this.getMovementState().setSwampTile(nextPos, game);
                    this.getMovementState().move(nextPos);
                    return;

                }
            }
        } else {
            Position nextPos = this.getMovementState().findNextPosition();
            this.getMovementState().setSwampTile(nextPos, game);
            this.getMovementState().move(nextPos);
        }

        // once mercenary  has bypassed invisibility and moved, reset it back to randomMovement for its next move
        // as it might no longer be in recon range somehow
        if (underwentInvisBypass && player.getIsInvisible()) {
            this.setMovementState(new RandomMovementState(this, game));
        }

        // check if can fight if not bribed
        if (!this.isBribed && !this.isMindControlled) {

            List<Entity> entities = game.getEntitiesAtPosition(this.getPosition());

            if (entities.stream().anyMatch(i -> i instanceof Player)) {
                player = entities.stream().filter(i -> i instanceof Player).findAny().map(i -> (Player) i).orElse(null);
                // ASSUMES ASSASSINS CAN FIGHT PLAYERS EVEN IF THEY ARE INVISIBLE IF THEY ARE WITHIN RECON RANGE
                if (!player.getIsInvisible() || canBypassInvisibility() ) { // not invisible or is invisible and within recon range
                    player.getBattles().add(new Battle(game.getEntityList(), game.getPlayer(), this));
                }
            }
        }
    }


    public boolean canBypassInvisibility() {
        return false;
    }

    // checks if mercenery has the potential to bypass invisibility effects
    public void bypassPlayerInvisibility(Game game) {
        // does nothing for normal mercenaries
        return;
    }



    // a valid bribe
    // player uses up gold and the mer becomes bribed
    @Override
    public void bribe (List<Item> inventory) {
        List<Item> treasure = inventory.stream().filter(i -> i instanceof Treasure).collect(Collectors.toList());
        treasure = treasure.subList(0, getBribeAmount());
        inventory.removeAll(treasure);
        this.setIsBribed(true);
        this.setIsInteractable(false);
    }


    public Boolean isBlocked (Entity entity) {
        return (entity instanceof Boulder ||
                entity instanceof Wall ||
               (entity instanceof Door && ((Door) entity).getIsLocked())) ? true : false;
    }

    public void setTicksRemaining(int ticks) {
        this.ticksRemaining = ticks;
    }

    public boolean getIsBribed() {return isBribed;}
    public int getBribeRadius() {return bribeRadius;}
    public int getBribeAmount() {return bribeAmount;}
    public double getAllyAttack() { return allyAttack;}
    public double getAllyDefence() { return allyDefence;}
    public boolean getIsMindControlled() {return isMindControlled;}
    public int getMindControlDuration() {return mindControlDuration;}


    public void setIsBribed(boolean bool) {this.isBribed = bool;};
    public void setBribeRadius(int radius) {this.bribeRadius = radius;};
    public void setBribeAmount( int bribeAmount) {this.bribeAmount = bribeAmount;}
    public void setAllyAttack(double allyAttack) { this.allyAttack = allyAttack;}
    public void setAllyDefence(double allyDefence) { this.allyDefence = allyDefence;}
    public void setIsMindControlled(boolean isMindCountrolled) {this.isMindControlled = isMindCountrolled;}
    public void setMindControlDuration(int mindControlDuration) {this.mindControlDuration = mindControlDuration;}


}
