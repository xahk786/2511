package dungeonmania;

import dungeonmania.exceptions.InvalidActionException;
import dungeonmania.games.Game;
import dungeonmania.response.models.DungeonResponse;
import dungeonmania.util.Direction;
import dungeonmania.util.FileLoader;
import dungeonmania.persistence.load.GameFromJSON;
import dungeonmania.persistence.save.GameToJSON;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;


public class DungeonManiaController {


    private List<Game> games = new ArrayList<Game>();
    private Game currentGame;

    private int id = 0;


    public String getSkin() {
        return "default";
    }

    public String getLocalisation() {
        return "en_US";
    }

    /**
     * /dungeons
     */
    public static List<String> dungeons() {
        return FileLoader.listFileNamesInResourceDirectory("dungeons");
    }

    /**
     * /configs
     */
    public static List<String> configs() {
        return FileLoader.listFileNamesInResourceDirectory("configs");
    }

    /**
     * /game/new
     */

    /*
     * check dungeonName is in dungeons()
     * load dungeon with FileLoader.loadResourceFile("/dungeons/" + dungeonName + ".json") <-- returns string of "[entities], goal-condition"
     * Turn string into list of entities and into goals
     *
     * types "bow" and "shield" should not be added to entities list (Should be added to inventory list) (Spec 4.1)
     */
    public DungeonResponse newGame(String dungeonName, String configName) throws IllegalArgumentException {
        if (!dungeons().contains(dungeonName) || !configs().contains(configName)) {
            throw new IllegalArgumentException();
        }

        this.id++;
        String gameId = "game" + this.id;
        currentGame = new Game();
        currentGame.setDungeon(this);

        return currentGame.newGame(gameId, dungeonName, configName);
    }


    /**
     * /game/dungeonResponseModel
     */
    public DungeonResponse getDungeonResponseModel() {
        return currentGame.getDungeonResponseModel();
    }

    /**
     * /game/tick/item
     */
    public DungeonResponse tick(String itemUsedId) throws IllegalArgumentException, InvalidActionException {
        return currentGame.tick(itemUsedId);
    }

    /**
     * /game/tick/movement
     */
    public DungeonResponse tick(Direction movementDirection) {
        return currentGame.tick(movementDirection);
    }

    /**
     * Given a string argument (bow or shield), builds the buildable item and adds it to players inventory
     * as well as removes the recipe items
     * throws IllegalArgumentException if argument is not bow or shield
     * throws invalidActionException if player's inventory does not hold recipe items
    **/
    public DungeonResponse build(String buildable) throws IllegalArgumentException, InvalidActionException {
        return currentGame.build(buildable);
    }

    /**
     * /game/interact
     */
    //Interacts with a mercenary (where the Player bribes the mercenary) or a zombie spawner, where the Player destroys the spawner.
    public DungeonResponse interact(String entityId) throws IllegalArgumentException, InvalidActionException {
        return currentGame.interact(entityId);
    }

    /**
     * /game/save
     */
    public DungeonResponse saveGame(String name) {
        JSONObject gameJSON = GameToJSON.createGameToJSON(currentGame);

        // add make saves directory
        String dirPath = "./saves";
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdir();
        }

        String path = dirPath + "/" + name + ".json";
        File f = new File(path);
        f.delete();

        try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
            out.write(gameJSON.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // temp solution
        games.add(currentGame);
        return currentGame.getDungeonResponseModel();
    }

    /**
     * /game/load
     */
    public DungeonResponse loadGame(String name) throws IllegalArgumentException {
        if (!allGames().contains(name)) {
            throw new IllegalArgumentException();
        }

        // input String name is name of json file to load from
        try {
            String gameString = new String(Files.readAllBytes(Path.of("./saves/" + name + ".json")));
            JSONObject gameJSON = new JSONObject(gameString);
            Game game = GameFromJSON.createGameFromJSON(/*this,*/ gameJSON);
            currentGame = game;

            return currentGame.getDungeonResponseModel();
        }
        catch(IOException e) {
            throw new IllegalArgumentException();
        }
    }


    public DungeonResponse rewind(int ticks) throws IllegalArgumentException {
        currentGame.rewind(ticks);
        return currentGame.getDungeonResponseModel();
    }


    /**
     * /games/all
     */
    public List<String> allGames() {
        List<String> allGames = new ArrayList<>();
        File dir = new File("./saves");
        String[] filenames = dir.list();
        for (String string : filenames) {
            allGames.add(string.replace(".json", ""));
        }
        return allGames;
    }

    public Game getCurrentGame() { return currentGame;}
    public void setCurrentGame(Game currentGame) {this.currentGame = currentGame;}

}


