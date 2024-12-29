const isEmpty = (str: string) => {
  if (!str) {
    return true;
  }
  return str.length < 1;
};

const isBlank = (str: string) => {
  if (!str) {
    return true;
  }

  return str.trim().length < 1;
};

const isNotBlank = (str: string) => {
  return !isBlank(str);
};

export default {
  isEmpty,
  isBlank,
  isNotBlank,
};
