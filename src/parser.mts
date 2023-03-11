export type ParseResult<R> = [R, string] | [];
export type Parser<R = string> = (input: string) => ParseResult<R>;

export const nothing: Parser = (str: string) => ["", str];

export const satisfy =
  (fn: (input: string) => boolean): Parser =>
  (input: string) => {
    if (fn(input[0]!)) {
      return [input[0]!, input.slice(1)];
    } else {
      return [];
    }
  };

export const chars =
  (c: string): Parser =>
  (str) =>
    str.startsWith(c) ? [c, str.slice(c.length)] : [];

export const char =
  (c: string): Parser =>
  (str) =>
    str.length && str[0] === c ? [str[0], str.slice(1)] : [];

export const range =
  (start: string, end: string): Parser =>
  (str: string) =>
    str[0]! >= start && str[0]! <= end ? [str[0]!, str.slice(1)] : [];

export const some =
  (parser: Parser): Parser =>
  (str: string) => {
    const recurse = (
      memo: string,
      remaining: string
      // TODO Typeduplizierung
    ): [string, string] | [] => {
      if (!remaining) {
        return [memo, remaining];
      }
      const result = parser(remaining);
      return !result.length
        ? !memo
          ? []
          : [memo, remaining]
        : recurse(`${memo}${result[0]}`, result[1]);
    };
    return recurse("", str);
  };

export function seq<R>(
  parsers: Parser[],
  reducer: (input: string[]) => R
): Parser<R> {
  return (str) => {
    const recurse = (
      remainingParsers: Parser[],
      [memo, remainingStr]: [string[], string]
    ): [string[], string] | [] => {
      if (!remainingParsers.length) {
        return [memo, remainingStr];
      }
      const result = remainingParsers[0]!(remainingStr);
      return !result.length
        ? []
        : recurse(remainingParsers.slice(1), [
            [...memo, result[0]],
            result[1]!,
          ]);
    };

    const [result, remaining] = recurse(parsers, [[], str]);
    return !result ? [] : [reducer(result), remaining!];
  };
}

export const choice =
  // eslint-disable-next-line prettier/prettier
  <R,>(...parsers: Parser<R>[]): Parser<R> =>
    (str: string) => {
      if (!parsers.length) {
        return [];
      }
      const result = parsers[0]!(str);
      return result.length ? result : choice(...parsers.slice(1))(str);
    };
