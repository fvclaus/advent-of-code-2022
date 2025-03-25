
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class Day16 {

    private static String input = """
Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
Valve BB has flow rate=13; tunnels lead to valves CC, AA
Valve CC has flow rate=2; tunnels lead to valves DD, BB
Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
Valve EE has flow rate=3; tunnels lead to valves FF, DD
Valve FF has flow rate=0; tunnels lead to valves EE, GG
Valve GG has flow rate=0; tunnels lead to valves FF, HH
Valve HH has flow rate=22; tunnel leads to valve GG
Valve II has flow rate=0; tunnels lead to valves AA, JJ
Valve JJ has flow rate=21; tunnel leads to valve II""";

    public static void main(String[] args) throws Exception {
        List<Valve> valves = parseLines();
        for (Valve valve : valves) {
            System.out.println(valve);
        }

        Map<String, Valve> labelToValve = valves.stream()
                .collect(Collectors.toMap(Valve::getLabel, v -> v));

        part1(labelToValve);

    }

    // The sensors have led you to the origin of the distress signal: yet another handheld device, just like the one the Elves gave you. However, you don't see any Elves around; instead, the device is surrounded by elephants! They must have gotten lost in these tunnels, and one of the elephants apparently figured out how to turn on the distress signal.
    // 
    // The ground rumbles again, much stronger this time. What kind of cave is this, exactly? You scan the cave with your handheld device; it reports mostly igneous rock, some ash, pockets of pressurized gas, magma... this isn't just a cave, it's a volcano!
    // 
    // You need to get the elephants out of here, quickly. Your device estimates that you have 30 minutes before the volcano erupts, so you don't have time to go back out the way you came in.
    // 
    // You scan the cave for other options and discover a network of pipes and pressure-release valves. You aren't sure how such a system got into a volcano, but you don't have time to complain; your device produces a report (your puzzle input) of each valve's flow rate if it were opened (in pressure per minute) and the tunnels you could use to move between the valves.
    // 
    // There's even a valve in the room you and the elephants are currently standing in labeled AA. You estimate it will take you one minute to open a single valve and one minute to follow any tunnel from one valve to another. What is the most pressure you could release?
    // 
    // For example, suppose you had the following scan output:
    // 
    // Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
    // Valve BB has flow rate=13; tunnels lead to valves CC, AA
    // Valve CC has flow rate=2; tunnels lead to valves DD, BB
    // Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
    // Valve EE has flow rate=3; tunnels lead to valves FF, DD
    // Valve FF has flow rate=0; tunnels lead to valves EE, GG
    // Valve GG has flow rate=0; tunnels lead to valves FF, HH
    // Valve HH has flow rate=22; tunnel leads to valve GG
    // Valve II has flow rate=0; tunnels lead to valves AA, JJ
    // Valve JJ has flow rate=21; tunnel leads to valve II
    // 
    // All of the valves begin closed. You start at valve AA, but it must be damaged or jammed or something: its flow rate is 0, so there's no point in opening it. However, you could spend one minute moving to valve BB and another minute opening it; doing so would release pressure during the remaining 28 minutes at a flow rate of 13, a total eventual pressure release of 28 * 13 = 364. Then, you could spend your third minute moving to valve CC and your fourth minute opening it, providing an additional 26 minutes of eventual pressure release at a flow rate of 2, or 52 total pressure released by valve CC.
    // 
    // Making your way through the tunnels like this, you could probably open many or all of the valves by the time 30 minutes have elapsed. However, you need to release as much pressure as possible, so you'll need to be methodical. Instead, consider this approach:
    // 
    // == Minute 1 ==
    // No valves are open.
    // You move to valve DD.
    // 
    // == Minute 2 ==
    // No valves are open.
    // You open valve DD.
    // 
    // == Minute 3 ==
    // Valve DD is open, releasing 20 pressure.
    // You move to valve CC.
    // 
    // == Minute 4 ==
    // Valve DD is open, releasing 20 pressure.
    // You move to valve BB.
    // 
    // == Minute 5 ==
    // Valve DD is open, releasing 20 pressure.
    // You open valve BB.
    // 
    // == Minute 6 ==
    // Valves BB and DD are open, releasing 33 pressure.
    // You move to valve AA.
    // 
    // == Minute 7 ==
    // Valves BB and DD are open, releasing 33 pressure.
    // You move to valve II.
    // 
    // == Minute 8 ==
    // Valves BB and DD are open, releasing 33 pressure.
    // You move to valve JJ.
    // 
    // == Minute 9 ==
    // Valves BB and DD are open, releasing 33 pressure.
    // You open valve JJ.
    // 
    // == Minute 10 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve II.
    // 
    // == Minute 11 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve AA.
    // 
    // == Minute 12 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve DD.
    // 
    // == Minute 13 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve EE.
    // 
    // == Minute 14 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve FF.
    // 
    // == Minute 15 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve GG.
    // 
    // == Minute 16 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You move to valve HH.
    // 
    // == Minute 17 ==
    // Valves BB, DD, and JJ are open, releasing 54 pressure.
    // You open valve HH.
    // 
    // == Minute 18 ==
    // Valves BB, DD, HH, and JJ are open, releasing 76 pressure.
    // You move to valve GG.
    // 
    // == Minute 19 ==
    // Valves BB, DD, HH, and JJ are open, releasing 76 pressure.
    // You move to valve FF.
    // 
    // == Minute 20 ==
    // Valves BB, DD, HH, and JJ are open, releasing 76 pressure.
    // You move to valve EE.
    // 
    // == Minute 21 ==
    // Valves BB, DD, HH, and JJ are open, releasing 76 pressure.
    // You open valve EE.
    // 
    // == Minute 22 ==
    // Valves BB, DD, EE, HH, and JJ are open, releasing 79 pressure.
    // You move to valve DD.
    // 
    // == Minute 23 ==
    // Valves BB, DD, EE, HH, and JJ are open, releasing 79 pressure.
    // You move to valve CC.
    // 
    // == Minute 24 ==
    // Valves BB, DD, EE, HH, and JJ are open, releasing 79 pressure.
    // You open valve CC.
    // 
    // == Minute 25 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // == Minute 26 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // == Minute 27 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // == Minute 28 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // == Minute 29 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // == Minute 30 ==
    // Valves BB, CC, DD, EE, HH, and JJ are open, releasing 81 pressure.
    // 
    // This approach lets you release the most pressure possible in 30 minutes with this valve layout, 1651.
    // 
    // Work out the steps to release the most pressure in 30 minutes. What is the most pressure you can release?
    private static void part1(Map<String, Valve> labelToValve) {
        DecisionTree root = new DecisionTree(30, labelToValve.get("AA"), null);
        
        var decisionQueue = new ArrayDeque<DecisionTree>();
        decisionQueue.add(root);

        DecisionTree maxPressure = root;

        while (!decisionQueue.isEmpty()) {
            var node = decisionQueue.poll();
            var valve = node.valve;
            var openedValves = node.openedValves;
            var remainingMinutes = node.remainingMinutes;
            var candidateQueue = new ArrayDeque<Map.Entry<Valve, Integer>>();
            candidateQueue.add(new AbstractMap.SimpleEntry<>(valve, 0));
            var visitedValves = new HashSet<Valve>();
            var valveToDistance = new HashMap<Valve, Integer>();
            while (!candidateQueue.isEmpty()) {
                var currentEntry = candidateQueue.poll();
                var currentValve = currentEntry.getKey();
                var distance = currentEntry.getValue();
                if (!openedValves.contains(currentValve) && currentValve.flowRate > 0 && (remainingMinutes - distance) > 0) {
                    valveToDistance.put(currentValve, distance);
                }
                visitedValves.add(currentValve);
                for (Valve neighbor : currentValve.neighbors) {
                    if (!visitedValves.contains(neighbor)) {
                        candidateQueue.add(new AbstractMap.SimpleEntry<>(neighbor, distance + 1));
                    }
                }
            }

            for (Valve nextValveCandidates : valveToDistance.keySet()) {
                var distanceToValve = valveToDistance.get(nextValveCandidates);
                DecisionTree newNode = new DecisionTree((remainingMinutes - (distanceToValve + 1)), nextValveCandidates, node);
                if (newNode.totalPressureRelease > maxPressure.totalPressureRelease) {
                    maxPressure = newNode;
                }

                decisionQueue.add(newNode);
            }
        }

        System.out.println(maxPressure + ": " + maxPressure.totalPressureRelease + ", remainingMinutes" + maxPressure.remainingMinutes);
    }

    private static class DecisionTree {

        private final int remainingMinutes;
        private final Valve valve;
        private int totalPressureRelease = 0;
        private DecisionTree parent;
        private List<DecisionTree> children = new ArrayList<>();
        private final Set<Valve> openedValves;

        public DecisionTree(int remainingMinutes, Valve valve, DecisionTree parent) {
            this.remainingMinutes = remainingMinutes;
            this.valve = valve;
            this.parent = parent;

            if (parent != null) {
                this.openedValves = new HashSet<>(parent.openedValves);
                this.openedValves.add(valve);
                var pressureRelease = parent.totalPressureRelease;
                pressureRelease += this.remainingMinutes * valve.flowRate;
                this.totalPressureRelease = pressureRelease;
                parent.children.add(this);
            } else {
                this.openedValves = new HashSet<>();
            }

        }

        @Override
        public String toString() {
            var parents = new LinkedList<DecisionTree>();
            parents.add(this);
            var currentParent = this.parent;
            while (currentParent != null) {
                parents.addFirst(currentParent);
                currentParent = currentParent.parent;
            }
            return parents.stream().map(n -> n.valve.label).collect(Collectors.joining("->"));
        }

    }

    private static List<Valve> parseLines() throws Exception {
        List<Valve> valves = new ArrayList<>();
        Map<Valve, List<String>> neighborMap = new HashMap<>();

        Pattern pattern = Pattern.compile("Valve (\\w+) has flow rate=(\\d+); tunnels? leads? to valves? (.+)");
        // String[] lines = input.split("\n");
        var lines = Files.readAllLines(Path.of(Day16.class.getResource("/16.txt").toURI()));

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String label = matcher.group(1);
                int flowRate = Integer.parseInt(matcher.group(2));
                String[] neighborLabels = matcher.group(3).split(", ");

                Valve valve = new Valve(flowRate, label);
                valves.add(valve);
                neighborMap.put(valve, Arrays.asList(neighborLabels));
            }
        }

        // Link neighbors
        for (Valve valve : valves) {
            for (String neighborLabel : neighborMap.get(valve)) {
                var neighbor = valves.stream().filter(v -> v.label.equals(neighborLabel)).findFirst().get();
                valve.addNeighbor(neighbor);
            }
        }

        return valves;
    }

    static class Valve {

        private final String label;
        private final int flowRate;
        private final List<Valve> neighbors;

        public Valve(int flowRate, String label) {
            this.flowRate = flowRate;
            this.label = label;
            this.neighbors = new ArrayList<>();
        }

        public void addNeighbor(Valve neighbor) {
            neighbors.add(neighbor);
        }

        public String getLabel() {
            return this.label;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Valve otherValve)) {
                return false;
            }

            return otherValve.label.equals(label);
        }

        @Override
        public int hashCode() {
            return this.label.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Valve ").append(label).append(" has flow rate=").append(flowRate);
            if (neighbors.size() == 1) {
                sb.append("; tunnel leads to valve ");
            } else {
                sb.append("; tunnels lead to valves ");
            }
            for (int i = 0; i < neighbors.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(neighbors.get(i).label);
            }
            return sb.toString();
        }
    }
}
