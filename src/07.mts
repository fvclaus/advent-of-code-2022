import { chars, choice, satisfy, seq, some } from "./parser.mjs";

// const fileTreeParser = (input: string): ParseResult<object>  => {
//     return
// }

const input = "$ cd /";

let rest: string | undefined = input;
let output = null;

const alphaNumericCharacter = satisfy(
  (char) => char.match(/^[a-z0-9/.]+$/i) != null
);

const digit = satisfy((char) => char.match(/[0-9]/) != null);

[output, rest] = chars("$ cd ")(rest);
if (rest == null) {
  throw new Error(`Abort`);
}
[output, rest] = some(alphaNumericCharacter)(rest);

console.log(output, rest);

rest = input;
[output, rest] = seq(
  [chars("$ cd "), some(alphaNumericCharacter)],
  (result) => {
    console.log(result);
    return result.join("");
  }
)(rest);

const text = `
$ cd /
$ ls
dir a
14848514 b.txt
8504156 c.dat
dir d
$ cd a
$ ls
dir e
29116 f
2557 g
62596 h.lst
$ cd e
$ ls
584 i
$ cd ..
$ cd ..
$ cd d
$ ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k
`
  .trim()
  .split("\n");

interface Folder {
  [name: string]: Folder | number;
}

let fileSystem: Folder = {
  "/": {},
};

let currentDir = fileSystem;

for (const line of text) {
  const [newFileSystem] = choice<Folder>(
    seq<Folder>(
      [chars("$ cd "), some(alphaNumericCharacter)],
      ([_, dirName]) => {
        if (dirName == null) {
          throw new Error(`Did not find dirname`);
        }
        const folder = currentDir[dirName];
        if (folder == null) {
          throw new Error(`dir ${dirName} does not exist`);
        }
        if (typeof folder === "number") {
          throw new Error(`dir ${dirName} is actually a file`);
        }
        currentDir = folder;
        return fileSystem;
      }
    ),
    seq<Folder>([chars("$ ls")], () => {
      return fileSystem;
    }),
    seq<Folder>([chars("dir "), some(alphaNumericCharacter)], ([, dirName]) => {
      if (dirName == null) {
        throw new Error(`Did not find dirname`);
      }
      currentDir[dirName] = {
        "..": currentDir,
      };
      return fileSystem;
    }),
    seq<Folder>(
      [some(digit), chars(" "), some(alphaNumericCharacter)],
      ([size, _, fileName]) => {
        if (fileName == null || size == null) {
          throw new Error(`Input not valid`);
        }
        currentDir[fileName] = parseInt(size, 10);
        return fileSystem;
      }
    )
  )(line);

  if (newFileSystem == null) {
    throw new Error(`Could not parse line ${line}`);
  }
  fileSystem = newFileSystem;
}
console.log(fileSystem);
