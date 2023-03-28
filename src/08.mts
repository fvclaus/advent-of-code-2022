/* --- Day 8: Treetop Tree House ---

The expedition comes across a peculiar patch of tall trees all planted carefully in a grid. The Elves explain that a previous expedition planted these trees as a reforestation effort. Now, they're curious if this would be a good location for a tree house.

First, determine whether there is enough tree cover here to keep a tree house hidden. To do this, you need to count the number of trees that are visible from outside the grid when looking directly along a row or column.

The Elves have already launched a quadcopter to generate a map with the height of each tree (your puzzle input). For example:

30373
25512
65332
33549
35390

Each tree is represented as a single digit whose value is its height, where 0 is the shortest and 9 is the tallest.

A tree is visible if all of the other trees between it and an edge of the grid are shorter than it. Only consider trees in the same row or column; that is, only look up, down, left, or right from any given tree.

All of the trees around the edge of the grid are visible - since they are already on the edge, there are no trees to block the view. In this example, that only leaves the interior nine trees to consider:

    The top-left 5 is visible from the left and top. (It isn't visible from the right or bottom since other trees of height 5 are in the way.)
    The top-middle 5 is visible from the top and right.
    The top-right 1 is not visible from any direction; for it to be visible, there would need to only be trees of height 0 between it and an edge.
    The left-middle 5 is visible, but only from the right.
    The center 3 is not visible from any direction; for it to be visible, there would need to be only trees of at most height 2 between it and an edge.
    The right-middle 3 is visible from the right.
    In the bottom row, the middle 5 is visible, but the 3 and 4 are not.

With 16 trees visible on the edge and another 5 visible in the interior, a total of 21 trees are visible in this arrangement.

Consider your map; how many trees are visible from outside the grid? */

import { List } from "immutable";
import * as R from "ramda";
import * as RA from "ramda-adjunct";
import { readLines } from "./readLines.mjs";

const forest: List<List<number | undefined>> = readLines("08.txt").reduce(
  (forest, line, r) => {
    const treeLine = line
      .split("")
      .reduce(
        (treeLine, tree, c) => treeLine.set(c, parseInt(tree, 10)),
        List<number | undefined>()
      );

    return forest.set(r, treeLine);
  },
  List<List<number | undefined>>()
);

class Coordinate {
  constructor(public r: number, public c: number) {}

  public get height(): number | undefined {
    const row = forest.get(this.r);
    if (row == null) {
      return undefined;
    }
    return row.get(this.c);
  }

  public get valid(): boolean {
    if (this.r < 0 || this.c < 0) {
      return false;
    }
    const row = forest.get(this.r);
    if (row == null) {
      return false;
    }

    return this.c < row.size;
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
    if (this.r == 0 || this.r == forest.size - 1) {
      return true;
    }
    const row = forest.get(this.r);
    if (row == null) {
      return false;
    }
    if (this.c == 0 || this.c == row.size - 1) {
      return true;
    }
    return false;
  }

  public distance(other: Coordinate): number {
    return Math.abs(this.c - other.c) + Math.abs(this.r - other.r);
  }
}

const getSameOrLargerNeighbor = (
  start: Coordinate,
  inc: (coordinate: Coordinate) => Coordinate
): [number | undefined, Coordinate] => {
  const height = start.height!;
  let previous = start;
  let current = inc(start);
  while (current.valid) {
    const nextHeight = current.height;
    if (nextHeight != null && nextHeight >= height) {
      return [nextHeight, current];
    }
    previous = current;
    current = inc(current);
  }
  return [undefined, previous];
};

const isVisible = (pos: Coordinate): boolean => {
  if (pos.isOnBorder()) {
    return true;
  }

  return R.any(
    (neighbor) => neighbor()[0] == null,
    [
      Coordinate.prototype.walkUp,
      Coordinate.prototype.walkRight,
      Coordinate.prototype.walkDown,
      Coordinate.prototype.walkLeft,
    ].map(
      (walkFn) => () =>
        getSameOrLargerNeighbor(pos, (pos: Coordinate) => walkFn.bind(pos)())
    )
  );
};

// eslint-disable-next-line prettier/prettier
const mapForest = <T,>(
  mapFn: (height: number | undefined, coordinate: Coordinate) => T
): List<List<T>> => {
  return forest.reduce((updatedForest, treeLine, r) => {
    return updatedForest.set(
      r,
      treeLine.reduce((updatedTreeLine, height, c) => {
        return updatedTreeLine.set(c, mapFn(height, new Coordinate(r, c)));
      }, List<T>())
    );
  }, List<List<T>>());
};

const forestWithVisibleTrees = mapForest((height, coordinate) =>
  isVisible(coordinate) ? height : undefined
);

console.log(
  R.count(
    RA.isNotUndefined,
    forestWithVisibleTrees.flatten().toArray() as (number | undefined)[]
  )
);

const calculateScenicScore = (start: Coordinate): number => {
  return R.reduce(
    R.multiply,
    1,
    [
      Coordinate.prototype.walkUp,
      Coordinate.prototype.walkRight,
      Coordinate.prototype.walkDown,
      Coordinate.prototype.walkLeft,
    ].map((walkFn) => {
      const [, end] = getSameOrLargerNeighbor(start, (pos: Coordinate) =>
        walkFn.bind(pos)()
      );

      return start.distance(end);
    })
  );
};

const forestWithScenicScore = mapForest((_, coordinate) =>
  calculateScenicScore(coordinate)
);

console.log(
  Math.max(...(forestWithScenicScore.flatten().toArray() as number[]))
);
