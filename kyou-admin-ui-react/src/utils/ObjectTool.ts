/** 判断对象是否为 Null */
const isNull = (obj: any) => {
  return !obj;
};

/**
 * 判断对象 obj 是否包含属性 keys 中的任意一个
 * @param obj
 * @param keys
 * @returns {boolean}
 */
const hasAnyKey = (obj: any, keys: string[]) => {
  return keys.some((key) => obj.hasOwnProperty(key));
};

export default {
  isNull,
  hasAnyKey,
  pick(allValues: any, keys: (string | number | (string | number)[])[]) {
    const result: any = {};
    keys.forEach((key) => {
      if (Array.isArray(key)) {
        key.forEach((k) => {
          result[k] = allValues[k];
        });
      } else {
        result[key] = allValues[key];
      }
    });
    return result;
  },
};
