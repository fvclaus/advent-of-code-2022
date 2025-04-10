import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Day22 {

    public static String input = """
                    ...#
                    .#..
                    #...
                    ....
            ...#.......#
            ........#...
            ..#....#....
            ..........#.
                    ...#....
                    .....#..
                    .#......
                    ......#.

            10R5L5R10L4R5L5
                        """;

    public enum GameElement {
        VOID, WALL, OPEN
    }

    public enum Turn {
        CLOCKWISE, COUNTER_CLOCKWISE
    }

    public record Movement(int numberOfSteps, Turn turn) {
    }

    public static class Game {
        public GameElement[][] board;
        public List<Movement> movements;

        public Game(GameElement[][] board, List<Movement> movements) {
            this.board = board;
            this.movements = movements;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();

            // Convert board to string
            for (GameElement[] row : board) {
                for (GameElement element : row) {
                    switch (element) {
                        case OPEN:
                            sb.append('.');
                            break;
                        case WALL:
                            sb.append('#');
                            break;
                        case VOID:
                            sb.append(' ');
                            break;
                    }
                }
                sb.append('\n');
            }

            // Add a blank line between board and directions
            sb.append('\n');

            // Convert directions to string
            for (Movement direction : movements) {
                sb.append(direction.numberOfSteps);
                if (direction.turn != null) {
                    sb.append(direction.turn == Turn.CLOCKWISE ? 'R' : 'L');
                }
            }

            return sb.toString();
        }
    }

    public static Game parseInput() throws Exception {

        // String[] parts = input.split("\n\n");
        String[] parts = Files.readString(Path.of(Day22.class.getResource("/22.txt").toURI())).split("\n\n");
        String[] boardLines = parts[0].split("\n");
        String directionsStr = parts[1].trim();

        // Find the maximum width of the board
        int maxWidth = 0;
        for (String line : boardLines) {
            maxWidth = Math.max(maxWidth, line.length());
        }

        // Initialize the board with VOID
        GameElement[][] board = new GameElement[boardLines.length][maxWidth];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                board[i][j] = GameElement.VOID;
            }
        }

        // Fill the board with actual elements
        for (int i = 0; i < boardLines.length; i++) {
            String line = boardLines[i];
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
                if (c == '.') {
                    board[i][j] = GameElement.OPEN;
                } else if (c == '#') {
                    board[i][j] = GameElement.WALL;
                } else {
                    board[i][j] = GameElement.VOID;
                }
            }
        }

        // Parse directions
        List<Movement> directions = new ArrayList<>();
        StringBuilder number = new StringBuilder();

        for (int i = 0; i < directionsStr.length(); i++) {
            char c = directionsStr.charAt(i);
            if (Character.isDigit(c)) {
                number.append(c);
            } else {
                if (number.length() > 0) {
                    int steps = Integer.parseInt(number.toString());
                    Turn turn = null;
                    if (c == 'R') {
                        turn = Turn.CLOCKWISE;
                    } else if (c == 'L') {
                        turn = Turn.COUNTER_CLOCKWISE;
                    } else {
                        throw new IllegalStateException("Should not happen");
                    }
                    directions.add(new Movement(steps, turn));
                    number = new StringBuilder();
                }
            }
        }

        // Add the last number if there is one (without a turn)
        if (number.length() > 0) {
            int steps = Integer.parseInt(number.toString());
            directions.add(new Movement(steps, null));
        }

        return new Game(board, directions);
    }

    public static enum Direction {
        RIGHT, LEFT, UP, DOWN
    }

    private static Map<Direction, Direction> clockwise = Map.of(
            Direction.DOWN, Direction.LEFT,
            Direction.LEFT, Direction.UP,
            Direction.UP, Direction.RIGHT,
            Direction.RIGHT, Direction.DOWN);

    private static Map<Direction, Direction> counterClockwise = clockwise.entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    public static Direction turn(Direction direction, Turn turn) {
        return switch (turn) {
            case CLOCKWISE -> {
                yield clockwise.get(direction);
            }
            case COUNTER_CLOCKWISE -> {
                yield counterClockwise.get(direction);
            }
        };

    }

    public static void main(String[] args) throws Exception {
        Game game = parseInput();
        var direction = Direction.RIGHT;
        var row = 0;
        var column = 0;
        while (game.board[row][column] == GameElement.VOID) {
            column++;
        }

        for (var i = 0; i < game.movements.size(); i++) {
            var movement = game.movements.get(i);
            var steps = movement.numberOfSteps;

            while (steps > 0) {

                var newRow = row;
                var newColumn = column;

                do {
                    switch (direction) {
                        case DOWN -> newRow = (newRow + 1) % game.board.length;
                        case UP -> newRow = newRow == 0 ? game.board.length - 1 : newRow - 1;
                        case RIGHT -> newColumn = (newColumn + 1) % game.board[newRow].length;
                        case LEFT -> newColumn = newColumn == 0 ? game.board[newRow].length - 1 : newColumn - 1;
                    }
                } while (game.board[newRow][newColumn] == GameElement.VOID);

                if (game.board[newRow][newColumn] == GameElement.OPEN) {
                    column = newColumn;
                    row = newRow;
                } else if (game.board[newRow][newColumn] == GameElement.WALL) {
                    break;
                } else if (game.board[newRow][newColumn] == GameElement.VOID) {
                    throw new IllegalStateException("should not happen");
                }

                steps--;

            }

            if (movement.turn != null) {
                direction = turn(direction, movement.turn);
            }
        }
        System.out.println((row + 1) + " " + (column + 1) + " " + direction);
        System.out.println(((row + 1) * 1000) + (4 * (column + 1)));
        // System.out.println(game.toString());
    }
}
