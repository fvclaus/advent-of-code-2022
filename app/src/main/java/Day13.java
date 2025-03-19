
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

sealed interface PacketData extends Comparable<PacketData> {

}

record PrimitivePacketData(int content) implements PacketData {

    @Override
    public int compareTo(PacketData other) {
        if (other instanceof ListPacketData l) {
            return new ListPacketData(this).compareTo(l);
        }
        PrimitivePacketData p = (PrimitivePacketData) other;
        if (content() < p.content()) {
            return -1;
        } else if (content() > p.content()) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return String.valueOf(this.content);
    }

}

final class ListPacketData implements PacketData {

    // PECS prevents me from typing this better, because I .add and .get from the list.
    private final List<Object> content;

    public ListPacketData() {
        this.content = new ArrayList<>();
    }

    public ListPacketData(ListPacketData data) {
        this.content = new ArrayList<>();
        this.content.add(data);
    }

    public ListPacketData(PrimitivePacketData data) {
        this.content = new ArrayList<>();
        this.content.add(data);
    }

    public void add(PrimitivePacketData data) {
        this.content.add(data);
    }

    public void add(ListPacketData data) {
        this.content.add(data);
    }

    @Override
    public String toString() {
        return "[" + this.content.stream().map(Object::toString).collect(Collectors.joining(",")) + "]";
    }

    @Override
    public int compareTo(PacketData other) {

        if (other instanceof PrimitivePacketData p) {
            return this.compareTo(new ListPacketData(p));
        }
        ListPacketData otherList = (ListPacketData) other;
        for (var i = 0; i < content.size(); i++) {
            PacketData left = (PacketData) content.get(i);
            if (i >= otherList.content.size()) {
                return 1;
            }
            PacketData right = (PacketData) otherList.content.get(i);
            var comparison = left.compareTo(right);
            if (comparison != 0) {
                return comparison;
            }

        }
        return content.size() < otherList.content.size() ? -1 : 0;
    }

}

record PairOfPackets(ListPacketData left, ListPacketData right) {

    public boolean isInCorrectOrder() {
        var comparison = left.compareTo(right);
        if (comparison == 0) {
            throw new IllegalStateException("The instruction don't cover this case");
        }
        return comparison == -1;
    }
};

public class Day13 {

    private static String input = """
[1,1,3,1,1]
[1,1,5,1,1]

[[1],[2,3,4]]
[[1],4]

[9]
[[8,7,6]]

[[4,4],4,4]
[[4,4],4,4,4]

[7,7,7,7]
[7,7,7]

[]
[3]

[[[]]]
[[]]

[1,[2,[3,[4,[5,6,7]]]],8,9]
[1,[2,[3,[4,[5,6,0]]]],8,9]""";

    private static ListPacketData parseLine(String line) {

        if (line.charAt(0) != '[') {
            throw new IllegalArgumentException("Line should start with [, but didn't: " + line);
        }
        var stack = new ArrayDeque<ListPacketData>();
        stack.push(new ListPacketData());
        var tokens = Arrays.stream(line.split(","))
                .flatMap(segment -> {
                    return Arrays.stream(segment.split("(?<=\\[)|(?=\\[)|(?<=\\])|(?=\\])"));
                })
                .toList();
        for (var i = 1; i < (tokens.size() - 1); i++) {
            var currentList = stack.peek();
            var token = String.valueOf(tokens.get(i));
            if (token.equals(",")) {
                throw new IllegalStateException("Commas should not be here.");
            }
            switch (token) {
                case "[" -> {
                    ListPacketData newList = new ListPacketData();
                    currentList.add(newList);
                    stack.push(newList);
                }
                case "]" -> {
                    stack.pop();
                }
                default -> {
                    currentList.add(new PrimitivePacketData(Integer.valueOf(token)));
                }

            }
        }

        if (line.charAt(line.length() - 1) != ']') {
            throw new IllegalArgumentException("Line should end with ], but didn't: " + line);
        }
        return stack.peek();
    }

    public static void main(String[] args) throws Exception {
        var lines = Files.readAllLines(Path.of(Day13.class.getResource("/13.txt").toURI())).stream();
        // var lines = Arrays.stream(input.split("\n"));
        var data = lines
                .filter(l -> l.trim().length() > 0)
                .map(Day13::parseLine)
                .collect(Collectors.toCollection(ArrayList::new));
        print(data);
        part1(data);

        part2(data);

    }

