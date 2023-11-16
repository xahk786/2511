package dungeonmania.CollectableEntities;

import dungeonmania.MovingEntities.Player;

public interface Consumable {
    public void consume(Player player);
    public void reduceDuration();
    public void nextPotionInEffect();
}
