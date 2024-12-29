import { Switch as AntdSwitch, SwitchProps } from 'antd';
import React from 'react';

type CusSwitchProps = SwitchProps & {
  value?: boolean;
  onChange?: (value: boolean) => void;
  checkedChildren?: React.ReactNode;
  unCheckedChildren?: React.ReactNode;
  activeValue?: any;
  inactiveValue?: any;
};

const FormSwitch: React.FC<CusSwitchProps> = ({
  value,
  onChange,
  checkedChildren,
  unCheckedChildren,
  activeValue = true,
  inactiveValue = false,
  ...restProps
}) => {
  return (
    <AntdSwitch
      checkedChildren={checkedChildren}
      unCheckedChildren={unCheckedChildren}
      checked={value === activeValue}
      onChange={(_value) => {
        if (_value) {
          onChange?.(activeValue);
        } else {
          onChange?.(inactiveValue);
        }
      }}
      {...restProps}
    />
  );
};

export default FormSwitch;
