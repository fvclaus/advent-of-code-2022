
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private record Position(int x, int y) {

        Position  {
            if (x < 0 || x > 6) {
                throw new IllegalArgumentException("x must be >= 0 and y must be <= 6");
            }
        }
    }

    private static class Tower {

        private final Set<Position> positions = new HashSet<>(List.of(new Position(0, 0), new Position(1, 0), new Position(2, 0), new Position(3, 0), new Position(4, 0), new Position(5, 0), new Position(6, 0)));
        private int maxY = 0;

        public void addPositions(Collection<Position> newPositions) {
            for (var pos : newPositions) {
                if (pos.y > maxY) {
                    maxY = pos.y;
                }
                this.positions.add(pos);
            }
        }

        public boolean contains(Position pos) {
            return this.positions.contains(pos);
        }

    }

    private static class Rock {

        private List<Position> positions;
        private final Tower tower;

        public Rock(long i, Tower bottom) {
            var y = bottom.maxY + 4;
            List<Position> immutablePositions;
            switch ((int) (i % 5)) {
                case 0 -> {
                    // ####
                    immutablePositions = List.of(new Position(2, y), new Position(3, y), new Position(4, y), new Position(5, y));
                }
                case 1 -> {
                    // .#.
                    // ###
                    // .#.
                    immutablePositions = List.of(new Position(3, y + 2), new Position(2, y + 1), new Position(3, y + 1), new Position(4, y + 1), new Position(3, y));
                }
                case 2 -> {
                    // ..#
                    // ..#
                    // ###                    
                    immutablePositions = List.of(new Position(4, y + 2), new Position(4, y + 1), new Position(2, y), new Position(3, y), new Position(4, y));
                }
                case 3 -> {
                    // #
                    // #
                    // #
                    // #                    
                    immutablePositions = List.of(new Position(2, y + 3), new Position(2, y + 2), new Position(2, y + 1), new Position(2, y));
                }

                case 4 -> {
                    // ##
                    // ##                    
                    immutablePositions = List.of(new Position(2, y + 1), new Position(3, y + 1), new Position(2, y), new Position(3, y));
                }
                default -> {
                    throw new IllegalArgumentException(String.valueOf(i % 5));
                }
            }
            this.positions = new ArrayList<>(immutablePositions);
            this.tower = bottom;
        }

        public boolean move(Direction dir) {
            return switch (dir) {
                case Direction.RIGHT -> {
                    var canNotMove = this.positions.stream().anyMatch(p -> p.x == 6 || tower.contains(new Position(p.x + 1, p.y)));
                    if (!canNotMove) {
                        this.positions = positions.stream().map(p -> new Position(p.x + 1, p.y)).toList();
                    }
                    yield false;
                }
                case Direction.LEFT -> {
                    var canNotMove = this.positions.stream().anyMatch(p -> p.x == 0 || tower.contains(new Position(p.x - 1, p.y)));
                    if (!canNotMove) {
                        this.positions = positions.stream().map(p -> new Position(p.x - 1, p.y)).toList();
                    }
                    yield false;

                }
                case Direction.DOWN -> {
                    var isAtRest = this.positions.stream().anyMatch(p -> tower.contains(new Position(p.x, p.y - 1)));
                    if (isAtRest) {
                        tower.addPositions(positions);
                    } else {
                        this.positions = positions.stream().map(p -> new Position(p.x, p.y - 1)).toList();
                    }
                    yield isAtRest;
                }
            };
        }
    }

    public static void main(String[] args) throws Exception {
        var tower = new Tower();
        var numberOfRocks = 0l;
        var jetDirections = parseJetPattern();
        var numberOfJets = 0;

        while (numberOfRocks < 2022) {
            var rock = new Rock(numberOfRocks, tower);
            var atRest = false;
            while (!atRest) {
                var jetDirection = jetDirections.get(numberOfJets);
                numberOfJets = (numberOfJets + 1) % jetDirections.size();
                rock.move(jetDirection);
                atRest = rock.move(Direction.DOWN);
            }
            numberOfRocks += 1;
        }
        System.out.println(tower.maxY);
    }
}
