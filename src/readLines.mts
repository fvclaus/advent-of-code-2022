import { createReadStream } from "node:fs";
import { dirname, join } from "node:path";
import { createInterface } from "node:readline";
import { fileURLToPath } from "node:url";

export const readLines = async function* (
  filename: string
): AsyncGenerator<string, boolean, void> {
  const __filename = fileURLToPath(import.meta.url);
  const __dirname = dirname(__filename);

  const fileStream = createReadStream(join(__dirname, filename));

  const rl = createInterface({
    input: fileStream,
    crlfDelay: Infinity,
  });
  for await (const line of rl) {
    yield line;
  }
  return true;
};
