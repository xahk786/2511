package dungeonmania.persistence.save;

import java.io.IOException;

import org.json.JSONObject;

import dungeonmania.games.Game;
import dungeonmania.util.FileLoader;

public class GameToJSON {
    public static JSONObject createGameToJSON(Game game) {
        JSONObject gameJSON = new JSONObject();
        gameJSON.put("gameId", game.getGameId());
        gameJSON.put("dungeonName", game.getDungeonName());
        gameJSON.put("configName", game.getConfigName());
        gameJSON.put("tick_counter", game.getTickCounter());
        // set playerEntity from entities list in load.java (no need to save here)
        gameJSON.put("entities", EntitiesToJSON.createEntitiestoJSON(game.getEntityList()));
        gameJSON.put("configFile", game.getConfigFile());
        
        try {
            gameJSON.put("goal-condition", new JSONObject(FileLoader.loadResourceFile("/dungeons/" + game.getDungeonName() + ".json")).getJSONObject("goal-condition"));
        } catch(IOException e) {
            throw new IllegalArgumentException();
        }

        return gameJSON;
    }
}
