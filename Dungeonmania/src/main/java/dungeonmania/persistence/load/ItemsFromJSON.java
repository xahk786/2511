package dungeonmania.persistence.load;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Item;
import dungeonmania.games.Game;

public class ItemsFromJSON {
    public static List<Item> createItemsFromJSON (JSONArray itemsJSON, Game game) {
        List<Item> items = new ArrayList<Item>();
        for (int i = 0; i < itemsJSON.length(); i++) {
            items.add(createItemFromJSON(itemsJSON.getJSONObject(i), game));
        }
        return items;
    }

    public static Item createItemFromJSON(JSONObject itemJSON, Game game) {
        return (Item) EntitiesFromJSON.createEntityFromJSON(itemJSON, game.getConfigFile(), game);
    }
}
