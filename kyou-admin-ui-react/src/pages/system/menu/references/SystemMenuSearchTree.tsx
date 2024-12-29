import SearchTree from '@/components/ProFormItem/FormSearchTree';
import ApiSystemMenu from '@/services/system/ApiSystemMenu';
import { FormItemProps } from '@/typing';
import React from 'react';

type SystemMenuSearchTreeProps = FormItemProps & {
  checkable?: boolean;
  checkStrictly?: boolean;
  action?: string;
};

const SystemMenuSearchTree: React.FC<SystemMenuSearchTreeProps> = (props) => {
  return (
    <SearchTree
      {...props}
      treeProps={{ id: 'id', label: 'label', children: 'children' }}
      checkable={true}
      loadData={() => ApiSystemMenu.treeSelect({})}
      placeholder="请输入菜单名称"
    />
  );
};

export default SystemMenuSearchTree;
