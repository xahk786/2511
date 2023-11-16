package dungeonmania.StaticEntities;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import dungeonmania.Distance;
import dungeonmania.Entity;
import dungeonmania.Item;
import dungeonmania.tickable;
import dungeonmania.Battles.Battle;
import dungeonmania.MovingEntities.Player;
import dungeonmania.MovingEntities.ZombieToast;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class ZombieToastSpawner extends StaticEntity implements tickable {

    int zombieSpawnRate;
    public ZombieToastSpawner(String id, Position position) {
        super(id, "zombie_toast_spawner", position, true, true); // toaster is passable in sample
        this.setIsInteractable(true);
    }

    public void tick(Game game) {
        int ticks = game.getTickCounter();

        // time to spawn a zombie yay
        if ((zombieSpawnRate != 0) && (ticks != 0 ) && (ticks % zombieSpawnRate == 0)) {
            this.spawn(game);
        }
    }

     // zombies only spawn from zombie spawner. they spawn to a cardilly adjacent box.
    public void spawn (Game game) {

        List<Position> unblocked = new ArrayList<>();

        unblocked = new Distance(new ZombieToast("-10", "zombie_toast", null, game)).getValidCardinalNeighbour(game, this.getPosition());
        // doenst spawn if the zombie spawner is surrounded by items.
        // other spawn somewhere cardianlly adjacent to the toast spanwer
        if (!unblocked.isEmpty()) {

            Random rand = new Random();
            Position randPos = unblocked.get(rand.nextInt(unblocked.size()));

            ZombieToast newZombie = (ZombieToast) game.newEntity("zombie_toast", randPos);
            game.addEntitiy(newZombie);

            if (game.getEntitiesAtPosition(randPos).stream().anyMatch(i -> i instanceof Player)) {
                Player player = game.getPlayer();
                player.getBattles().add(new Battle(game.getEntityList(), player, newZombie));
            }

        }

    }

    public Boolean isBlocked (Entity entity) {
        return (entity instanceof Boulder ||
                entity instanceof Wall ||
                entity instanceof Door && ((Door) entity).getIsLocked()) ? true : false;
    }

    public void setSpawnRate(int rate) {this.zombieSpawnRate = rate;}
    public int getSpawnRate() {return zombieSpawnRate;}

    // check if player can destroy spawner- entites stream with intereact id
    public void tryDestroy(Game game, Player player) {
        // check cardinally adjacent
        int x = getPosition().getX();
        int y = getPosition().getY();

        List<Position> cardinalPositions = Arrays.asList(
            new Position(x, y + 1),
            new Position(x, y - 1),
            new Position(x - 1, y),
            new Position(x + 1, y)
        );

        Position isPlayerCardinal = cardinalPositions.stream().filter(i -> i.equals(player.getPosition())).findFirst().orElse(null);
        if (isPlayerCardinal == null) {
            return;
        }
        // check if player has a weapon - forum said they only test for sword
        List<Item> items = player.getInventory();
        Item weapon = (Item) items.stream().filter(i -> i.getType().equals("sword")).findFirst().orElse(null);
        if (weapon == null) {
            return;
        }
        // destory
        game.removeEntity(this);
    }
}
