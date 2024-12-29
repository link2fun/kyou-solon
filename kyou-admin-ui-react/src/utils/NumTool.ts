import CollectionTool from '@/utils/CollectionTool';
import * as math from 'mathjs';
import { MathExpression, Matrix } from 'mathjs';

/** 找到第一个不是 undefined 的值 */
function findFirst<T>(...args: T[]): T {
  return args.find((item) => item !== undefined)!;
}

/**
 * 根据当前变化的key, 来获取最新的值
 * @param key 当前变化的key
 * @param changedKeys 变化的key
 * @param changedValues 变化的值
 * @param allValues 所有的值
 * @returns {any}
 */
export function getLatestValue(
  key: string,
  changedKeys: string[],
  changedValues: any,
  allValues: any,
): any {
  if (!CollectionTool.containsItem(changedKeys, key)) {
    return parseFloat(allValues[key]) || 0;
  }
  return parseFloat(changedValues[key]) || 0;
}

/**
 * 将输入的字符串或数字转换为number类型，并最多保留8位小数，如果精度不足8位小数，则保留实际精度。
 * 如果输入不是数字或无法转换为数字，则返回默认值。
 * @param {string|number} input - 输入的字符串或数字
 * @param {number} defaultValue - 默认值，如果输入不是数字或无法转换为数字，则返回该值
 * @returns {number} - 转换后的结果，如果输入不是数字或无法转换为数字，则返回默认值
 */
const convertToNumberWithPrecision = (
  input: string | number | undefined,
  precision = 8,
  defaultValue: number,
) => {
  let num;

  if (typeof input === 'string') {
    num = parseFloat(input);
  } else if (typeof input === 'number') {
    num = input;
  } else {
    return defaultValue;
  }

  // 使用 toFixed() 方法保留最多precision位小数，同时限制小数位数在实际精度和precision之间
  return parseFloat(
    num.toFixed(
      Math.min(precision, (num.toString().split('.')[1] || '').length),
    ),
  );
};

/**
 * Evaluate an expression.
 * @param expr The expression to be evaluated
 * @param scope Scope to read/write variables
 * @param precision The precision of the result
 * @param defaultValue  The default value if the result is not a number
 * @returns The result of the expression
 */
const evaluate = (
  expr: MathExpression | Matrix,
  scope?: object,
  precision = 8,
  defaultValue = 0,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
) => {
  return convertToNumberWithPrecision(
    math.evaluate(expr, scope),
    precision,
    defaultValue,
  );
};

export default {
  findFirst,
  getLatestValue,
  evaluate,
  convertToNumberWithPrecision,
};
