package dungeonmania.MovingEntities;

import java.util.List;
import java.util.stream.Collectors;

import dungeonmania.Distance;
import dungeonmania.Item;

import dungeonmania.CollectableEntities.Treasure;
import dungeonmania.MoveStates.FollowPlayerState;
import dungeonmania.games.Game;
import dungeonmania.util.Position;


public class Assassin extends Mercenary {

    private double bribeFailRate;
    private int reconRadius;

    public Assassin(String id, Position position, Game game) {
        super(id, "assassin", position, game);
        this.setMovementState(new FollowPlayerState(this, game));
    }

    // everytime it moves, it checks if the player is invisible and if its within range to pybass it. if id can, it does so.
    @Override
    public void bypassPlayerInvisibility(Game game) {
            this.setMovementState(new FollowPlayerState(this, game));
    }

    @Override
    public boolean canBypassInvisibility() {
        System.out.println("reconRange:" + reconRadius);
        List<Position> reconRange = new Distance(this).getRadiusSquare(reconRadius, this.getPosition());
        Player player = this.getGame().getPlayer();
         // if player is invisible and its position is within the assassins recon range, then its can bypass invisibility
        return  reconRange.stream().anyMatch(i -> i.equals(player.getPosition())) ? true : false;

    }

    @Override
    public void bribe(List<Item> inventory) {

        // treasure is used up regardless if bribe failes or not.
        List<Item> treasure = inventory.stream()
                                       .filter(i -> i instanceof Treasure)
                                       .collect(Collectors.toList());
        treasure = treasure.subList(0, getBribeAmount());
        inventory.removeAll(treasure);

        // do not bribe if the bribe fails
        if (!this.actionSucceeds(bribeFailRate)) {
            System.out.println("failed bribing bc returned ");
            return;
        }
        // if it succeeds tho, bribe it
        this.setIsBribed(true);
        this.setIsInteractable(false);
    }

    public void setBribeFailRate(double bribeFailRate) {this.bribeFailRate = bribeFailRate; }
    public void setReconRadius(int reconRadius) {this.reconRadius = reconRadius;}

}
