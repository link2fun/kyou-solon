const Convert = {
  /** Convert any to boolean */
  toBoolean: (value: string | boolean | undefined | number, defaultValue: boolean = false) => {
    if (value === undefined) {
      return defaultValue;
    }
    return value === 'true' || value === '1' || value === true || value === 'yes' || value === 'Y' || value === 'TRUE';
  },

  /** Convert any boolean to number boolean */
  toBooleanStr1or0: (
    value: string | boolean | undefined | number,
    defaultValue: boolean = false,
    trueValue: any = '1',
    falseValue: any = '0',
  ) => {
    return Convert.toBoolean(value, defaultValue) ? trueValue : falseValue;
  },
};

export default Convert;
