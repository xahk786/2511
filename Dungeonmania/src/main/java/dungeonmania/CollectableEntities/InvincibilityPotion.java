package dungeonmania.CollectableEntities;

import java.util.List;

import dungeonmania.MovingEntities.Player;
import dungeonmania.util.Position;

public class InvincibilityPotion extends CollectableEntity implements Consumable {
    private int duration;
    private Player player;

    public InvincibilityPotion(String id, Position position, int duration){
        super(id, "invincibility_potion", position);
        this.duration = duration;
    }

    public int getDuration(){
        return this.duration;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    @Override
    public void consume(Player player) {
        this.player = player;
        if (player.getPotionQueue().size() == 0) {
            player.setIsInvincible(true);
        }
        player.addPotionToQueue(this);
    }

    @Override
    public void nextPotionInEffect() {
        player.setIsInvincible(true);
        player.setIsInvisible(false);
    }

    // reduces the duration of current potion. If potion is used up, consume the next on on the que
    @Override
    public void reduceDuration() {
        List <Consumable> potionQ = player.getPotionQueue();
        duration --;
        if (duration == 0) { // remove potion from q and from potion in use if used up
            potionQ.remove(this);
            if (potionQ.isEmpty()) {
                player.setIsInvincible(false);
            }
            if (!potionQ.isEmpty()) {
                player.getPotionQueue().get(0).nextPotionInEffect();
            }
        }
    }

}