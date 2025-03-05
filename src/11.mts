import * as R from "ramda";
import { regex } from "regex";
import { Map } from "immutable";
import { readLines } from "./readLines.mjs";

const exampleLines = `Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1`.split("\n");

const lines = readLines("11.txt", { trim: false });

type Monkey = {
  increaseWorryLevel: (worryLevel: number) => number;
  nextMonkey: (worryLevel: number) => number;
  index: number;
};

type Item = {
  worryLevel: number;
  monkey: number;
  numberOfInspections: Map<number, number>;
};

type State = Item[];

type Scenario = {
  state: State;
  monkeys: Monkey[];
};

const { monkeys, state: initialState } = R.pipe(
  R.splitWhenever((l: string) => l.trim().length === 0),
  R.map((monkeyDescription: string[]) => monkeyDescription.join("\n")),
  R.map((description: string) => {
    const re = regex`
      ^Monkey\s+(?<index>\d+):
      \s+Starting\s+items:\s+(?<items>[^\n]+)
      \s+Operation:\s+new\s+=\s+old\s+(?<operation>\+|\*)\s+(?<operationParam>[^\n]+)
      \s+Test:\s+divisible\s+by\s+(?<test>\d+)
      \s+If\s+true:\s+throw\s+to\s+monkey\s+(?<trueCondition>\d+)
      \s+If\s+false:\s+throw\s+to\s+monkey\s+(?<falseCondition>\d+)$`;
    const match = re.exec(description);
    const groups = match?.groups;
    if (match == null || groups == null) {
      throw new Error(`Did not match description ${description}`);
    }

    const parseIntFromMatch = (groupName: string): number =>
      parseInt(groups[groupName]!, 10);

    const [test, falseCondition, trueCondition, index] = [
      parseIntFromMatch("test"),
      parseIntFromMatch("falseCondition"),
      parseIntFromMatch("trueCondition"),
      parseIntFromMatch("index"),
    ];

    function buildIncreaseWorryFunction(
      operation: "+" | "*",
      operationParam: string
    ): (worryLevel: number) => number {
      if (operationParam === "old") {
        switch (operation) {
          case "+":
            return (l) => l + l;
          case "*":
            return (l) => l * l;
        }
      } else {
        const operand = parseInt(operationParam);
        switch (operation) {
          case "+":
            return (l) => l + operand;
          case "*":
            return (l) => l * operand;
        }
      }
    }

    const monkey: Monkey = {
      index,
      increaseWorryLevel: buildIncreaseWorryFunction(
        groups["operation"] as "*" | "+",
        groups["operationParam"]
      ),
      nextMonkey(worryLevel: number): number {
        return worryLevel % test === 0 ? trueCondition : falseCondition;
      },
    };
    const items: Item[] = groups["items"]!.split(",").map((item) => ({
      monkey: index,
      worryLevel: parseInt(item, 10),
      numberOfInspections: Map<number, number>(),
    }));
    return [monkey, items] as const;
  }),
  R.reduce(
    (currentState: Scenario, [monkey, items]: readonly [Monkey, Item[]]) => {
      return {
        monkeys: [...currentState.monkeys, monkey],
        state: [...currentState.state, ...items],
      };
    },
    {
      monkeys: [],
      state: [],
    } as Scenario
  )
)(exampleLines);

const finalStatePart1 = R.pipe(
  R.times(
    R.always((state: State) => {
      return R.reduce(
        (state: State, monkey: Monkey) => {
          return state.map((item) => {
            if (item.monkey !== monkey.index) {
              return item;
            }
            const newWorryLevel = Math.floor(
              monkey.increaseWorryLevel(item.worryLevel) / 3
            );
            const nextMonkey = monkey.nextMonkey(newWorryLevel);
            return {
              worryLevel: newWorryLevel,
              monkey: nextMonkey,
              numberOfInspections: item.numberOfInspections.update(
                monkey.index,
                0,
                R.inc
              ),
            };
          });
        },
        state,
        monkeys
      );
    })
  ),
  R.reduce((acc, f: (state: State) => State) => f(acc), initialState)
)(20);

function calculateMonkeyBusiness(state: State): number {
  const finalCount = R.reduce(
    (acc: Map<number, number>, item: Item) => {
      return acc.mergeWith(R.add, item.numberOfInspections);
    },
    Map<number, number>(),
    state
  );

  console.log(finalCount.toArray());

  return finalCount
    .sort(R.descend(R.identity))
    .valueSeq()
    .take(2)
    .reduce(R.multiply, 1);
}

const monkeyBusinessPart1 = calculateMonkeyBusiness(finalStatePart1);

console.log(monkeyBusinessPart1);
