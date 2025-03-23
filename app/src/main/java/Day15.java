
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Day15 {

    private static final String input = """
Sensor at x=2, y=18: closest beacon is at x=-2, y=15
Sensor at x=9, y=16: closest beacon is at x=10, y=16
Sensor at x=13, y=2: closest beacon is at x=15, y=3
Sensor at x=12, y=14: closest beacon is at x=10, y=16
Sensor at x=10, y=20: closest beacon is at x=10, y=16
Sensor at x=14, y=17: closest beacon is at x=10, y=16
Sensor at x=8, y=7: closest beacon is at x=2, y=10
Sensor at x=2, y=0: closest beacon is at x=2, y=10
Sensor at x=0, y=11: closest beacon is at x=2, y=10
Sensor at x=20, y=14: closest beacon is at x=25, y=17
Sensor at x=17, y=20: closest beacon is at x=21, y=22
Sensor at x=16, y=7: closest beacon is at x=15, y=3
Sensor at x=14, y=3: closest beacon is at x=15, y=3
Sensor at x=20, y=1: closest beacon is at x=15, y=3""";

    record Position(int x, int y) {

        public int manhattanDistance(Position other) {
            return Math.abs(other.x - x) + Math.abs(other.y - y);
        }
    }

    record Beacon(Position position) {

    }

    record Sensor(Position position, Beacon closestBeacon, int distanceToSensor) {

        public static Sensor of(Position position, Beacon closestBeacon) {
            return new Sensor(position, closestBeacon, position.manhattanDistance(closestBeacon.position));
        }

    }

    private static boolean isInOtherSensorRange(List<Sensor> sensors, Sensor currentSensor, Position position) {
        for (Sensor sensor : sensors) {
            if (sensor.equals(currentSensor)) {
                continue;
            }
            int distance = sensor.position().manhattanDistance(position);
            if (distance <= sensor.distanceToSensor) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        var sensors = parse();
        part1(sensors);
        part2(sensors);
    }

    // Your handheld device indicates that the distress signal is coming from a beacon nearby. The distress beacon is not detected by any sensor, but the distress beacon must have x and y coordinates each no lower than 0 and no larger than 4000000.
    // 
    // To isolate the distress beacon's signal, you need to determine its tuning frequency, which can be found by multiplying its x coordinate by 4000000 and then adding its y coordinate.
    // 
    // In the example above, the search space is smaller: instead, the x and y coordinates can each be at most 20. With this reduced search area, there is only a single position that could have a beacon: x=14, y=11. The tuning frequency for this distress beacon is 56000011.
    // 
    // Find the only possible position for the distress beacon. What is its tuning frequency?
    private static void part2(List<Sensor> sensors) throws IllegalStateException {
        var maxY = 4_000_000;
        for (var sensor : sensors) {
            System.out.println("Checking sensor " + sensor.position());
            if (sensor.position().y() < 0) {
                throw new IllegalStateException("Can only handle sensor below search area");
            }
            var minY = sensor.position.y() + -1 * sensor.distanceToSensor;
            if (minY > maxY) {
                System.out.println("Sensor doesn't cover search area");
                // Sensor doesn't cover search area
                continue;
            }
            var ret = scanOutsideOfSensorRange(Math.max(0, minY), maxY, sensor, sensors);
            if (ret != null) {
                System.out.println("Found solution at " + ret);
                System.out.println("Tuning frequency: " + ((long) ret.x() * 4_000_000 + ret.y()));
                break;
            }
        }
    }

    private static Position scanOutsideOfSensorRange(int rangeStart, int rangeEnd, Sensor sensor, List<Sensor> sensors) throws IllegalStateException {
        if (rangeStart > rangeEnd) {
            throw new IllegalArgumentException("minY must not be greater than maxY");
        }
        if (rangeStart < 0) {
            throw new IllegalArgumentException("rangeStart must not be less than 0");
        }
        for (var y = rangeStart; y <= rangeEnd; y++) {
            var distanceToTargetRow = Math.abs(sensor.position.y() - y);
            var diff = sensor.distanceToSensor() - distanceToTargetRow;
            var right = (sensor.position.x() + (diff + 1));
            var left = (sensor.position.x() - (diff + 1));

            if (right >= 0 && right <= rangeEnd && !isInOtherSensorRange(sensors, sensor, new Position(right, y))) {
                return new Position(right, y);
            }
            if (left >= 0 && left <= rangeEnd && !isInOtherSensorRange(sensors, sensor, new Position(left, y))) {
                return new Position(left, y);
            }
        }
        return null;
    }

    // You feel the ground rumble again as the distress signal leads you to a large network of subterranean tunnels. You don't have time to search them all, but you don't need to: your pack contains a set of deployable sensors that you imagine were originally built to locate lost Elves.
    // 
    // The sensors aren't very powerful, but that's okay; your handheld device indicates that you're close enough to the source of the distress signal to use them. You pull the emergency sensor system out of your pack, hit the big button on top, and the sensors zoom off down the tunnels.
    // 
    // Once a sensor finds a spot it thinks will give it a good reading, it attaches itself to a hard surface and begins monitoring for the nearest signal source beacon. Sensors and beacons always exist at integer coordinates. Each sensor knows its own position and can determine the position of a beacon precisely; however, sensors can only lock on to the one beacon closest to the sensor as measured by the Manhattan distance. (There is never a tie where two beacons are the same distance to a sensor.)
    // 
    // It doesn't take long for the sensors to report back their positions and closest beacons (your puzzle input). For example:
    // 
    // Sensor at x=2, y=18: closest beacon is at x=-2, y=15
    // Sensor at x=9, y=16: closest beacon is at x=10, y=16
    // Sensor at x=13, y=2: closest beacon is at x=15, y=3
    // Sensor at x=12, y=14: closest beacon is at x=10, y=16
    // Sensor at x=10, y=20: closest beacon is at x=10, y=16
    // Sensor at x=14, y=17: closest beacon is at x=10, y=16
    // Sensor at x=8, y=7: closest beacon is at x=2, y=10
    // Sensor at x=2, y=0: closest beacon is at x=2, y=10
    // Sensor at x=0, y=11: closest beacon is at x=2, y=10
    // Sensor at x=20, y=14: closest beacon is at x=25, y=17
    // Sensor at x=17, y=20: closest beacon is at x=21, y=22
    // Sensor at x=16, y=7: closest beacon is at x=15, y=3
    // Sensor at x=14, y=3: closest beacon is at x=15, y=3
    // Sensor at x=20, y=1: closest beacon is at x=15, y=3
    // 
    // So, consider the sensor at 2,18; the closest beacon to it is at -2,15. For the sensor at 9,16, the closest beacon to it is at 10,16.
    // 
    // Drawing sensors as S and beacons as B, the above arrangement of sensors and beacons looks like this:
    // 
    //                1    1    2    2
    //      0    5    0    5    0    5
    //  0 ....S.......................
    //  1 ......................S.....
    //  2 ...............S............
    //  3 ................SB..........
    //  4 ............................
    //  5 ............................
    //  6 ............................
    //  7 ..........S.......S.........
    //  8 ............................
    //  9 ............................
    // 10 ....B.......................
    // 11 ..S.........................
    // 12 ............................
    // 13 ............................
    // 14 ..............S.......S.....
    // 15 B...........................
    // 16 ...........SB...............
    // 17 ................S..........B
    // 18 ....S.......................
    // 19 ............................
    // 20 ............S......S........
    // 21 ............................
    // 22 .......................B....
    // 
    // This isn't necessarily a comprehensive map of all beacons in the area, though. Because each sensor only identifies its closest beacon, if a sensor detects a beacon, you know there are no other beacons that close or closer to that sensor. There could still be beacons that just happen to not be the closest beacon to any sensor. Consider the sensor at 8,7:
    // 
    //                1    1    2    2
    //      0    5    0    5    0    5
    // -2 ..........#.................
    // -1 .........###................
    //  0 ....S...#####...............
    //  1 .......#######........S.....
    //  2 ......#########S............
    //  3 .....###########SB..........
    //  4 ....#############...........
    //  5 ...###############..........
    //  6 ..#################.........
    //  7 .#########S#######S#........
    //  8 ..#################.........
    //  9 ...###############..........
    // 10 ....B############...........
    // 11 ..S..###########............
    // 12 ......#########.............
    // 13 .......#######..............
    // 14 ........#####.S.......S.....
    // 15 B........###................
    // 16 ..........#SB...............
    // 17 ................S..........B
    // 18 ....S.......................
    // 19 ............................
    // 20 ............S......S........
    // 21 ............................
    // 22 .......................B....
    // 
    // This sensor's closest beacon is at 2,10, and so you know there are no beacons that close or closer (in any positions marked #).
    // 
    // None of the detected beacons seem to be producing the distress signal, so you'll need to work out where the distress beacon is by working out where it isn't. For now, keep things simple by counting the positions where a beacon cannot possibly be along just a single row.
    // 
    // So, suppose you have an arrangement of beacons and sensors like in the example above and, just in the row where y=10, you'd like to count the number of positions a beacon cannot possibly exist. The coverage from all sensors near that row looks like this:
    // 
    //                  1    1    2    2
    //        0    5    0    5    0    5
    //  9 ...#########################...
    // 10 ..####B######################..
    // 11 .###S#############.###########.
    // 
    // In this example, in the row where y=10, there are 26 positions where a beacon cannot be present.
    // 
    // Consult the report from the sensors you just deployed. In the row where y=2000000, how many positions cannot contain a beacon?
    private static void part1(List<Sensor> sensors) {
        var yTarget = 10;
        Set<Integer> positions = getCoverage(sensors, yTarget);
        positions.removeAll(sensors.stream().map(s -> s.closestBeacon().position().x()).collect(Collectors.toSet()));
        System.out.println(positions.size());
    }

    private static Set<Integer> getCoverage(List<Sensor> sensors, int yTarget) {
        Set<Integer> positions = new HashSet<>();
        for (Sensor sensor : sensors) {
            var distanceToTargetRow = Math.abs(sensor.position.y() - yTarget);
            var distanceToBeacon = sensor.position.manhattanDistance(sensor.closestBeacon.position);
            if (distanceToBeacon < distanceToTargetRow) {
                // Cannot scan target row
                continue;
            }
            var diff = distanceToBeacon - distanceToTargetRow;
            while (diff >= 0) {
                positions.add(sensor.position.x() + diff);
                positions.add(sensor.position.x() - diff);
                diff -= 1;
            }
        }

        return positions;
    }

    public static List<Sensor> parse() throws Exception {
        List<Sensor> sensors = new ArrayList<>();
        Pattern pattern = Pattern.compile(
                "Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)"
        );

        // var lines = input.split("\n");
        var lines = Files.readAllLines(Path.of(Day15.class.getResource("/15.txt").toURI()));

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                int sensorX = Integer.parseInt(matcher.group(1));
                int sensorY = Integer.parseInt(matcher.group(2));
                int beaconX = Integer.parseInt(matcher.group(3));
                int beaconY = Integer.parseInt(matcher.group(4));

                Position sensorPos = new Position(sensorX, sensorY);
                Position beaconPos = new Position(beaconX, beaconY);
                Beacon beacon = new Beacon(beaconPos);

                sensors.add(Sensor.of(sensorPos, beacon));
            }
        }

        return sensors;
    }
}
