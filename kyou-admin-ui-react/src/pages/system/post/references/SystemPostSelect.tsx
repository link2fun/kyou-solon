import useSelectControl from '@/hooks/useSelectControl';
import ApiSystemPost from '@/services/system/ApiSystemPost';
import { Select, Tag } from 'antd';
import React from 'react';

interface SystemPostSelectProps {
  value?: number;
  onChange?: (value: number) => void;
  allowClear?: boolean;
  mode?: 'multiple' | 'tags' | undefined;
  readonly?: boolean;
}

const SystemPostSelect: React.FC<SystemPostSelectProps> = (props) => {
  const selectControl = useSelectControl({
    loadData: async () => {
      const data = await ApiSystemPost.list({});
      return data.rows.map((item: any) => {
        return {
          value: item.postId,
          label: item.postName,
        };
      });
    },
    value: props.value,
    onChange: props.onChange,
  });

  if (props.readonly) {
    return (
      <span>
        {selectControl.getValueItem().map((item) => (
          <Tag key={item.value}>{item.label}</Tag>
        ))}
      </span>
    );
  }

  return (
    <Select
      options={selectControl.options}
      mode={props.mode}
      onChange={props.onChange}
      value={props.value}
      loading={selectControl.loading}
      allowClear={props.allowClear}
    ></Select>
  );
};

export default SystemPostSelect;
