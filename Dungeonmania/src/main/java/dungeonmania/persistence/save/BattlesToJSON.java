package dungeonmania.persistence.save;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import dungeonmania.Battles.Battle;
import dungeonmania.Battles.Round;

public class BattlesToJSON {
    public static JSONArray createBattlesToJSON(List<Battle> battles) {
        JSONArray battlesJSON = new JSONArray();
        for (Battle battle : battles) {
            battlesJSON.put(createBattleToJSON(battle));
        }
        return battlesJSON;
    }

    public static JSONObject createBattleToJSON(Battle battle) {
        JSONObject battleJSON = new JSONObject();
        battleJSON.put("enemyType", battle.getEnemyType());
        battleJSON.put("initialPlayerHealth", battle.getInitialPlayerHealth());
        battleJSON.put("initialEnemyHealth", battle.getInitialEnemyHealth());
        battleJSON.put("rounds", createRoundsToJSON(battle.getRounds()));

        return battleJSON;
    }

    public static JSONArray createRoundsToJSON(List<Round> rounds) {
        JSONArray roundsJSON = new JSONArray();
        for (Round round : rounds) {
            roundsJSON.put(createRoundToJSON(round));
        }
        return roundsJSON;
    }

    public static JSONObject createRoundToJSON(Round round) {
        JSONObject roundJSON = new JSONObject();
        roundJSON.put("deltaPlayerHealth", round.getDeltaPlayerHealth());
        roundJSON.put("deltaEnemyHealth", round.getDeltaEnemyHealth());
        roundJSON.put("weaponryUsed", ItemsToJSON.createItemsToJSON(round.getWeaponryUsed()));
        
        return roundJSON;
    }
}
