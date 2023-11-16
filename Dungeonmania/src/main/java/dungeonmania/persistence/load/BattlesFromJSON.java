package dungeonmania.persistence.load;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Battles.Battle;
import dungeonmania.Battles.Round;
import dungeonmania.games.Game;

public class BattlesFromJSON {
    public static List<Battle> createBattlesFromJSON(JSONArray battlesJSON, Game game) {
        List<Battle> battles = new ArrayList<Battle>();
        for (int i = 0; i < battlesJSON.length(); i++) {
            battles.add(createBattleFromJSON(battlesJSON.getJSONObject(i), game));
        }
        return battles;
    }

    public static Battle createBattleFromJSON(JSONObject battleJSON, Game game) {
        String enemyType = battleJSON.getString("enemyType");
        JSONArray roundsJSON = battleJSON.getJSONArray("rounds");
        double initialPlayerHealth = battleJSON.getDouble("initialPlayerHealth");
        double initialEnemyHealth = battleJSON.getDouble("initialEnemyHealth");
        return new Battle(enemyType, createRoundsFromJSON(roundsJSON, game), initialPlayerHealth, initialEnemyHealth);
    }

    public static List<Round> createRoundsFromJSON(JSONArray roundsJSON, Game game) {
        List<Round> rounds = new ArrayList<Round>();
        for (int i = 0; i < roundsJSON.length(); i++) {
            rounds.add(createRoundFromJSON(roundsJSON.getJSONObject(i), game));
        }
        return rounds;
    }

    public static Round createRoundFromJSON(JSONObject roundJSON, Game game) {
        double deltaPlayerHealth = roundJSON.getDouble("deltaPlayerHealth");
        double deltaEnemyHealth = roundJSON.getDouble("deltaEnemyHealth");
        JSONArray weaponryUsedJSON = roundJSON.getJSONArray("weaponryUsed");
        return new Round(deltaPlayerHealth, deltaEnemyHealth, ItemsFromJSON.createItemsFromJSON(weaponryUsedJSON, game));
    }
}
