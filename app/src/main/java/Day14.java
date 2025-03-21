
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

class Day14 {

    static String input = """
498,4 -> 498,6 -> 496,6
503,4 -> 502,4 -> 502,9 -> 494,9""";

    record Coordinate(int x, int y) {

        public Coordinate  {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException("Coordinates cannot be negative. Got: (" + x + "," + y + ")");
            }
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }

    record StonePath(List<Coordinate> coordinates) {

        @Override
        public String toString() {
            return this.coordinates.stream().map(Object::toString).collect(Collectors.joining(" -> "));
        }
    }

    enum CaveObject {
        STONE('#'),
        SAND('o'),
        AIR('.');

        private final char symbol;

        CaveObject(char symbol) {
            this.symbol = symbol;
        }

        public char getSymbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return String.valueOf(symbol);
        }
    }

    static class Cave {

        private final ArrayList<ArrayList<CaveObject>> grid = new ArrayList<>();
        private final int originalGridSize;

        // final int minX;
        public Cave(List<StonePath> paths) {
            var minX = paths.stream()
                    .flatMap(path -> path.coordinates().stream())
                    .reduce(
                            Integer.MAX_VALUE,
                            (mins, coord) -> Math.min(mins, coord.x()),
                            (a, b) -> Math.min(a, b)
                    );
            // this.minX = minX < 5 ? 0 : minX - 5;

            for (var path : paths) {
                this.addPath(path);
            }
            this.originalGridSize = grid.size();
        }

        public void addCaveObject(int x, int y, CaveObject caveObject) {
            while (grid.size() < (y + 1)) {
                grid.add(new ArrayList<>());
            }
            var line = grid.get(y);
            for (var i = 0; i < x; i++) {
                if (line.size() < x + 1) {
                    line.add(CaveObject.AIR);
                }
            }
            if (line.size() >= x + 1) {
                line.set(x, caveObject);
            } else {
                line.add(caveObject);
            }
        }

        private void addPath(StonePath path) {
            List<Coordinate> coords = path.coordinates();

            for (int i = 0; i < coords.size() - 1; i++) {
                Coordinate current = coords.get(i);
                Coordinate next = coords.get(i + 1);

                drawLine(current, next);
            }
        }

        private void drawLine(Coordinate from, Coordinate to) {

            int x1 = from.x();
            int y1 = from.y();
            int x2 = to.x();
            int y2 = to.y();

            int dx = Integer.compare(x2, x1);  // -1, 0, or 1
            int dy = Integer.compare(y2, y1);  // -1, 0, or 1

            if (Math.abs(dx + dy) != 1) {
                throw new IllegalStateException("Only one of dx, dy should be -1 or 1, but was dx: " + dx + ", dy: " + dy);
            }

            int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));

            for (int i = 0; i <= steps; i++) {
                int x = x1 + i * dx;
                int y = y1 + i * dy;
                addCaveObject(x, y, CaveObject.STONE);
            }
        }

        private CaveObject at(int x1, int y1) {
            var line = this.grid.get(y1);
            if (line.size() < (x1 + 1)) {
                return CaveObject.AIR;
            }
            return line.get(x1);
        }

        public Coordinate simulateSand(boolean simulateFloor) {
            int x = 500;
            int y = 0;

            BiFunction<Integer, Integer, CaveObject> get = (x1, y1) -> {
                if (y1 >= grid.size() || x1 < 0) {
                    if (simulateFloor) {
                        if (y1 == originalGridSize) {
                            return CaveObject.AIR;
                        } else if (y1 == originalGridSize + 1) {
                            return CaveObject.STONE;
                        } else {
                            throw new IllegalStateException("Should not happen");
                        }
                    } else {
                        return null;
                    }
                }

                return this.at(x1, y1);
            };

            if (get.apply(x, y) == CaveObject.SAND) {
                // Cave is full
                return null;
            }
            while (true) {
                if (get.apply(x, y + 1) == null) {
                    // Falls into the depth
                    return null;
                } else if (get.apply(x, y + 1) == CaveObject.AIR) {
                    // Sand falls down
                    y += 1;
                } else if (get.apply(x - 1, y + 1) == CaveObject.AIR) {
                    // Sand falls down to the left
                    y += 1;
                    x -= 1;
                } else if (get.apply(x + 1, y + 1) == CaveObject.AIR) {
                    // Sand falls down to the right
                    y += 1;
                    x += 1;
                } else {
                    // Sand comes to rest
                    return new Coordinate(x, y);
                }
            }
        }

        public void print() {
            var minX = this.grid.stream()
                    .reduce(
                            Integer.MAX_VALUE,
                            (mins, line) -> {
                                for (int i = 0; i < Math.min(line.size(), mins); i++) {
                                    CaveObject object = line.get(i);
                                    if (object == CaveObject.STONE || object == CaveObject.SAND) {
                                        return Math.min(mins, i);
                                    }
                                }
                                // Ignore lines with only null values.
                                return mins;
                            },
                            (a, b) -> Math.min(a, b)
                    );
            minX = minX < 5 ? 0 : minX - 5;
            for (var line : grid) {
                for (int i = minX; i < line.size(); i++) {
                    var object = line.get(i);
                    if (object != null) {
                        System.out.print(object.symbol);
                    } else {
                        System.out.print(".");
                    }
                }
                System.out.print("\n");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        List<StonePath> paths = parsePaths();

        System.out.println("Original input:");
        System.out.println(input);

        System.out.println("\nReconstructed input from parsed data:");
        for (StonePath path : paths) {
            System.out.println(path);
        }

        part1(paths);

        part2(paths);
    }

    // You realize you misread the scan. There isn't an endless void at the bottom of the scan - there's floor, and you're standing on it!
    // 
    // You don't have time to scan the floor, so assume the floor is an infinite horizontal line with a y coordinate equal to two plus the highest y coordinate of any point in your scan.
    // 
    // In the example above, the highest y coordinate of any point is 9, and so the floor is at y=11. (This is as if your scan contained one extra rock path like -infinity,11 -> infinity,11.) With the added floor, the example above now looks like this:
    // 
    //         ...........+........
    //         ....................
    //         ....................
    //         ....................
    //         .........#...##.....
    //         .........#...#......
    //         .......###...#......
    //         .............#......
    //         .............#......
    //         .....#########......
    //         ....................
    // <-- etc #################### etc -->
    // 
    // To find somewhere safe to stand, you'll need to simulate falling sand until a unit of sand comes to rest at 500,0, blocking the source entirely and stopping the flow of sand into the cave. In the example above, the situation finally looks like this after 93 units of sand come to rest:
    // 
    // ............o............
    // ...........ooo...........
    // ..........ooooo..........
    // .........ooooooo.........
    // ........oo#ooo##o........
    // .......ooo#ooo#ooo.......
    // ......oo###ooo#oooo......
    // .....oooo.oooo#ooooo.....
    // ....oooooooooo#oooooo....
    // ...ooo#########ooooooo...
    // ..ooooo.......ooooooooo..
    // #########################
    // 
    // Using your scan, simulate the falling sand until the source of the sand becomes blocked. How many units of sand come to rest?
    private static void part2(List<StonePath> paths) {
        Cave cave = new Cave(paths);
        cave.print();

        int sandCount = 0;
        Coordinate sandPosition;
        while ((sandPosition = cave.simulateSand(true)) != null) {
            cave.addCaveObject(sandPosition.x(), sandPosition.y(), CaveObject.SAND);
            // cave.print();
            sandCount++;
        }
        // Display the cave
        System.out.println("\nCave representation after " + sandCount + " units of sand:");
        cave.print();
        System.out.println("Total sand units: " + sandCount);
    }

    // The distress signal leads you to a giant waterfall! Actually, hang on - the signal seems like it's coming from the waterfall itself, and that doesn't make any sense. However, you do notice a little path that leads behind the waterfall.
    // 
    // Correction: the distress signal leads you behind a giant waterfall! There seems to be a large cave system here, and the signal definitely leads further inside.
    // 
    // As you begin to make your way deeper underground, you feel the ground rumble for a moment. Sand begins pouring into the cave! If you don't quickly figure out where the sand is going, you could quickly become trapped!
    // 
    // Fortunately, your familiarity with analyzing the path of falling material will come in handy here. You scan a two-dimensional vertical slice of the cave above you (your puzzle input) and discover that it is mostly air with structures made of rock.
    // 
    // Your scan traces the path of each solid rock structure and reports the x,y coordinates that form the shape of the path, where x represents distance to the right and y represents distance down. Each path appears as a single line of text in your scan. After the first point of each path, each point indicates the end of a straight horizontal or vertical line to be drawn from the previous point. For example:
    // 
    // 498,4 -> 498,6 -> 496,6
    // 503,4 -> 502,4 -> 502,9 -> 494,9
    // 
    // This scan means that there are two paths of rock; the first path consists of two straight lines, and the second path consists of three straight lines. (Specifically, the first path consists of a line of rock from 498,4 through 498,6 and another line of rock from 498,6 through 496,6.)
    // 
    // The sand is pouring into the cave from point 500,0.
    // 
    // Drawing rock as #, air as ., and the source of the sand as +, this becomes:
    // 
    // 
    //   4     5  5
    //   9     0  0
    //   4     0  3
    // 0 ......+...
    // 1 ..........
    // 2 ..........
    // 3 ..........
    // 4 ....#...##
    // 5 ....#...#.
    // 6 ..###...#.
    // 7 ........#.
    // 8 ........#.
    // 9 #########.
    // 
    // Sand is produced one unit at a time, and the next unit of sand is not produced until the previous unit of sand comes to rest. A unit of sand is large enough to fill one tile of air in your scan.
    // 
    // A unit of sand always falls down one step if possible. If the tile immediately below is blocked (by rock or sand), the unit of sand attempts to instead move diagonally one step down and to the left. If that tile is blocked, the unit of sand attempts to instead move diagonally one step down and to the right. Sand keeps moving as long as it is able to do so, at each step trying to move down, then down-left, then down-right. If all three possible destinations are blocked, the unit of sand comes to rest and no longer moves, at which point the next unit of sand is created back at the source.
    // 
    // So, drawing sand that has come to rest as o, the first unit of sand simply falls straight down and then stops:
    // 
    // ......+...
    // ..........
    // ..........
    // ..........
    // ....#...##
    // ....#...#.
    // ..###...#.
    // ........#.
    // ......o.#.
    // #########.
    // 
    // The second unit of sand then falls straight down, lands on the first one, and then comes to rest to its left:
    // 
    // ......+...
    // ..........
    // ..........
    // ..........
    // ....#...##
    // ....#...#.
    // ..###...#.
    // ........#.
    // .....oo.#.
    // #########.
    // 
    // After a total of five units of sand have come to rest, they form this pattern:
    // 
    // ......+...
    // ..........
    // ..........
    // ..........
    // ....#...##
    // ....#...#.
    // ..###...#.
    // ......o.#.
    // ....oooo#.
    // #########.
    // 
    // After a total of 22 units of sand:
    // 
    // ......+...
    // ..........
    // ......o...
    // .....ooo..
    // ....#ooo##
    // ....#ooo#.
    // ..###ooo#.
    // ....oooo#.
    // ...ooooo#.
    // #########.
    // 
    // Finally, only two more units of sand can possibly come to rest:
    // 
    // ......+...
    // ..........
    // ......o...
    // .....ooo..
    // ....#ooo##
    // ...o#ooo#.
    // ..###ooo#.
    // ....oooo#.
    // .o.ooooo#.
    // #########.
    // 
    // Once all 24 units of sand shown above have come to rest, all further sand flows out the bottom, falling into the endless void. Just for fun, the path any new sand takes before falling forever is shown here with ~:
    // 
    // .......+...
    // .......~...
    // ......~o...
    // .....~ooo..
    // ....~#ooo##
    // ...~o#ooo#.
    // ..~###ooo#.
    // ..~..oooo#.
    // .~o.ooooo#.
    // ~#########.
    // ~..........
    // ~..........
    // ~..........
    // 
    // Using your scan, simulate the falling sand. How many units of sand come to rest before sand starts flowing into the abyss below?
    private static void part1(List<StonePath> paths) {
        Cave cave = new Cave(paths);
        cave.print();
        int sandCount = 0;
        Coordinate sandPosition;
        while ((sandPosition = cave.simulateSand(false)) != null) {
            cave.addCaveObject(sandPosition.x(), sandPosition.y(), CaveObject.SAND);
            // cave.print();
            sandCount++;
        }
        // Display the cave
        System.out.println("\nCave representation after " + sandCount + " units of sand:");
        cave.print();
        System.out.println("Total sand units: " + sandCount);
    }

    private static List<StonePath> parsePaths() throws Exception {
        List<StonePath> paths = new ArrayList<>();

        // List<String> lines = Arrays.asList(input.trim().split("\n"));
        List<String> lines = Files.readAllLines(java.nio.file.Path.of(Day14.class.getResource("/14.txt").toURI()));

        for (String line : lines) {
            paths.add(parseLine(line));
        }

        return paths;
    }

    private static StonePath parseLine(String line) {
        String[] coordinatePairs = line.split(" -> ");

        List<Coordinate> coordinates = new ArrayList<>();

        for (String pair : coordinatePairs) {
            String[] values = pair.split(",");

            if (values.length != 2) {
                throw new IllegalArgumentException("Invalid coordinate pair: " + pair);
            }

            try {
                int x = Integer.parseInt(values[0]);
                int y = Integer.parseInt(values[1]);

                coordinates.add(new Coordinate(x, y));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid coordinate value in pair: " + pair, e);
            }
        }

        return new StonePath(coordinates);
    }

}