    // Now, you just need to put all of the packets in the right order. Disregard the blank lines in your list of received packets.
    // 
    // The distress signal protocol also requires that you include two additional divider packets:
    // 
    // [[2]]
    // [[6]]
    // 
    // Using the same rules as before, organize all packets - the ones in your list of received packets as well as the two divider packets - into the correct order.
    // 
    // For the example above, the result of putting the packets in the correct order is:
    // 
    // []
    // [[]]
    // [[[]]]
    // [1,1,3,1,1]
    // [1,1,5,1,1]
    // [[1],[2,3,4]]
    // [1,[2,[3,[4,[5,6,0]]]],8,9]
    // [1,[2,[3,[4,[5,6,7]]]],8,9]
    // [[1],4]
    // [[2]]
    // [3]
    // [[4,4],4,4]
    // [[4,4],4,4,4]
    // [[6]]
    // [7,7,7]
    // [7,7,7,7]
    // [[8,7,6]]
    // [9]
    // 
    // Afterward, locate the divider packets. To find the decoder key for this distress signal, you need to determine the indices of the two divider packets and multiply them together. (The first packet is at index 1, the second packet is at index 2, and so on.) In this example, the divider packets are 10th and 14th, and so the decoder key is 140.
    // 
    // Organize all of the packets into the correct order. What is the decoder key for the distress signal?
    private static void part2(ArrayList<ListPacketData> data) {
        ListPacketData decoderPacket1 = new ListPacketData(new ListPacketData(new PrimitivePacketData(2)));
        ListPacketData decoderPacket2 = new ListPacketData(new ListPacketData(new PrimitivePacketData(6)));
        data.add(decoderPacket1);
        data.add(decoderPacket2);
        Comparator<ListPacketData> comparator = (a, b) -> a.compareTo(b);

        data.sort(comparator);
        print(data);

        int indexDecoderPacket1 = Arrays.binarySearch(data.toArray(new ListPacketData[0]), decoderPacket1, comparator);
        int indexDecoderPacket2 = Arrays.binarySearch(data.toArray(new ListPacketData[0]), decoderPacket2, comparator);

        System.out.println(indexDecoderPacket1);
        System.out.println(indexDecoderPacket2);
        System.out.println((indexDecoderPacket1 + 1) * (indexDecoderPacket2 + 1));
    }

    private static void print(List<ListPacketData> data) {
        for (PacketData line : data) {
            System.out.println(line);
        }
    }

