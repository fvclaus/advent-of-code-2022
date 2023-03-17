import { readLines } from "./readLines.mjs";

const forest: (number | undefined)[][] = [];

// const text = `
// 30373
// 25512
// 65332
// 33549
// 35390`
//   .trim()
//   .split("\n");

readLines("08.txt").forEach((line, r) => {
  let treeLine = forest[r];
  line.split("").forEach((tree, c) => {
    if (treeLine == null) {
      treeLine = [];
    }
    treeLine[c] = parseInt(tree, 10);
  });

  if (treeLine == null) {
    throw new Error("Hä?");
  }

  forest[r] = treeLine;
});

class Coordinate {
  constructor(public r: number, public c: number) {}

  public get height(): number | undefined {
    const row = forest[this.r];
    if (row == null) {
      return undefined;
    }
    return row[this.c];
  }

  public get valid(): boolean {
    if (this.r < 0 || this.c < 0) {
      return false;
    }
    const row = forest[this.r];
    if (row == null) {
      return false;
    }

    return this.c < row.length;
  }

  public walkUp(): Coordinate {
    return new Coordinate(this.r - 1, this.c);
  }

  public walkDown(): Coordinate {
    return new Coordinate(this.r + 1, this.c);
  }

  public walkLeft(): Coordinate {
    return new Coordinate(this.r, this.c - 1);
  }

  public walkRight(): Coordinate {
    return new Coordinate(this.r, this.c + 1);
  }

  public isOnBorder(): boolean {
    if (this.r == 0 || this.r == forest.length - 1) {
      return true;
    }
    const row = forest[this.r];
    if (row == null) {
      return false;
    }
    if (this.c == 0 || this.c == row.length - 1) {
      return true;
    }
    return false;
  }
}

const getSameOrLargerNeighbor = (
  start: Coordinate,
  inc: (pos: Coordinate) => Coordinate
): number | undefined => {
  const height = start.height!;
  let pos = inc(start);
  while (pos.valid) {
    const nextHeight = pos.height;
    if (nextHeight != null && nextHeight >= height) {
      return nextHeight;
    }
    pos = inc(pos);
  }
  return undefined;
};

const isVisible = (pos: Coordinate): boolean => {
  if (pos.isOnBorder()) {
    return true;
  }
  // TODO Lazy
  const hasOnlyLargerNeighbors = [
    getSameOrLargerNeighbor(pos, (pos) => pos.walkUp()),
    getSameOrLargerNeighbor(pos, (pos) => pos.walkRight()),
    getSameOrLargerNeighbor(pos, (pos) => pos.walkDown()),
    getSameOrLargerNeighbor(pos, (pos) => pos.walkLeft()),
  ];

  return hasOnlyLargerNeighbors.some((pos) => pos == null);
};

for (let r = 1; r <= forest.length - 2; r++) {
  const row = forest[r];
  if (row == null) {
    throw new Error(`Hä?`);
  }
  for (let c = 1; c <= row.length - 2; c++) {
    if (!isVisible(new Coordinate(r, c))) {
      row[c] = undefined;
    }
  }
}

console.log(forest);

console.log(
  forest
    .flatMap((row) => row)
    .reduce(
      (counter, tree) => (tree != null ? ++counter! : counter),
      0 as number
    )
);
