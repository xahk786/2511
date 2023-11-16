package dungeonmania.persistence.load;

import java.util.ArrayList;

import org.json.JSONObject;

import dungeonmania.Entity;
import dungeonmania.MovingEntities.Player;
import dungeonmania.games.Game;

public class GameFromJSON {
    public static Game createGameFromJSON(JSONObject gameJSON) {

        Game game = new Game(gameJSON.getString("gameId"), gameJSON.getString("dungeonName"),
                            gameJSON.getString("configName"), gameJSON.getInt("tick_counter"),
                            gameJSON.getJSONObject("configFile"), gameJSON.getJSONObject("goal-condition"));
        ArrayList<Entity> entities = EntitiesFromJSON.createEntitiesFromJSON(gameJSON.getJSONArray("entities"), game.getConfigFile(), game);
        game.setEntitiesList(entities);
        game.setPlayerEntity((Player) entities.stream().filter(e -> e.getType().equals("player")).findFirst().orElse(null));

        return game;
    }
}
