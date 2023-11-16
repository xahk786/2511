package dungeonmania.persistence.save;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Item;

public class ItemsToJSON {
    public static JSONArray createItemsToJSON(List<Item> items) {
        JSONArray itemsJSON = new JSONArray();
        for (Item item : items) {
            itemsJSON.put(createItemToJSON(item));
        }
        return itemsJSON;
    }

    public static JSONObject createItemToJSON(Item item) {
        return EntitiesToJSON.createEntitytoJSON(item);
    }
}
