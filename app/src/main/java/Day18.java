import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day18 {

    public record Coordinate(int x, int y, int z) {
        public static List<Coordinate> parseInput(String input) {
            return input.lines()
                    .map(line -> {
                        String[] parts = line.trim().split(",");
                        return new Coordinate(
                                Integer.parseInt(parts[0]),
                                Integer.parseInt(parts[1]),
                                Integer.parseInt(parts[2]));
                    })
                    .collect(Collectors.toList());
        }

        public List<Coordinate> getAdjacent() {
            return List.of(
                    new Coordinate(x + 1, y, z),
                    new Coordinate(x - 1, y, z),
                    new Coordinate(x, y + 1, z),
                    new Coordinate(x, y - 1, z),
                    new Coordinate(x, y, z + 1),
                    new Coordinate(x, y, z - 1));
        }
    }

    private static String input = """
                    2,2,2
                    1,2,2
                    3,2,2
                    2,1,2
                    2,3,2
                    2,2,1
                    2,2,3
                    2,2,4
                    2,2,6
                    1,2,5
                    3,2,5
                    2,1,5
                    2,3,5
            """;

    // --- Day 18: Boiling Boulders ---
    //
    // You and the elephants finally reach fresh air. You've emerged near the base
    // of a large volcano that seems to be actively erupting! Fortunately, the lava
    // seems to be flowing away from you and toward the ocean.
    //
    // Bits of lava are still being ejected toward you, so you're sheltering in the
    // cavern exit a little longer. Outside the cave, you can see the lava landing
    // in a pond and hear it loudly hissing as it solidifies.
    //
    // Depending on the specific compounds in the lava and speed at which it cools,
    // it might be forming obsidian! The cooling rate should be based on the surface
    // area of the lava droplets, so you take a quick scan of a droplet as it flies
    // past you (your puzzle input).
    //
    // Because of how quickly the lava is moving, the scan isn't very good; its
    // resolution is quite low and, as a result, it approximates the shape of the
    // lava droplet with 1x1x1 cubes on a 3D grid, each given as its x,y,z position.
    //
    // To approximate the surface area, count the number of sides of each cube that
    // are not immediately connected to another cube. So, if your scan were only two
    // adjacent cubes like 1,1,1 and 2,1,1, each cube would have a single side
    // covered and five sides exposed, a total surface area of 10 sides.
    //
    // Here's a larger example:
    //
    // 2,2,2
    // 1,2,2
    // 3,2,2
    // 2,1,2
    // 2,3,2
    // 2,2,1
    // 2,2,3
    // 2,2,4
    // 2,2,6
    // 1,2,5
    // 3,2,5
    // 2,1,5
    // 2,3,5
    //
    // In the above example, after counting up all the sides that aren't connected
    // to another cube, the total surface area is 64.
    //
    // What is the surface area of your scanned lava droplet?
    //
    // Your puzzle answer was 4310.
    // --- Part Two ---
    //
    // Something seems off about your calculation. The cooling rate depends on
    // exterior surface area, but your calculation also included the surface area of
    // air pockets trapped in the lava droplet.
    //
    // Instead, consider only cube sides that could be reached by the water and
    // steam as the lava droplet tumbles into the pond. The steam will expand to
    // reach as much as possible, completely displacing any air on the outside of
    // the lava droplet but never expanding diagonally.
    //
    // In the larger example above, exactly one cube of air is trapped within the
    // lava droplet (at 2,2,5), so the exterior surface area of the lava droplet is
    // 58.
    //
    // What is the exterior surface area of your scanned lava droplet?

    public static void main(String[] args) throws Exception {
        // var coords = Coordinate.parseInput(input);
        var coords = Coordinate.parseInput(Files.readString(Path.of(Day18.class.getResource("/18.txt").toURI())));
        Map<Coordinate, Integer> openSides = new HashMap<>();

        for (Coordinate coord : coords) {
            if (openSides.containsKey(coord)) {
                throw new IllegalArgumentException("Should not happen");
            }
            openSides.put(coord, 6);
            for (var neighbor : coord.getAdjacent()) {
                if (openSides.containsKey(neighbor)) {
                    openSides.computeIfPresent(coord, (_, i) -> i - 1);
                    openSides.computeIfPresent(neighbor, (_, i) -> i - 1);
                }
            }
        }

        var totalSurfaceArea = openSides.values().stream().mapToInt(i -> i).sum();
        System.out.println(totalSurfaceArea);

        // Find the bounds of the space
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;

        for (Coordinate c : openSides.keySet()) {
            minX = Math.min(minX, c.x);
            maxX = Math.max(maxX, c.x);
            minY = Math.min(minY, c.y);
            maxY = Math.max(maxY, c.y);
            minZ = Math.min(minZ, c.z);
            maxZ = Math.max(maxZ, c.z);
        }

        // Expand bounds by 1 to ensure we can "go around" the lava droplet
        minX--;
        maxX++;
        minY--;
        maxY++;
        minZ--;
        maxZ++;

        // Start flood fill from a corner outside the lava droplet
        Coordinate start = new Coordinate(minX, minY, minZ);
        var queue = new ArrayDeque<Coordinate>();
        queue.add(start);
        var visited = new HashSet<Coordinate>();

        while (!queue.isEmpty()) {
            var current = queue.poll();

            if (visited.contains(current)) {
                continue;
            }

            visited.add(current);

            for (var neighbor : current.getAdjacent()) {
                // Skip if out of bounds
                if (neighbor.x < minX || neighbor.x > maxX ||
                        neighbor.y < minY || neighbor.y > maxY ||
                        neighbor.z < minZ || neighbor.z > maxZ) {
                    continue;
                }

                if (openSides.containsKey(neighbor)) {
                    openSides.computeIfPresent(neighbor, (_, i) -> i - 1);
                } else if (!visited.contains(neighbor)) {
                    queue.add(neighbor);
                }
            }

        }

        var insideSurfaceArea = openSides.values().stream().mapToInt(i -> i).sum();
        System.out.println(totalSurfaceArea - insideSurfaceArea);
    }

}
