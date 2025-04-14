import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day23 {
    private static final String input = """
                ..............
                ..............
                .......#......
                .....###.#....
                ...#...#.#....
                ....#...##....
                ...#.###......
                ...##.#.##....
                ....#..#......
                ..............
                ..............
                ..............
            """;

    record Position(int x, int y) {
    }

    private static final Direction[] DIRECTION_ORDER = { Direction.NORTH, Direction.SOUTH, Direction.WEST,
            Direction.EAST };

    enum Direction {
        NORTH {
            @Override
            public List<Position> getAdjacentPositions(Position position) {
                return List.of(
                        new Position(position.x() - 1, position.y() - 1),
                        new Position(position.x(), position.y() - 1),
                        new Position(position.x() + 1, position.y() - 1));
            }
        },
        SOUTH {
            @Override
            public List<Position> getAdjacentPositions(Position position) {
                return List.of(
                        new Position(position.x() - 1, position.y() + 1),
                        new Position(position.x(), position.y() + 1),
                        new Position(position.x() + 1, position.y() + 1));
            }
        },
        EAST {
            @Override
            public List<Position> getAdjacentPositions(Position position) {
                return List.of(
                        new Position(position.x() + 1, position.y() - 1),
                        new Position(position.x() + 1, position.y()),
                        new Position(position.x() + 1, position.y() + 1));
            }
        },
        WEST {
            @Override
            public List<Position> getAdjacentPositions(Position position) {
                return List.of(
                        new Position(position.x() - 1, position.y() - 1),
                        new Position(position.x() - 1, position.y()),
                        new Position(position.x() - 1, position.y() + 1));
            }
        };

        public abstract List<Position> getAdjacentPositions(Position position);
    }

    record Elf(int id) {
    }

    public static void main(String[] args) throws Exception {
        var elves = parseInput();
        print(elves);

        var round = 0;
        while (true) {
            Map<Position, List<Elf>> newPositions = new HashMap<>();
            Map<Position, Elf> posToElf = elves.entrySet().stream()
                    .collect(Collectors.toMap(e -> e.getValue(), e -> e.getKey()));
            int numberOfMovingElfs = 0;
            for (Elf elf : elves.keySet()) {
                Position elfPos = elves.get(elf);
                boolean hasAnyNeighbors = Stream.of(
                        new Position(elfPos.x() - 1, elfPos.y() - 1), new Position(elfPos.x(), elfPos.y() - 1),
                        new Position(elfPos.x() + 1, elfPos.y() - 1),
                        new Position(elfPos.x() - 1, elfPos.y()), new Position(elfPos.x() + 1, elfPos.y()),
                        new Position(elfPos.x() - 1, elfPos.y() + 1), new Position(elfPos.x(), elfPos.y() + 1),
                        new Position(elfPos.x() + 1, elfPos.y() + 1)).anyMatch(pos -> posToElf.containsKey(pos));

                if (!hasAnyNeighbors) {
                    continue; // Skip this elf - it doesn't need to move
                }
                numberOfMovingElfs++;
                for (var i = 0; i < 4; i++) {
                    var directionIndex = (round + i) % 4;
                    var direction = DIRECTION_ORDER[directionIndex];
                    var positions = direction.getAdjacentPositions(elves.get(elf));
                    boolean isMovePossible = positions.stream().allMatch(pos -> !posToElf.containsKey(pos));
                    if (isMovePossible) {
                        var nextPos = positions.get(1);
                        newPositions.computeIfAbsent(nextPos, _ -> new ArrayList<>()).add(elf);
                        break;
                    }
                }
            }
            if (numberOfMovingElfs == 0) {
                break;
            }
            for (var pos : newPositions.keySet()) {
                var elfsAtPos = newPositions.get(pos);
                if (elfsAtPos.size() == 1) {
                    elves.put(elfsAtPos.get(0), pos);
                }
            }
            System.out.println("----------------------------");
            print(elves);
            round++;
        }
        int emptyGroundTiles = countEmptyGroundTiles(elves);
        System.out.println("Empty ground tiles: " + emptyGroundTiles);
        System.out.println("Rounds: " + round);
    }

    static int countEmptyGroundTiles(Map<Elf, Position> elves) {
        Set<Position> positions = elves.values().stream().collect(Collectors.toSet());
        int minX = elves.values().stream().mapToInt(Position::x).min().getAsInt();
        int maxX = elves.values().stream().mapToInt(Position::x).max().getAsInt();
        int minY = elves.values().stream().mapToInt(Position::y).min().getAsInt();
        int maxY = elves.values().stream().mapToInt(Position::y).max().getAsInt();

        int totalTiles = (maxX - minX + 1) * (maxY - minY + 1);
        int occupiedTiles = positions.size();
        return totalTiles - occupiedTiles;
    }

    static Map<Elf, Position> parseInput() throws Exception {
        Map<Elf, Position> elves = new HashMap<>();
        // String[] lines = input.split("\n");
        String[] lines = Files.readAllLines(Path.of(Day23.class.getResource("/23.txt").toURI()))
                .toArray(new String[] {});
        int elfId = 0;

        for (int y = 0; y < lines.length; y++) {
            String line = lines[y];
            for (int x = 0; x < line.length(); x++) {
                if (line.charAt(x) == '#') {
                    elves.put(new Elf(elfId++), new Position(x, y));
                }
            }
        }
        return elves;
    }

    static void print(Map<Elf, Position> elves) {
        Set<Position> positions = elves.values().stream().collect(Collectors.toSet());
        int minX = elves.values().stream().mapToInt(Position::x).min().getAsInt();
        int maxX = elves.values().stream().mapToInt(Position::x).max().getAsInt();
        int minY = elves.values().stream().mapToInt(Position::y).min().getAsInt();
        int maxY = elves.values().stream().mapToInt(Position::y).max().getAsInt();

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                System.out.print(positions.contains(new Position(x, y)) ? '#' : '.');
            }
            System.out.println();
        }
    }
}
