import { useEffect, useState } from 'react';

interface SelectItem {
  value: number | string;
  label: string;
}

interface UseSelectProps {
  /** 使用的初始查询条件 */
  loadParams?: object;
  /** 加载数据源的方法 */
  loadData: (loadParams?: object) => Promise<SelectItem[]>;
  /** 从 ProForm 传入, 用于计算只读的值 */
  value?: any;
  /** 从 ProFrom 传入, 暂时未用 */
  onChange?: (value: any) => void;
}

const useSelectControl = (props: UseSelectProps) => {
  /** 定义加载状态 */
  const [loading, setLoading] = useState<boolean>(false);
  /** 数据源 */
  const [selectDs, setSelectDs] = useState<SelectItem[]>([]);
  /** 数据源是否已经加载过 */
  const [loaded, setLoaded] = useState<boolean>(false);

  /** 加载数据 */
  const loadDs = () => {
    if (loaded) {
      // 已经加载过数据源
      return;
    }
    // 现在去加载
    setLoading(true);
    props
      .loadData(props.loadParams || {})
      .then((ds) => {
        setSelectDs(ds);
        setLoaded(true);
      })
      .catch(() => {})
      .finally(() => {
        setLoading(false);
      });
  };

  /** 获取只读值, 是一个 array 用来渲染只读表单 */
  const getValueItem = () => {
    if (props.value === undefined) {
      // 没有选中值
      return [];
    }
    let _value: any[];
    if (Array.isArray(props.value)) {
      _value = props.value;
    } else {
      _value = [props.value];
    }
    return _value.map((v) => {
      const item = selectDs.find((ds) => ds.value === v);
      return item ? item : { label: v, value: v };
    });
  };

  const getValueLabel = () => {
    return getValueItem().map((item) => item.label);
  };

  useEffect(() => {
    loadDs();
  }, []);

  return {
    options: selectDs,
    loading,
    value: props.value,
    onChange: props.onChange,
    getValueItem,
    getValueLabel,
  };
};

export default useSelectControl;
