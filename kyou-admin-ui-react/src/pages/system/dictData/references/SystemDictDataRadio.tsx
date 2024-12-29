import useDict, { type DictValue } from '@/hooks/useDict';
import { ProFormRadio } from '@ant-design/pro-components';
import { ProFormRadioGroupProps } from '@ant-design/pro-form';
import React from 'react';

type SystemDictDataRadioProps = ProFormRadioGroupProps & {
  typeCode: string;
  value?: string;
  readonly?: boolean;
  /**
   * 值也显示在label中, 默认为 true
   * true: 值 / 说明
   * false: 说明
   *  */
  valueInLabel?: boolean;
};

const SystemDictDataRadio: React.FC<SystemDictDataRadioProps> = ({
  typeCode,
  value,
  readonly = false,
  valueInLabel = true,
  ...restProps
}) => {
  const { dict } = useDict({ typeCode, valueInLabel });

  if (readonly) {
    const dictValue = (dict || []).find(
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
    <ProFormRadio.Group
      allowClear
      style={{ minWidth: 120 }}
      placeholder="请选择"
      {...restProps}
      options={(dict || []).map((item: DictValue) => {
        return {
          value: item.dictValue,
          label: valueInLabel
            ? `${item.dictValue} / ${item.dictLabel}`
            : item.dictLabel,
        };
      })}
    ></ProFormRadio.Group>
  );
};

export default SystemDictDataRadio;
