import ApiSystemDept from '@/services/system/ApiSystemDept';
import { handleTree } from '@/utils/utils';
import { Tag, TreeSelect } from 'antd';
import React, { useEffect, useState } from 'react';

interface SystemDeptTreeSelectProps {
  value?: string;
  onChange?: (value: string) => void;
  allowClear?: boolean;
  onlyLeafCanSelected?: boolean;
  readonly?: boolean;
  multiple?: boolean;
}

/** 部门树形选择 */
const SystemDeptTreeSelect: React.FC<SystemDeptTreeSelectProps> = (props) => {
  const [loading, setLoading] = useState(false);
  const [listData, setListData] = useState<
    { deptId: string; deptName: string; children: [] }[]
  >([]);
  const [treeData, setTreeData] = useState<
    { deptId: string; deptName: string; children: [] }[]
  >([]);

  const loadDs = () => {
    if (treeData.length < 1) {
      setLoading(true);
      ApiSystemDept.list({})
        .then((data: any) => {
          setListData(data);
          const ds = handleTree(data, 'deptId');
          setTreeData(ds);
        })
        .finally(() => setLoading(false));
    }
  };

  const getValueItem: () => any[] = () => {
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
      const item = listData.find((ds) => ds.deptId === v);
      return item
        ? { label: item.deptName, value: item.deptId }
        : { label: v, value: v };
    });
  };

  useEffect(() => {
    loadDs();
  }, []);

  if (props.readonly) {
    const items = getValueItem();
    return (
      <span>
        {items.map((item) => (
          <Tag key={item.value}>{item.label}</Tag>
        ))}
      </span>
    );
  }
  return (
    <TreeSelect
      loading={loading}
      popupMatchSelectWidth
      multiple={props.multiple}
      value={props.value}
      treeData={treeData}
      allowClear={props.allowClear}
      fieldNames={{
        value: 'deptId',
        label: 'deptName',
        children: 'children',
      }}
      placeholder="部门"
      treeDefaultExpandAll
      onChange={props.onChange}
    />
  );
};

export default SystemDeptTreeSelect;
