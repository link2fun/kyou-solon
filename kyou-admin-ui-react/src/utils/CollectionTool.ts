import ObjectTool from '@/utils/ObjectTool';
import StrTool from '@/utils/StrTool';

const isEmpty = (list: string[]) => {
  if (ObjectTool.isNull(list)) {
    return true;
  }
  return list.length < 1;
};

const isNotEmpty = (list: string[]) => {
  return !isEmpty(list);
};

const containsItem = (list: string[], item: string) => {
  if (StrTool.isBlank(item)) {
    return false;
  }
  if (isEmpty(list)) {
    return false;
  }

  return list.findIndex((value) => value === item) >= 0;
};

const containsAny = (list: string[], items: string[]) => {
  if (isEmpty(items)) {
    return false;
  }

  return list.filter((s) => containsItem(items, s)).length > 0;
};

export default {
  isEmpty,
  isNotEmpty,
  containsItem,
  containsAny,
};
