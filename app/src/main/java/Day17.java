
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class Day17 {

    private static enum Direction {
        LEFT,
        RIGHT,
        DOWN;

        public static Direction of(int value) {
            return switch (value) {
                case '>' ->
                    Direction.RIGHT;
                case '<' ->
                    Direction.LEFT;
                case 'v' ->
                    Direction.DOWN;
                default ->
                    throw new IllegalArgumentException(String.valueOf(value));
            };
        }
    }

    public static List<Direction> parseJetPattern() throws Exception {
        // var line = ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>";
        var line = Files.readString(Path.of(Day17.class.getResource("/17.txt").toURI())).trim();
        return line.chars().mapToObj(Direction::of).toList();
    }

    private static class Position {

        public int x;
        public int y;

        public Position(int x, int y) {
            if (x < 0 || x > 6) {
                throw new IllegalArgumentException("x must be >= 0 and y must be <= 6");
            }
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Position position = (Position) obj;
            return x == position.x && y == position.y;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(x) * 31 + Long.hashCode(y);
        }
    }

    private static enum Orientation {
        NORTH,
        EAST,
        SOUTH,
        WEST;
    }

    private static class Tower {

        private static final int VIEW_SIZE = 1_000_000;
        private final boolean[][] view = new boolean[VIEW_SIZE][7];
        private long reductions = 0;
        private int maxY = 0;

        public Tower() {
            Arrays.fill(view[0], true);
        }

        public void addPositions(Position[] newPositions) {
            for (int i = 0; i < newPositions.length; i++) {
                var pos = newPositions[i];
                maxY = Math.max(pos.y, maxY);
                if (view[pos.y][pos.x]) {
                    throw new IllegalArgumentException("Should not happen");
                }
                view[pos.y][pos.x] = true;
            }
            if (maxY >= (VIEW_SIZE - 100)) {
                this.compact();
            }
        }

        public boolean contains(int x, int y) {
            return view[y][x];
        }

        private void compact() {
            // Start on left wall
            var pos = new Position(0, maxY + 1);
            var orientation = Orientation.SOUTH;
            var minY = Integer.MAX_VALUE;
            while (!(pos.x == 6 && orientation == Orientation.EAST)) {
                if (pos.y < 1) {
                    throw new IllegalStateException("Should not happen");
                }
                minY = Math.min(minY, pos.y);
                switch (orientation) {
                    case NORTH -> {
                        if (pos.x == 6) {
                            throw new IllegalStateException("Should not happen");
                        }
                        var wall = view[pos.y + 1][pos.x];
                        var gap = !view[pos.y + 1][pos.x + 1];
                        if (wall) {
                            orientation = Orientation.WEST;
                        } else if (gap) {

                            orientation = Orientation.EAST;
                            pos.x = pos.x + 1;
                            pos.y = pos.y + 1;
                        } else {

                            pos.y += 1;
                        }
                    }
                    case EAST -> {
                        if (pos.x == 6) {
                            throw new IllegalStateException("Should not happen");
                        }
                        var wall = view[pos.y][pos.x + 1];
                        var gap = !view[pos.y - 1][pos.x + 1];
                        if (wall) {
                            orientation = Orientation.NORTH;
                        } else if (gap) {

                            orientation = Orientation.SOUTH;
                            pos.x += 1;
                            pos.y -= 1;
                        } else {

                            pos.x += 1;
                        }
                    }
                    case SOUTH -> {
                        var wall = view[pos.y - 1][pos.x];
                        // Walking along left wall. Don't check for gaps
                        var gap = pos.x >= 1 && !view[pos.y - 1][pos.x - 1];
                        if (wall) {
                            orientation = Orientation.EAST;
                        } else if (gap) {
                            orientation = Orientation.WEST;
                            pos.y -= 1;
                            pos.x -= 1;
                        } else {
                            pos.y -= 1;
                        }
                    }
                    case WEST -> {
                        // Facing left wall or stone
                        var wall = pos.x == 0 || view[pos.y][pos.x - 1];
                        var gap = !view[pos.y + 1][pos.x - 1];
                        if (wall) {
                            orientation = Orientation.SOUTH;
                        } else if (gap) {
                            orientation = Orientation.NORTH;
                            pos.y += 1;
                            pos.x -= 1;
                        } else {
                            pos.x -= 1;
                        }
                    }
                }
            }

            // Include the surface 
            minY -= 1;

            if (minY > 0) {
                // print();
                var numberOfRowsToKeep = (maxY + 1) - minY;
                // System.out.println("Compacting " + minY);
                for (int i = 0; i < view.length; i++) {
                    if (i <= numberOfRowsToKeep) {
                        System.arraycopy(view[i + minY], 0, view[i], 0, view[i + minY].length);
                    } else {
                        Arrays.fill(view[i], false);
                    }
                }

                this.maxY = maxY - minY;
                reductions += minY;
            }
        }

        private void print() {
            System.out.println("""
                                           ----------------------------------------------
                                           ----------------------------------------------
                                           """);
            for (var y = maxY + 20; y >= 0; y--) {
                var row = view[y];
                System.out.print("|");
                for (int i = 0; i < row.length; i++) {
                    System.out.print(row[i] ? (String) "#" : ".");
                }
                System.out.print("|\n");
            }
        }

    }

    private static abstract class Rock {

        protected Position[] positions;
        protected Map<Direction, Position[]> edges;
        protected final Tower tower;

        public Rock(Tower tower) {
            this.tower = tower;
        }

        public abstract void reset();

        public boolean move(Direction dir) {
            var canMoveRoot = switch (dir) {
                case Direction.RIGHT -> {
                    var canMove = true;
                    var edge = edges.get(dir);
                    for (var i = 0; i < edge.length; i++) {
                        var p = edge[i];
                        if (p.x == 6 || tower.contains(p.x + 1, p.y)) {
                            canMove = false;
                            break;
                        }
                    }
                    if (canMove) {
                        for (int i = 0; i < positions.length; i++) {
                            positions[i].x += 1;
                        }
                    }
                    yield false;
                }
                case Direction.LEFT -> {
                    var canMove = true;
                    var edge = edges.get(dir);
                    for (int i = 0; i < edge.length; i++) {
                        var p = edge[i];
                        if (p.x == 0 || tower.contains(p.x - 1, p.y)) {
                            canMove = false;
                            break;
                        }
                    }
                    if (canMove) {
                        for (int i = 0; i < positions.length; i++) {
                            positions[i].x -= 1;
                        }
                    }
                    yield false;
                }
                case Direction.DOWN -> {
                    var isAtRest = false;
                    var edge = edges.get(dir);
                    for (int i = 0; i < edge.length; i++) {
                        var p = edge[i];
                        if (tower.contains(p.x, p.y - 1)) {
                            isAtRest = true;
                            break;
                        }
                    }
                    if (isAtRest) {
                        tower.addPositions(positions);
                    } else {
                        for (int i = 0; i < positions.length; i++) {
                            positions[i].y -= 1;
                        }
                    }
                    yield isAtRest;
                }
            };
            return canMoveRoot;
        }
    }

    private static class HorizontalRock extends Rock {

        public HorizontalRock(Tower tower) {
            super(tower);
            this.positions = new Position[]{
                new Position(0, 0), new Position(0, 0),
                new Position(0, 0), new Position(0, 0)
            };
            edges = Map.of(
                    Direction.RIGHT, new Position[]{positions[3]},
                    Direction.LEFT, new Position[]{positions[0]},
                    Direction.DOWN, positions
            );
        }

        @Override
        public void reset() {
            int y = tower.maxY + 4;

            // ####
            positions[0].x = 2;
            positions[0].y = y;

            positions[1].x = 3;
            positions[1].y = y;

            positions[2].x = 4;
            positions[2].y = y;

            positions[3].x = 5;
            positions[3].y = y;
        }
    }

    private static class PlusRock extends Rock {

        public PlusRock(Tower tower) {
            super(tower);
            this.positions = new Position[]{
                new Position(0, 0), new Position(0, 0), new Position(0, 0),
                new Position(0, 0), new Position(0, 0)
            };

            edges = Map.of(
                    Direction.RIGHT, new Position[]{positions[0], positions[3], positions[4]},
                    Direction.LEFT, new Position[]{positions[0], positions[1], positions[4]},
                    Direction.DOWN, new Position[]{positions[1], positions[3], positions[4]}
            );
        }

        @Override
        public void reset() {
            int y = tower.maxY + 4;

            // .#.
            // ###
            // .#.
            positions[0].x = 3;
            positions[0].y = y + 2;

            positions[1].x = 2;
            positions[1].y = y + 1;

            positions[2].x = 3;
            positions[2].y = y + 1;

            positions[3].x = 4;
            positions[3].y = y + 1;

            positions[4].x = 3;
            positions[4].y = y;
        }
    }

    private static class LRock extends Rock {

        public LRock(Tower tower) {
            super(tower);
            this.positions = new Position[]{
                new Position(0, 0), new Position(0, 0), new Position(0, 0),
                new Position(0, 0), new Position(0, 0)
            };
            edges = Map.of(
                    Direction.RIGHT, new Position[]{positions[0], positions[1], positions[4]},
                    Direction.LEFT, new Position[]{positions[0], positions[1], positions[2]},
                    Direction.DOWN, new Position[]{positions[2], positions[3], positions[4]}
            );
        }

        @Override
        public void reset() {
            int y = tower.maxY + 4;

            // ..#
            // ..#
            // ###
            positions[0].x = 4;
            positions[0].y = y + 2;

            positions[1].x = 4;
            positions[1].y = y + 1;

            positions[2].x = 2;
            positions[2].y = y;

            positions[3].x = 3;
            positions[3].y = y;

            positions[4].x = 4;
            positions[4].y = y;
        }
    }

    private static class VerticalRock extends Rock {

        public VerticalRock(Tower tower) {
            super(tower);
            this.positions = new Position[]{
                new Position(0, 0), new Position(0, 0),
                new Position(0, 0), new Position(0, 0)
            };
            edges = Map.of(
                    Direction.RIGHT, positions,
                    Direction.LEFT, positions,
                    Direction.DOWN, new Position[]{positions[3]}
            );
        }

        @Override
        public void reset() {
            int y = tower.maxY + 4;

            // #
            // #
            // #
            // #
            positions[0].x = 2;
            positions[0].y = y + 3;

            positions[1].x = 2;
            positions[1].y = y + 2;

            positions[2].x = 2;
            positions[2].y = y + 1;

            positions[3].x = 2;
            positions[3].y = y;
        }
    }

    private static class SquareRock extends Rock {

        public SquareRock(Tower tower) {
            super(tower);
            this.positions = new Position[]{
                new Position(0, 0), new Position(0, 0),
                new Position(0, 0), new Position(0, 0)
            };

            edges = Map.of(
                    Direction.RIGHT, new Position[]{positions[1], positions[3]},
                    Direction.LEFT, new Position[]{positions[0], positions[2]},
                    Direction.DOWN, new Position[]{positions[2], positions[3]}
            );
        }

        @Override
        public void reset() {
            int y = tower.maxY + 4;

            // ##
            // ##
            positions[0].x = 2;
            positions[0].y = y + 1;

            positions[1].x = 3;
            positions[1].y = y + 1;

            positions[2].x = 2;
            positions[2].y = y;

            positions[3].x = 3;
            positions[3].y = y;
        }
    }

    // 
    // Your handheld device has located an alternative exit from the cave for you and the elephants. The ground is rumbling almost continuously now, but the strange valves bought you some time. It's definitely getting warmer in here, though.
    // 
    // The tunnels eventually open into a very tall, narrow chamber. Large, oddly-shaped rocks are falling into the chamber from above, presumably due to all the rumbling. If you can't work out where the rocks will fall next, you might be crushed!
    // 
    // The five types of rocks have the following peculiar shapes, where # is rock and . is empty space:
    // 
    // ####
    // 
    // .#.
    // ###
    // .#.
    // 
    // ..#
    // ..#
    // ###
    // 
    // #
    // #
    // #
    // #
    // 
    // ##
    // ##
    // 
    // The rocks fall in the order shown above: first the - shape, then the + shape, and so on. Once the end of the list is reached, the same order repeats: the - shape falls first, sixth, 11th, 16th, etc.
    // 
    // The rocks don't spin, but they do get pushed around by jets of hot gas coming out of the walls themselves. A quick scan reveals the effect the jets of hot gas will have on the rocks as they fall (your puzzle input).
    // 
    // For example, suppose this was the jet pattern in your cave:
    // 
    // >>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>
    // 
    // In jet patterns, < means a push to the left, while > means a push to the right. The pattern above means that the jets will push a falling rock right, then right, then right, then left, then left, then right, and so on. If the end of the list is reached, it repeats.
    // 
    // The tall, vertical chamber is exactly seven units wide. Each rock appears so that its left edge is two units away from the left wall and its bottom edge is three units above the highest rock in the room (or the floor, if there isn't one).
    // 
    // After a rock appears, it alternates between being pushed by a jet of hot gas one unit (in the direction indicated by the next symbol in the jet pattern) and then falling one unit down. If any movement would cause any part of the rock to move into the walls, floor, or a stopped rock, the movement instead does not occur. If a downward movement would have caused a falling rock to move into the floor or an already-fallen rock, the falling rock stops where it is (having landed on something) and a new rock immediately begins falling.
    // 
    // Drawing falling rocks with @ and stopped rocks with #, the jet pattern in the example above manifests as follows:
    // 
    // The first rock begins falling:
    // |..@@@@.|
    // |.......|
    // |.......|
    // |.......|
    // +-------+
    // 
    // Jet of gas pushes rock right:
    // |...@@@@|
    // |.......|
    // |.......|
    // |.......|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |...@@@@|
    // |.......|
    // |.......|
    // +-------+
    // 
    // Jet of gas pushes rock right, but nothing happens:
    // |...@@@@|
    // |.......|
    // |.......|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |...@@@@|
    // |.......|
    // +-------+
    // 
    // Jet of gas pushes rock right, but nothing happens:
    // |...@@@@|
    // |.......|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |...@@@@|
    // +-------+
    // 
    // Jet of gas pushes rock left:
    // |..@@@@.|
    // +-------+
    // 
    // Rock falls 1 unit, causing it to come to rest:
    // |..####.|
    // +-------+
    // 
    // A new rock begins falling:
    // |...@...|
    // |..@@@..|
    // |...@...|
    // |.......|
    // |.......|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Jet of gas pushes rock left:
    // |..@....|
    // |.@@@...|
    // |..@....|
    // |.......|
    // |.......|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |..@....|
    // |.@@@...|
    // |..@....|
    // |.......|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Jet of gas pushes rock right:
    // |...@...|
    // |..@@@..|
    // |...@...|
    // |.......|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |...@...|
    // |..@@@..|
    // |...@...|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Jet of gas pushes rock left:
    // |..@....|
    // |.@@@...|
    // |..@....|
    // |.......|
    // |..####.|
    // +-------+
    // 
    // Rock falls 1 unit:
    // |..@....|
    // |.@@@...|
    // |..@....|
    // |..####.|
    // +-------+
    // 
    // Jet of gas pushes rock right:
    // |...@...|
    // |..@@@..|
    // |...@...|
    // |..####.|
    // +-------+
    // 
    // Rock falls 1 unit, causing it to come to rest:
    // |...#...|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // A new rock begins falling:
    // |....@..|
    // |....@..|
    // |..@@@..|
    // |.......|
    // |.......|
    // |.......|
    // |...#...|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // The moment each of the next few rocks begins falling, you would see this:
    // 
    // |..@....|
    // |..@....|
    // |..@....|
    // |..@....|
    // |.......|
    // |.......|
    // |.......|
    // |..#....|
    // |..#....|
    // |####...|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |..@@...|
    // |..@@...|
    // |.......|
    // |.......|
    // |.......|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |..@@@@.|
    // |.......|
    // |.......|
    // |.......|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |...@...|
    // |..@@@..|
    // |...@...|
    // |.......|
    // |.......|
    // |.......|
    // |.####..|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |....@..|
    // |....@..|
    // |..@@@..|
    // |.......|
    // |.......|
    // |.......|
    // |..#....|
    // |.###...|
    // |..#....|
    // |.####..|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |..@....|
    // |..@....|
    // |..@....|
    // |..@....|
    // |.......|
    // |.......|
    // |.......|
    // |.....#.|
    // |.....#.|
    // |..####.|
    // |.###...|
    // |..#....|
    // |.####..|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |..@@...|
    // |..@@...|
    // |.......|
    // |.......|
    // |.......|
    // |....#..|
    // |....#..|
    // |....##.|
    // |....##.|
    // |..####.|
    // |.###...|
    // |..#....|
    // |.####..|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // |..@@@@.|
    // |.......|
    // |.......|
    // |.......|
    // |....#..|
    // |....#..|
    // |....##.|
    // |##..##.|
    // |######.|
    // |.###...|
    // |..#....|
    // |.####..|
    // |....##.|
    // |....##.|
    // |....#..|
    // |..#.#..|
    // |..#.#..|
    // |#####..|
    // |..###..|
    // |...#...|
    // |..####.|
    // +-------+
    // 
    // To prove to the elephants your simulation is accurate, they want to know how tall the tower will get after 2022 rocks have stopped (but before the 2023rd rock begins falling). In this example, the tower of rocks will be 3068 units tall.
    // 
    // How many units tall will the tower of rocks be after 2022 rocks have stopped falling?
    // 
    // Your puzzle answer was 3055.
    // 
    // The first half of this puzzle is complete! It provides one gold star: *
    // --- Part Two ---
    // 
    // The elephants are not impressed by your simulation. They demand to know how tall the tower will be after 1000000000000 rocks have stopped! Only then will they feel confident enough to proceed through the cave.
    // 
    // In the example above, the tower would be 1514285714288 units tall!
    // 
    // How tall will the tower be after 1000000000000 rocks have stopped?
    public static void main(String[] args) throws Exception {
        var tower = new Tower();
        var numberOfRocks = 0l;
        var jetDirections = parseJetPattern();
        var numberOfJets = 0;

        Rock[] rockShapes = new Rock[]{
            new HorizontalRock(tower),
            new PlusRock(tower),
            new LRock(tower),
            new VerticalRock(tower),
            new SquareRock(tower)
        };

        // Unfortunately too slow for 1_000_000_000 iterations. This requires a different solution
        while (numberOfRocks < 1_000_000_000_000f) {
            var rock = rockShapes[(int) (numberOfRocks % 5)];
            rock.reset();
            var atRest = false;
            if (numberOfRocks % 10_000_000L == 0) {
                System.out.println("Progress: " + (numberOfRocks / 1_000_000_000_000f) * 100 + " %");
            }
            while (!atRest) {
                var jetDirection = jetDirections.get(numberOfJets);
                numberOfJets = (numberOfJets + 1) % jetDirections.size();
                rock.move(jetDirection);
                atRest = rock.move(Direction.DOWN);
            }
            numberOfRocks += 1;
        }
        System.out.println(tower.maxY + tower.reductions);
    }
}