    // You climb the hill and again try contacting the Elves. However, you instead receive a signal you weren't expecting: a distress signal.
    // 
    // Your handheld device must still not be working properly; the packets from the distress signal got decoded out of order. You'll need to re-order the list of received packets (your puzzle input) to decode the message.
    // 
    // Your list consists of pairs of packets; pairs are separated by a blank line. You need to identify how many pairs of packets are in the right order.
    // 
    // For example:
    // 
    // [1,1,3,1,1]
    // [1,1,5,1,1]
    // 
    // [[1],[2,3,4]]
    // [[1],4]
    // 
    // [9]
    // [[8,7,6]]
    // 
    // [[4,4],4,4]
    // [[4,4],4,4,4]
    // 
    // [7,7,7,7]
    // [7,7,7]
    // 
    // []
    // [3]
    // 
    // [[[]]]
    // [[]]
    // 
    // [1,[2,[3,[4,[5,6,7]]]],8,9]
    // [1,[2,[3,[4,[5,6,0]]]],8,9]
    // 
    // Packet data consists of lists and integers. Each list starts with [, ends with ], and contains zero or more comma-separated values (either integers or other lists). Each packet is always a list and appears on its own line.
    // 
    // When comparing two values, the first value is called left and the second value is called right. Then:
    // 
    //     If both values are integers, the lower integer should come first. If the left integer is lower than the right integer, the inputs are in the right order. If the left integer is higher than the right integer, the inputs are not in the right order. Otherwise, the inputs are the same integer; continue checking the next part of the input.
    //     If both values are lists, compare the first value of each list, then the second value, and so on. If the left list runs out of items first, the inputs are in the right order. If the right list runs out of items first, the inputs are not in the right order. If the lists are the same length and no comparison makes a decision about the order, continue checking the next part of the input.
    //     If exactly one value is an integer, convert the integer to a list which contains that integer as its only value, then retry the comparison. For example, if comparing [0,0,0] and 2, convert the right value to [2] (a list containing 2); the result is then found by instead comparing [0,0,0] and [2].
    // 
    // Using these rules, you can determine which of the pairs in the example are in the right order:
    // 
    // == Pair 1 ==
    // - Compare [1,1,3,1,1] vs [1,1,5,1,1]
    //   - Compare 1 vs 1
    //   - Compare 1 vs 1
    //   - Compare 3 vs 5
    //     - Left side is smaller, so inputs are in the right order
    // 
    // == Pair 2 ==
    // - Compare [[1],[2,3,4]] vs [[1],4]
    //   - Compare [1] vs [1]
    //     - Compare 1 vs 1
    //   - Compare [2,3,4] vs 4
    //     - Mixed types; convert right to [4] and retry comparison
    //     - Compare [2,3,4] vs [4]
    //       - Compare 2 vs 4
    //         - Left side is smaller, so inputs are in the right order
    // 
    // == Pair 3 ==
    // - Compare [9] vs [[8,7,6]]
    //   - Compare 9 vs [8,7,6]
    //     - Mixed types; convert left to [9] and retry comparison
    //     - Compare [9] vs [8,7,6]
    //       - Compare 9 vs 8
    //         - Right side is smaller, so inputs are not in the right order
    // 
    // == Pair 4 ==
    // - Compare [[4,4],4,4] vs [[4,4],4,4,4]
    //   - Compare [4,4] vs [4,4]
    //     - Compare 4 vs 4
    //     - Compare 4 vs 4
    //   - Compare 4 vs 4
    //   - Compare 4 vs 4
    //   - Left side ran out of items, so inputs are in the right order
    // 
    // == Pair 5 ==
    // - Compare [7,7,7,7] vs [7,7,7]
    //   - Compare 7 vs 7
    //   - Compare 7 vs 7
    //   - Compare 7 vs 7
    //   - Right side ran out of items, so inputs are not in the right order
    // 
    // == Pair 6 ==
    // - Compare [] vs [3]
    //   - Left side ran out of items, so inputs are in the right order
    // 
    // == Pair 7 ==
    // - Compare [[[]]] vs [[]]
    //   - Compare [[]] vs []
    //     - Right side ran out of items, so inputs are not in the right order
    // 
    // == Pair 8 ==
    // - Compare [1,[2,[3,[4,[5,6,7]]]],8,9] vs [1,[2,[3,[4,[5,6,0]]]],8,9]
    //   - Compare 1 vs 1
    //   - Compare [2,[3,[4,[5,6,7]]]] vs [2,[3,[4,[5,6,0]]]]
    //     - Compare 2 vs 2
    //     - Compare [3,[4,[5,6,7]]] vs [3,[4,[5,6,0]]]
    //       - Compare 3 vs 3
    //       - Compare [4,[5,6,7]] vs [4,[5,6,0]]
    //         - Compare 4 vs 4
    //         - Compare [5,6,7] vs [5,6,0]
    //           - Compare 5 vs 5
    //           - Compare 6 vs 6
    //           - Compare 7 vs 0
    //             - Right side is smaller, so inputs are not in the right order
    // 
    // What are the indices of the pairs that are already in the right order? (The first pair has index 1, the second pair has index 2, and so on.) In the above example, the pairs in the right order are 1, 2, 4, and 6; the sum of these indices is 13.
    // 
    // Determine which pairs of packets are already in the right order. What is the sum of the indices of those pairs?
    private static void part1(List<ListPacketData> lines) {
        var sum = 0;
        for (int pairIndex = 1; pairIndex <= (lines.size() / 2); pairIndex += 1) {
            var pair = new PairOfPackets((ListPacketData) lines.get((pairIndex * 2) - 2), (ListPacketData) lines.get((pairIndex * 2) - 1));
            if (!pair.isInCorrectOrder()) {
                System.out.println("Pair " + pairIndex + " are not in the correct order: " + pair);
            } else {
                System.out.println("Pair " + pairIndex + " is in the correct order");
                sum += pairIndex;
            }
        }
        System.out.println(sum);
    }
}
