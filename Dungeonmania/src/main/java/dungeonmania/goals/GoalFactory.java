package dungeonmania.goals;

import dungeonmania.games.Game;

import org.json.JSONObject;

public class GoalFactory {


    public static Goal createGoal (JSONObject goals, Game game) {
        // Gets first goal
        String goal = goals.getString("goal");
        JSONObject configs = game.getConfigFile();

        switch (goal) {
            case "AND": {
                return new AndGoal(goals.getJSONArray("subgoals"), game);
            }
            case "OR": {
                return new OrGoal(goals.getJSONArray("subgoals"), game);
            }
            case "enemies": {
                int nKillsRequired = configs.getInt("enemy_goal");
                return new EnemiesGoal(nKillsRequired);
            }
            case "boulders": {
                return new BouldersGoal();
            }
            case "exit": {
                return new ExitGoal();
            }
            case "treasure": {
                int nTreasureRequired = configs.getInt("treasure_goal");
                return new TreasureGoal(nTreasureRequired, game);
            }
            default: return null;
        }

    }
}
