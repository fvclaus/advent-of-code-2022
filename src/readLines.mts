import { createReadStream } from "node:fs";
import { createInterface } from "node:readline";
import { buildPath } from "./buildPath.mjs";

export const readLines = async function* (
  filename: string
): AsyncGenerator<string, boolean, void> {
  const fileStream = createReadStream(buildPath(filename));

  const rl = createInterface({
    input: fileStream,
    crlfDelay: Infinity,
  });
  for await (const line of rl) {
    yield line;
  }
  return true;
};
