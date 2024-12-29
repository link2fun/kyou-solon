import ApiSystemDictData from '@/services/system/ApiSystemDictData';
import { useGetState, useSessionStorageState } from 'ahooks';
import { useEffect } from 'react';

export type DictValue = {
  dictType: string;
  dictValue: string;
  dictLabel: string;
  dictSort: number;
};

type UseDictProps = {
  typeCode: string;
  valueInLabel?: boolean;
};

const useDict = (props: UseDictProps) => {
  // 定义一个 state 来标记数据是否已经加载过
  const [loaded, setLoaded, getLoaded] = useGetState<boolean>(false);

  // 使用session管理缓存数据, 每次关闭浏览器后数据会被清空
  const [dict, setDict] = useSessionStorageState<DictValue[]>(
    `DICT:${props.typeCode}`,
    { defaultValue: [] },
  );

  const fetchDict = async () => {
    const data = await ApiSystemDictData.listByType(props.typeCode);
    setDict(data);
  };

  const proFormSelectRequest: () => Promise<any[]> = async () => {
    // 判断是否已经加载过数据, 如果没有加载过, 则延迟等待 loaded == true
    if (!getLoaded() && (dict || []).length < 1) {
      await new Promise((resolve) => {
        const timer = setInterval(() => {
          if (getLoaded()) {
            clearInterval(timer);
            resolve({});
          }
        }, 100);
      });
    }
    return (dict || [])?.map((item: DictValue) => {
      if (props.valueInLabel === true) {
        return {
          value: item.dictValue,
          label: `${item.dictValue} / ${item.dictLabel}`,
        };
      }
      return { value: item.dictValue, label: item.dictLabel };
    });
  };

  useEffect(() => {
    if (!loaded) {
      if ((dict?.length || 0) > 0) {
        setLoaded(true);
        return;
      }
      // 如果还没有加载过 则去加载一次
      fetchDict()
        .then()
        .catch(() => {})
        .finally(() => setLoaded(true));
    }
  }, [props.typeCode]);

  return { dict, proFormSelectRequest };
};

export default useDict;
