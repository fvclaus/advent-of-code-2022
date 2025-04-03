import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Day21 {
    private static String input = """
            root: pppw + sjmn
            dbpl: 5
            cczh: sllz + lgvd
            zczc: 2
            ptdq: humn - dvpt
            dvpt: 3
            lfqf: 4
            humn: 5
            ljgn: 2
            sjmn: drzm * dbpl
            sllz: 4
            pppw: cczh / lfqf
            lgvd: ljgn * ptdq
            drzm: hmdt - zczc
            hmdt: 32
            """;

    public static void main(String[] args) throws Exception {
        var expressions = parseInput();
        System.out.println(expressions.get("root").solve(expressions));
    }

    public static Map<String, Expression> parseInput() throws Exception {
        Map<String, Expression> expressions = new HashMap<>();

        // String[] lines = input.split("\n");
        var lines = Files.readAllLines(Path.of(Day21.class.getResource("/21.txt").toURI()));
        for (String line : lines) {
            String[] parts = line.split(": ");
            String label = parts[0];
            String expressionStr = parts[1];

            Expression expression;
            if (expressionStr.matches("\\d+")) {
                // Integer expression
                expression = new NumberExpression(Integer.parseInt(expressionStr));
            } else {
                // Operation expression
                String[] opParts = expressionStr.split(" ");
                String op1 = opParts[0];
                String operator = opParts[1];
                String op2 = opParts[2];

                expression = switch (operator) {
                    case "+" -> OperatorExpression.plus(op1, op2);
                    case "-" -> OperatorExpression.minus(op1, op2);
                    case "*" -> OperatorExpression.multiply(op1, op2);
                    case "/" -> OperatorExpression.divide(op1, op2);
                    default -> throw new IllegalArgumentException("Unknown operator: " + operator);
                };
            }

            expressions.put(label, expression);
        }

        return expressions;
    }

    public sealed interface Expression {
        long solve(Map<String, Expression> expressions);
    }

    public static final class NumberExpression implements Expression {
        private final long value;

        public NumberExpression(long value) {
            this.value = value;
        }

        @Override
        public long solve(Map<String, Expression> expressions) {
            return value;
        }
    }

    public static final class OperatorExpression implements Expression {
        private final String op1;
        private final String op2;
        private final BiFunction<Long, Long, Long> operator;

        public OperatorExpression(String op1, String op2, BiFunction<Long, Long, Long> operator) {
            this.op1 = op1;
            this.op2 = op2;
            this.operator = operator;
        }

        @Override
        public long solve(Map<String, Expression> expressions) {
            long value1 = expressions.get(op1).solve(expressions);
            long value2 = expressions.get(op2).solve(expressions);
            return operator.apply(value1, value2);
        }

        // Factory methods for common operations
        public static OperatorExpression plus(String op1, String op2) {
            return new OperatorExpression(op1, op2, (a, b) -> a + b);
        }

        public static OperatorExpression minus(String op1, String op2) {
            return new OperatorExpression(op1, op2, (a, b) -> a - b);
        }

        public static OperatorExpression multiply(String op1, String op2) {
            return new OperatorExpression(op1, op2, (a, b) -> a * b);
        }

        public static OperatorExpression divide(String op1, String op2) {
            return new OperatorExpression(op1, op2, (a, b) -> a / b);
        }
    }
}
