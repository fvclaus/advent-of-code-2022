
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

record Position(int line, int column) {

}

record ShortestPath(int distance, Node previous) {

}

class Node {

    private List<Node> neighbors;
    public Integer elevation;
    public Position position;
    private Graph graph;

    public Node(Integer elevation, Position position, Graph graph) {
        this.elevation = elevation;
        this.position = position;
        this.graph = graph;
    }

    public List<Node> getNeighbors() {
        if (this.neighbors == null) {
            var lineNumber = position.line();
            var columnNumber = position.column();
            this.neighbors = List.of(
                    new Position(lineNumber - 1, columnNumber),
                    new Position(lineNumber + 1, columnNumber),
                    new Position(lineNumber, columnNumber + 1),
                    new Position(lineNumber, columnNumber - 1))
                    .stream()
                    .map(this.graph::at)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();
        }
        return this.neighbors;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node node = (Node) obj;
        return position.equals(node.position);
    }

    @Override
    public int hashCode() {
        return position.hashCode();
    }

    @Override
    public String toString() {
        return "[" + position + "]";
    }

}

class Graph {

    private Position start;
    private Position end;
    public final List<List<Node>> grid = new ArrayList<>();

    public Graph(List<String> lines) {

        for (int lineNumber = 1; lineNumber <= lines.size(); lineNumber++) {
            var line = lines.get(lineNumber - 1).split("");
            var gridLine = new ArrayList<Node>();
            for (int columnNumber = 1; columnNumber <= line.length; columnNumber++) {
                var value = line[columnNumber - 1];
                if (value.equals("S")) {
                    value = "a";
                    this.start = new Position(lineNumber, columnNumber);
                } else if (value.equals("E")) {
                    value = "z";
                    this.end = new Position(lineNumber, columnNumber);
                }
                var elevation = value.charAt(0) - 'a';
                gridLine.add(new Node(elevation, new Position(lineNumber, columnNumber), this));
            }
            this.grid.add(gridLine);
        }
    }

    public Node getStart() {
        return at(this.start).get();
    }

    public Node getEnd() {
        return at(this.end).get();
    }

    public Optional<Node> at(Position pos) {
        if (pos.line() < 1 || pos.line() > grid.size()) {
            return Optional.empty();
        }
        var line = grid.get(pos.line() - 1);
        if (pos.column() < 1 || pos.column() > line.size()) {
            return Optional.empty();
        }
        return Optional.of(line.get(pos.column() - 1));
    }
}

public class Day12 {

    public static void main(String[] args) throws Exception {
        List<String> lines = Files.readAllLines(Path.of(Day12.class.getResource("/12.txt").toURI()));
        var graph = new Graph(lines);
        part1(graph);
        part2(graph);
    }

    // As you walk up the hill, you suspect that the Elves will want to turn this into a hiking trail. The beginning isn't very scenic, though; perhaps you can find a better starting point.
    // 
    // To maximize exercise while hiking, the trail should start as low as possible: elevation a. The goal is still the square marked E. However, the trail should still be direct, taking the fewest steps to reach its goal. So, you'll need to find the shortest path from any square at elevation a to the square marked E.
    // 
    // Again consider the example from above:
    // 
    // Sabqponm
    // abcryxxl
    // accszExk
    // acctuvwj
    // abdefghi
    // 
    // Now, there are six choices for starting position (five marked a, plus the square marked S that counts as being at elevation a). If you start at the bottom-left square, you can reach the goal most quickly:
    // 
    // ...v<<<<
    // ...vv<<^
    // ...v>E^^
    // .>v>>>^^
    // >^>>>>>^
    // 
    // This path reaches the goal in only 29 steps, the fewest possible.
    // 
    // What is the fewest steps required to move starting from any square with elevation a to the location that should get the best signal?
    private static void part2(Graph graph) {
        var absoluteShortestPath = new ShortestPath(Integer.MAX_VALUE, null);

        for (List<Node> line : graph.grid) {
            for (Node node : line) {
                if (node.elevation == 0) {
                    var shortestPath = findShortestPath(graph, node);
                    if (shortestPath != null) {
                        if (shortestPath.distance() < absoluteShortestPath.distance()) {
                            absoluteShortestPath = shortestPath;
                        }
                    }
                }
            }
        }
        System.out.println(absoluteShortestPath.distance());
    }

    // You try contacting the Elves using your handheld device, but the river you're following must be too low to get a decent signal.
    // 
    // You ask the device for a heightmap of the surrounding area (your puzzle input). The heightmap shows the local area from above broken into a grid; the elevation of each square of the grid is given by a single lowercase letter, where a is the lowest elevation, b is the next-lowest, and so on up to the highest elevation, z.
    // 
    // Also included on the heightmap are marks for your current position (S) and the location that should get the best signal (E). Your current position (S) has elevation a, and the location that should get the best signal (E) has elevation z.
    // 
    // You'd like to reach E, but to save energy, you should do it in as few steps as possible. During each step, you can move exactly one square up, down, left, or right. To avoid needing to get out your climbing gear, the elevation of the destination square can be at most one higher than the elevation of your current square; that is, if your current elevation is m, you could step to elevation n, but not to elevation o. (This also means that the elevation of the destination square can be much lower than the elevation of your current square.)
    // 
    // For example:
    // 
    // Sabqponm
    // abcryxxl
    // accszExk
    // acctuvwj
    // abdefghi
    // 
    // Here, you start in the top-left corner; your goal is near the middle. You could start by moving down or right, but eventually you'll need to head toward the e at the bottom. From there, you can spiral around to the goal:
    // 
    // v..v<<<<
    // >v.vv<<^
    // .>vv>E^^
    // ..v>>>^^
    // ..>>>>>^
    // 
    // In the above diagram, the symbols indicate whether the path exits each square moving up (^), down (v), left (<), or right (>). The location that should get the best signal is still E, and . marks unvisited squares.
    // 
    // This path reaches the goal in 31 steps, the fewest possible.
    // 
    // What is the fewest steps required to move from your current position to the location that should get the best signal?
    private static void part1(Graph graph) {
        var shortestPath = findShortestPath(graph, graph.getStart());
        System.out.println(shortestPath.distance());
    }

    private static ShortestPath findShortestPath(Graph graph, Node start) {
        Map<Node, ShortestPath> state = new HashMap<>();
        state.put(start, new ShortestPath(0, null));
        var queue = new PriorityQueue<Node>((a, b) -> {
            var stateA = state.get(a);
            var stateB = state.get(b);
            return stateA.distance() - stateB.distance();
        });
        queue.add(start);
        var end = graph.getEnd();

        while (!queue.isEmpty()) {
            var node = queue.poll();
            if (node.equals(end)) {
                break;
            }
            var path = state.get(node);
            if (path == null) {
                throw new IllegalStateException("No path defined for " + node);
            }
            for (Node neighbor : node.getNeighbors()) {
                boolean isReachable = (neighbor.elevation - node.elevation) <= 1;
                var neighborPath = state.getOrDefault(neighbor, new ShortestPath(Integer.MAX_VALUE, null));
                boolean isShorterPath = (path.distance() + 1) < neighborPath.distance();
                if (isReachable && isShorterPath) {
                    state.put(neighbor, new ShortestPath(path.distance() + 1, node));
                    queue.add(neighbor);
                }
            }
        }
        var node = graph.getEnd();
        return state.get(node);
    }
}
