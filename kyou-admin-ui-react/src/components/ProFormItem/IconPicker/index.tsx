import * as Icons from '@ant-design/icons';
import { SwapOutlined } from '@ant-design/icons';
import { ProForm, type ProFormItemProps } from '@ant-design/pro-components';
import { useDebounce } from 'ahooks';
import { Button, Input, type InputProps, Modal, Space } from 'antd';
import React, { useMemo, useState } from 'react';

const allIcons = Object.keys(Icons);

export type ProFormIconPickerProps = Omit<ProFormItemProps, 'children'> & {
  fieldProps?: InputProps;
};

const IconPickerContent: React.FC<{
  value?: any;
  onChange?: (value: any) => void;
  fieldProps?: InputProps;
  readonly?: boolean;
}> = ({ value, onChange, fieldProps, readonly }) => {
  const [open, setOpen] = useState(false);
  const [_search, setSearch] = useState<string>('');

  const search = useDebounce(_search, { wait: 200 });

  const showIcons = useMemo(() => {
    return allIcons.filter(
      (iconKey) => iconKey.toLowerCase().indexOf(search.toLowerCase()) > -1,
    );
  }, [search]);

  let SelectedIcon: any;
  if (value) {
    SelectedIcon = (Icons as Record<string, any>)[value as string];
  }

  // 只读模式下只展示已选图标
  if (readonly) {
    return <>{SelectedIcon && <SelectedIcon />}</>;
  }

  return (
    <div
      style={{
        ...fieldProps?.style,
        display: 'flex',
        alignItems: 'center',
      }}
    >
      <Space.Compact style={{ flex: 1, marginRight: 8 }}>
        <Input
          {...fieldProps}
          value={value}
          onChange={(e) => onChange?.(e.target.value)}
          style={{
            transition: 'width .3s',
            ...fieldProps?.style,
          }}
        />
        {SelectedIcon && (
          <Button
            icon={<SelectedIcon />}
            disabled
            style={{ pointerEvents: 'none' }}
          />
        )}
      </Space.Compact>

      <Button
        style={{ display: 'block' }}
        icon={<SwapOutlined />}
        onClick={() => setOpen(true)}
      />
      <Modal
        open={open}
        onCancel={() => setOpen(false)}
        width={800}
        footer={false}
        title="选择图标"
      >
        <Input.Search onChange={(e) => setSearch(e.target.value)} />
        <div
          className={'flex flex-wrap'}
          style={{ maxHeight: '60vh', overflowY: 'auto' }}
        >
          {showIcons.map((_value) => {
            const Icon = (Icons as Record<string, any>)[_value as string];
            return (
              <div
                className={
                  'mx-2 my-3 cursor-pointer items-center flex flex-col'
                }
                key={_value}
                onClick={() => {
                  onChange?.(_value as string);
                  setOpen(false);
                }}
              >
                <Icon style={{ fontSize: 40 }} title={value as string} />
              </div>
            );
          })}
        </div>
      </Modal>
    </div>
  );
};

const ProFormIconPicker: React.FC<ProFormIconPickerProps> = ({
  proFieldProps,
  fieldProps,
  ...restProps
}) => {
  const readonly = proFieldProps?.readonly ?? proFieldProps?.mode === 'read';
  return (
    <ProForm.Item {...restProps}>
      <IconPickerContent fieldProps={fieldProps} readonly={readonly} />
    </ProForm.Item>
  );
};

export default ProFormIconPicker;
