import type { DictValue } from '@/hooks/useDict';
import useDict from '@/hooks/useDict';
import { Select } from 'antd';
import React from 'react';

interface SystemDictValueSelectProps {
  typeCode: string;
  value?: string;
  onChange?: (value: string) => void;
  readonly?: boolean;
  /**
   * 值也显示在label中, 默认为 true
   * true: 值 / 说明
   * false: 说明
   *  */
  valueInLabel?: boolean;
}

const SystemDictDataSelect: React.FC<SystemDictValueSelectProps> = ({
  typeCode,
  value,
  onChange,
  readonly = false,
  valueInLabel = true,
}) => {
  const [selectDs] = useDict(typeCode);

  if (readonly) {
    const dictValue = (selectDs || []).find(
      (item: DictValue) => item.dictValue?.toString() === value?.toString(),
    );

    if (valueInLabel) {
      return (
        <span>
          {dictValue?.dictValue} / {dictValue?.dictLabel}
        </span>
      );
    }
    return <span>{dictValue?.dictLabel}</span>;
  }

  return (
    <Select
      value={value}
      onChange={onChange}
      allowClear
      style={{ minWidth: 120 }}
      placeholder="请选择"
    >
      {(selectDs || []).map((item: DictValue) => (
        <Select.Option value={item.dictValue} key={item.dictValue}>
          {valueInLabel
            ? `${item?.dictValue} / ${item.dictLabel}`
            : `${item.dictLabel}`}
        </Select.Option>
      ))}
    </Select>
  );
};

export default SystemDictDataSelect;
