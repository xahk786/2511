package dungeonmania.StaticEntities;

import dungeonmania.util.Position;

public class Exit extends StaticEntity {
    public Exit(String id, Position position) {
        super(id, "exit", position, false, true);
    }
}
