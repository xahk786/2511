package dungeonmania;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import dungeonmania.StaticEntities.SwampTile;

import dungeonmania.MovingEntities.MovingEntity;
import dungeonmania.games.Game;
import dungeonmania.util.Position;

public class Distance {

    private MovingEntity movingEntity;
    Map<Position, Double> distance = null;
    Map<Position, Position> predecessors = null;

    public Distance() {}

    public Distance(MovingEntity entity) {
        this.movingEntity = entity;}

    //   0 1 2 3 4 5 6
    //   - - - - - - - 0
    //   x - - - - - x 1
    //   - - - - - - - 2                 is player is at (3,4)
    //   - - - - - - - 3                 top left -> right = (0,1) -> (6->1)
    //   - - - P - - - 4                 bot left -> right = (0,7) -> (6->7)
    //   - - - - - - - 5
    //   - - - - - - - 6
    //   x - - - - - x 7
    public List<Position> getRadiusSquare (int radius, Position centre) {
        List<Position> radiusSquarePositions = new ArrayList<>();
        int minX = centre.getX() - radius;
        int minY = centre.getY() - radius;
        int maxX = centre.getX() + radius;
        int maxY = centre.getY() + radius;

        // doubles gameMap just for goo measure when doing dijkstra
        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x ++) {
                radiusSquarePositions.add(new Position(x, y));
            }
        }
        return radiusSquarePositions;
    }



    //  0 1 2 3 4 5 6 7 8 9   < - X
    //  - - - - - - - - - E 0  Y       smallest = 0 0
    //  - - - - B - - - - - 1  |       biggest = (9,3) this is our game map.
    //  P - - - - - - - - - 2  v
    //  - E - - - - - - - - 3

    // everything above will represent a node
    // gives a list of the gameMap position, even if its blocked
    public List<Position> getGameMap (Game game) {
        List<Position> allPositions = new ArrayList<>();
        List<Entity> entities = game.getEntityList();

        int maxX = 0;
        int maxY = 0;
        for (Entity find : entities) {
            if (find.getPosition().getX() > maxX) {
                maxX = find.getPosition().getX();
            }
            if (find.getPosition().getY() > maxY) {
                maxY = find.getPosition().getY();
            }
        }

        // game map is unlimited... so the nodes go forever. the player will eventuall
        // walk out of the gamemap if if goods going one direction and then dijstra will fail....
        // CHANGE TO GET MAX VALUE OF EITHER X AND Y
        for (int y = -10; y <= maxY + 10; y++) {
            for (int x = -10; x <= maxX + 10; x ++) {
                allPositions.add(new Position(x, y));
            }
        }
        return allPositions;
    }



    // gives a list of the gameMap positions EXCLUDING BLOCKED stuff
    public List<Position> getWalkableGameMap (Game game, List<Position> gameMap) {
        List<Position> allPositions = new ArrayList<>();
        for (Position pos : gameMap) {
            List <Entity> entAtPos = game.getEntitiesAtPosition(pos);
            if (!entAtPos.stream().anyMatch(i -> movingEntity.isBlocked(i))) {
                    allPositions.add(pos);
            }
        }
        return allPositions;
    }

    // 0 1 2
    // 7 c 3
    // 6 5 4
    // gets cardinals (Odd numbers), blocked or unblocked.
    public List<Position> getCardinals (Position pos) {
        List<Position> ListOfCardinals = new ArrayList<>();
        List<Position> adjacents = pos.getAdjacentPositions();

        for (int i = 0; i < adjacents.size(); i++) {
            if (i != 0 && i % 2 != 0) { // only odd indexes are cardinal
                Position checkPos = adjacents.get(i);
                ListOfCardinals.add(checkPos);
           }
        }
        return ListOfCardinals;
    }


    // gets cardianl positions that the entitiy can move to
    // (these are basically the neighbours for each node(position) on gameMap graph)
    public List<Position> getValidCardinalNeighbour (Game game, Position position) {

        List<Position> cardinals = getCardinals(position);
        List<Position> unblockedCardinals = new ArrayList<>();

        // for all cardinals, get the entities there and check if blocked. if not, add it to list
        for (Position pos : cardinals) {
            List<Entity> entities = game.getEntitiesAtPosition(pos);
            if (!entities.stream().anyMatch(i -> movingEntity.isBlocked(i))) {
                unblockedCardinals.add(pos);
            }
        }
        return unblockedCardinals;
    }

    public List<Position> validCardinalWithinGameMap(Game game, Position position, List<Position> map) {

//         for (Position pos : map) {
// ;           System.out.println(pos);
//         }
        List<Position> unblockedCardinals = getValidCardinalNeighbour(game, position);
        List<Position> unblockedCardinalsWithinGameMap = new ArrayList<>();
        for (Position pos : unblockedCardinals) {
            if (map.stream().anyMatch(i -> i.equals(pos))) {
                unblockedCardinalsWithinGameMap.add(pos);
            }
        }
        //unblockedCardinalsWithinGameMap.forEach( k ->  System.out.println("unblcoked" + k));
        return unblockedCardinalsWithinGameMap;
    }

    // get the shortest distance from entity to player.
    // THERE IS ALWAYS A PATH TO THE PLAYER.
    public void dijkstra(Game game, List<Position> unblockedGameMap, Position src) {


        Map<Position, Double> dist = new HashMap<>();   // a map of the shortest dist from the src to every other node.
        Map<Position, Position> pred = new HashMap<>();  // which node we came from to get shortest path
        Queue<Position> queue = new LinkedList<Position>();   // doenst really matter since distance is the same but yes

        dist.put(src, 0.0); // src is 0 dis away from src
        queue.add(src);         // add src to our PQ

        // initalise everything but our src node to infinity
        // initialise all preds but src to be null
        for (Position position : unblockedGameMap) {
            if (!position.equals(src)) {
                dist.put(position, Double.POSITIVE_INFINITY);
                pred.put(position, null);
            }
        }

        // is PQ not empty,
        while (!queue.isEmpty()) {
            Position dequeued = queue.poll();     // removes the head.

            for (Position neighbour : validCardinalWithinGameMap(game, dequeued, unblockedGameMap)) {
                if (dist.get(dequeued) + 1 < dist.get(neighbour)) { //check if path is shorter than exisitng path for neighbour
                    // if neighbour is swamp tile, add 3, else add 1
                    if (game.getEntitiesAtPosition(neighbour).stream().anyMatch(e -> e.getType().equals("swamp_tile"))) {
                        SwampTile swampTile = (SwampTile) game.getEntitiesAtPosition(neighbour).stream().filter(e -> e.getType().equals("swamp_tile")).findFirst().get();
                        dist.put(neighbour, dist.get(dequeued) + swampTile.getMovement_factor() + 1);
                    } else {
                        dist.put(neighbour, dist.get(dequeued) + 1);
                    }
                    pred.put(neighbour, dequeued);
                    queue.add(neighbour);
                }
            }

        }
        this.distance = dist;
        this.predecessors = pred;
    }


    public Map<Position, Double> getDistance () { return distance;}
    public Map<Position, Position> getPredecessors() {return predecessors;}
}
