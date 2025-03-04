import * as R from "ramda";
import { readLines } from "./readLines.mjs";

// const lines = `
// addx 15
// addx -11
// addx 6
// addx -3
// addx 5
// addx -1
// addx -8
// addx 13
// addx 4
// noop
// addx -1
// addx 5
// addx -1
// addx 5
// addx -1
// addx 5
// addx -1
// addx 5
// addx -1
// addx -35
// addx 1
// addx 24
// addx -19
// addx 1
// addx 16
// addx -11
// noop
// noop
// addx 21
// addx -15
// noop
// noop
// addx -3
// addx 9
// addx 1
// addx -3
// addx 8
// addx 1
// addx 5
// noop
// noop
// noop
// noop
// noop
// addx -36
// noop
// addx 1
// addx 7
// noop
// noop
// noop
// addx 2
// addx 6
// noop
// noop
// noop
// noop
// noop
// addx 1
// noop
// noop
// addx 7
// addx 1
// noop
// addx -13
// addx 13
// addx 7
// noop
// addx 1
// addx -33
// noop
// noop
// noop
// addx 2
// noop
// noop
// noop
// addx 8
// noop
// addx -1
// addx 2
// addx 1
// noop
// addx 17
// addx -9
// addx 1
// addx 1
// addx -3
// addx 11
// noop
// noop
// addx 1
// noop
// addx 1
// noop
// noop
// addx -13
// addx -19
// addx 1
// addx 3
// addx 26
// addx -30
// addx 12
// addx -1
// addx 3
// addx 1
// noop
// noop
// noop
// addx -9
// addx 18
// addx 1
// addx 2
// noop
// noop
// addx 9
// noop
// noop
// noop
// addx -1
// addx 2
// addx -37
// addx 1
// addx 3
// noop
// addx 15
// addx -21
// addx 22
// addx -6
// addx 1
// noop
// addx 2
// addx 1
// noop
// addx -10
// noop
// noop
// addx 20
// addx 1
// addx 2
// addx 2
// addx -6
// addx -11
// noop
// noop
// noop`
//   .split("\n")
//   .filter((l) => l.trim().length > 0);

const lines = readLines("10.txt");

type RegisterOperation = {
  dX: number;
};

type RegisterState = {
  duringCycle: number;
  endOfCycle: number;
};

const states: RegisterState[] = R.pipe(
  R.chain((line: string) => {
    if (line === "noop") {
      return [{ dX: 0 }];
    } else if (line.startsWith("addx")) {
      const [_, arg] = line.split(" ");
      if (arg == null) {
        throw new Error(`Cannot extract arg in line ${line}`);
      }
      return [{ dX: 0 }, { dX: parseInt(arg, 10) }];
    } else {
      throw new Error(`Unknown line ${line}`);
    }
  }),
  R.scan(
    (acc: RegisterState, current: RegisterOperation) => {
      return {
        duringCycle: acc.endOfCycle,
        endOfCycle: acc.endOfCycle + current.dX,
      };
    },
    {
      duringCycle: 1,
      endOfCycle: 1,
    }
  ),
  R.drop(1)
)(lines);
console.log(states);

const selectStates = [20, 60, 100, 140, 180, 220].map((i) => {
  return i * states.at(i)!.duringCycle;
});

console.log(selectStates);

console.log(R.sum(selectStates));

const mappedIndex = R.addIndex(R.map);

const foo = R.pipe(
  R.take(240),
  // @ts-ignore
  mappedIndex((state: RegisterState, i: number) => {
    const drawPosition = i % 40;
    const isSpriteCoveringDrawPosition =
      Math.abs(drawPosition - state.duringCycle) <= 1;
    return isSpriteCoveringDrawPosition ? "#" : ".";
  }),
  R.splitEvery(40),
  R.map(R.join(""))
)(states);

console.log(foo);
