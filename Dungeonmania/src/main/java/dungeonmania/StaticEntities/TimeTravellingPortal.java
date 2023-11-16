package dungeonmania.StaticEntities;

import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class TimeTravellingPortal extends StaticEntity {

    public TimeTravellingPortal(String id, String type, Position position, boolean isInteractable, boolean isPassable) {
        super(id, "time_travelling_portal", position, false, true);
    }

    public void timeTravel(Game game) {
        game.getDungeon().rewind(30);
    }
}
