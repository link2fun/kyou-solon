import React, { useMemo, useState } from 'react';

import { SwapOutlined } from '@ant-design/icons';
import * as Icons from '@ant-design/icons/lib/icons';
import { createField } from '@ant-design/pro-form/es/BaseForm';
import { ProFormFieldItemProps } from '@ant-design/pro-form/es/typing';
import { useDebounce } from 'ahooks';
import { Button, Input, InputProps, Modal } from 'antd';

const allIcons = Object.keys(Icons);

export type ProFormIconPickerProps = ProFormFieldItemProps<InputProps> & {
  value?: any;
  onChange?: any;
};

const BaseProFormIconPicker: React.FC<ProFormIconPickerProps> =
  React.forwardRef((props, ref: any) => {
    //   // 定义 state 打开关闭 Modal
    const [open, setOpen] = useState(false);
    // 定义 state 暂存搜索信息
    const [_search, setSearch] = useState<string>('');
    // 这么写是为了防止restProps中 带入 onChange, defaultValue, rules props tabUtil
    const { fieldProps } = props;

    const search = useDebounce(_search, { wait: 200 });

    const showIcons = useMemo(() => {
      return allIcons.filter(
        (iconKey) => iconKey.toLowerCase().indexOf(search.toLowerCase()) > -1,
      );
    }, [search]);

    let SelectedIcon = undefined;
    if (props.value as keyof typeof Icons) {
      SelectedIcon = Icons[props?.value as keyof typeof Icons];
    }
    // 这么写是为了防止restProps中 带入 onChange, defaultValue, rules props tabUtil

    if (
      props?.proFieldProps?.mode === 'read' ||
      props?.proFieldProps?.readonly ||
      props.readonly
    ) {
      return <>{SelectedIcon && <SelectedIcon />}</>;
    }

    // @ts-ignore
    return (
      <div
        style={{
          ...fieldProps?.style,
          display: 'flex',
          alignItems: 'center',
        }}
        ref={ref}
      >
        <Input
          {...fieldProps}
          addonAfter={SelectedIcon && <SelectedIcon />}
          style={{
            flex: 1,
            transition: 'width .3s',
            marginRight: 8,
            ...fieldProps?.style,
          }}
        />

        <Button
          style={{
            display: 'block',
          }}
          icon={<SwapOutlined />}
          onClick={async () => {
            setOpen(true);
          }}
        ></Button>
        <Modal
          open={open}
          onCancel={() => setOpen(false)}
          width={800}
          footer={false}
        >
          <Input.Search onChange={(e) => setSearch(e.target.value)} />
          <div className={'flex flex-wrap'}>
            {showIcons.map((_value) => {
              // If using Typescript
              const Icon = Icons[_value as keyof typeof Icons];

              return (
                <div
                  className={
                    'mx-2 my-3 cursor-pointer items-center flex flex-col'
                  }
                  key={_value}
                  onClick={() => {
                    // @ts-ignore
                    fieldProps?.onChange?.(_value as string);
                    setOpen(false);
                  }}
                >
                  <Icon
                    style={{ fontSize: 40 }}
                    title={fieldProps?.value as string}
                  />
                </div>
              );
            })}
          </div>
        </Modal>
      </div>
    );
  });

const ProFormIconPicker = createField(
  BaseProFormIconPicker,
) as typeof BaseProFormIconPicker;

export default ProFormIconPicker;
