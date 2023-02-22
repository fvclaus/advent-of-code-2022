import { createReadStream, readFileSync } from "node:fs";
import { createInterface } from "node:readline";
import { buildPath } from "./buildPath.mjs";

export const iterateLines = async function* (
  filename: string
): AsyncGenerator<string, boolean, void> {
  const fileStream = createReadStream(buildPath(filename));

  const rl = createInterface({
    input: fileStream,
    crlfDelay: Infinity,
  });
  for await (const line of rl) {
    if (line.trim() !== "") {
      yield line;
    }
  }
  return true;
};

export const readLines = (filename: string): string[] => {
  const content = readFileSync(buildPath(filename)).toString();
  const lines = content.split("\n");
  return lines.filter((line) => line.trim() !== "");
};
