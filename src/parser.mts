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

export const digits: Parser<number> = (str: string) => {
  const [head, rest] = some(
    satisfy((input: string) => input.match(/[0-9]/) != null)
  )(str);
  if (head == null || rest == null) {
    return [];
  }
  return [parseInt(head, 10), rest];
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

export const whitespace = some(satisfy((input) => input.match(/\s/) != null));

// @ts-ignore
export function seq<Parser1Type, R>(
  parser: [Parser<Parser1Type>],
  reducer: (input: [Parser1Type]) => R
): Parser<R>;

export function seq<Parser1Type, Parser2Type, R>(
  parser: [Parser<Parser1Type>, Parser<Parser2Type>],
  reducer: (input: [Parser1Type, Parser2Type]) => R
): Parser<R>;

export function seq<Parser1Type, Parser2Type, Parser3Type, R>(
  parser: [Parser<Parser1Type>, Parser<Parser2Type>, Parser<Parser3Type>],
  reducer: (input: [Parser1Type, Parser2Type, Parser3Type]) => R
): Parser<R>;

export function seq(
  parsers: Parser<unknown>[],
  reducer: (input: unknown[]) => unknown
): Parser<unknown> {
  return (str) => {
    const recurse = (
      remainingParsers: Parser<unknown>[],
      [memo, remainingStr]: [unknown[], string]
    ): [unknown[], string] | [] => {
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
