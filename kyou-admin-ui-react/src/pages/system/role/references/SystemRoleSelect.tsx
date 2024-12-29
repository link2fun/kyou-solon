import useSelectControl from '@/hooks/useSelectControl';
import ApiSystemRole from '@/services/system/ApiSystemRole';
import { Select, Tag } from 'antd';
import React from 'react';

interface SystemRoleSelectProps {
  value?: number;
  onChange?: (value: number) => void;
  allowClear?: boolean;
  mode?: 'multiple' | 'tags' | undefined;
  readonly?: boolean;
}

const SystemRoleSelect: React.FC<SystemRoleSelectProps> = (props) => {
  const roleSelect = useSelectControl({
    onChange: props.onChange,
    value: props.value,
    loadParams: {},
    loadData: async (params) => {
      const data = await ApiSystemRole.list(params);
      return data.rows.map((item: any) => {
        return {
          value: item.roleId,
          label: item.roleName,
        };
      });
    },
  });

  if (props.readonly) {
    // 只读模式
    return (
      <span>
        {roleSelect.getValueItem().map((item) => (
          <Tag key={item.value}>{item.label}</Tag>
        ))}
      </span>
    );
  }

  return (
    <Select
      options={roleSelect.options}
      mode={props.mode}
      onChange={props.onChange}
      value={props.value}
      loading={roleSelect.loading}
      allowClear={props.allowClear}
    ></Select>
  );
};

export default SystemRoleSelect;
