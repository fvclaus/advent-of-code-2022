
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
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

    record Path(List<Coordinate> coordinates) {

        @Override
        public String toString() {
            return this.coordinates.stream().map(Object::toString).collect(Collectors.joining(" -> "));
        }
    }

    enum CaveObject {
        STONE('#'),
        SAND('o');

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

        final int minX;

        public Cave(List<Path> paths) {
            var minX = paths.stream()
                    .flatMap(path -> path.coordinates().stream())
                    .reduce(
                            Integer.MAX_VALUE,
                            (mins, coord) -> Math.min(mins, coord.x()),
                            (a, b) -> Math.min(a, b)
                    );
            this.minX = minX < 5 ? 0 : minX - 5;

            for (var path : paths) {
                this.addPath(path);
            }
        }

        public void addCaveObject(int x, int y, CaveObject caveObject) {
            while (grid.size() < (y + 1)) {
                grid.add(new ArrayList<>());
            }
            var line = grid.get(y);
            for (var i = 0; i < x; i++) {
                if (line.size() < x + 1) {
                    line.add(null);
                }
            }
            if (line.size() >= x + 1) {
                line.set(x, caveObject);
            } else {
                line.add(caveObject);
            }
        }

        private void addPath(Path path) {
            List<Coordinate> coords = path.coordinates();

            for (int i = 0; i < coords.size() - 1; i++) {
                Coordinate current = coords.get(i);
                Coordinate next = coords.get(i + 1);

                drawLine(current, next);
            }
        }

        private void drawLine(Coordinate from, Coordinate to) {

            int x1 = from.x() - minX;
            int y1 = from.y();
            int x2 = to.x() - minX;
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

        private CaveObject get(int x, int y) {
            var line = this.grid.get(y);
            if (line.size() < (x + 1)) {
                return null;
            }
            return line.get(x);
        }

        public Coordinate simulateSand() {
            int x = 500 - minX;
            int y = 0;

            if (this.get(x, y) != null) {
                return null;
            }
            while (true) {

                if ((y + 1) >= grid.size()) {
                    return null;
                } else if (get(x, y + 1) == null) {
                    // Sand falls down
                    y += 1;
                } else if (get(x - 1, y + 1) == null) {
                    // Sand falls down to the left
                    y += 1;
                    x -= 1;
                } else if (get(x + 1, y + 1) == null) {
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
            for (var line : grid) {
                for (CaveObject object : line) {
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
        List<Path> paths = parsePaths();

        System.out.println("Original input:");
        System.out.println(input);

        System.out.println("\nReconstructed input from parsed data:");
        for (Path path : paths) {
            System.out.println(path);
        }

        Cave cave = new Cave(paths);
        cave.print();

        int sandCount = 0;
        Coordinate sandPosition;
        while ((sandPosition = cave.simulateSand()) != null) {
            cave.addCaveObject(sandPosition.x(), sandPosition.y(), CaveObject.SAND);
            // cave.print();
            sandCount++;
        }
        // Display the cave
        System.out.println("\nCave representation after " + sandCount + " units of sand:");
        cave.print();
        System.out.println("Total sand units: " + sandCount);
    }

    private static List<Path> parsePaths() throws Exception {
        List<Path> paths = new ArrayList<>();

        // List<String> lines = Arrays.asList(input.trim().split("\n"));
        List<String> lines = Files.readAllLines(java.nio.file.Path.of(Day14.class.getResource("/14.txt").toURI()));

        for (String line : lines) {
            paths.add(parseLine(line));
        }

        return paths;
    }

    private static Path parseLine(String line) {
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

        return new Path(coordinates);
    }

}
