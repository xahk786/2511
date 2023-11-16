package dungeonmania.CollectableEntities;

import java.util.List;

import dungeonmania.MovingEntities.Player;
import dungeonmania.util.Position;

public class InvisibilityPotion extends CollectableEntity implements Consumable {
    private int duration;
    private Player player;

    public InvisibilityPotion(String id, Position position, int duration){
        super(id, "invisibility_potion", position);
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration){
        this.duration = duration;
    }

    @Override
    public void consume(Player player) {
        this.player = player;
        // if its the first postion were consuming, we set it to its effect.
        if (player.getPotionQueue().size() == 0) {
            player.setIsInvisible(true);
        }
        player.addPotionToQueue(this);
    }

    @Override
    public void nextPotionInEffect() {
        player.setIsInvisible(true);
        player.setIsInvincible(false);
    }

    @Override
    public void reduceDuration() {
        List <Consumable> potionQ = player.getPotionQueue();
        duration --;
        if (duration == 0) { // remove potion from q and from potion in use if used up
            potionQ.remove(this);
            if (potionQ.isEmpty()) {
                player.setIsInvisible(false);
            }
            if (!potionQ.isEmpty()) {
                player.getPotionQueue().get(0).nextPotionInEffect();
            }
        }
    }
}
