import ApiSystemMenu from '@/services/system/ApiSystemMenu';
import { handleTree } from '@/utils/utils';
import { ProFormTreeSelect } from '@ant-design/pro-components';
import React from 'react';

const SystemMenuSelect: React.FC<any> = (props) => {
  const fetchData = async () => {
    const menuList = await ApiSystemMenu.list({});
    const subMenuTree: any[] = handleTree(menuList, 'menuId');
    const menuTree = { menuId: 0, menuName: '主类目', children: [] } as any;
    menuTree.children = subMenuTree;
    return [menuTree];
  };

  return (
    <ProFormTreeSelect
      fieldProps={{
        fieldNames: {
          label: 'menuName',
          value: 'menuId',
          children: 'children',
        },
        multiple: true,
        treeCheckable: true,
      }}
      request={fetchData}
      {...props}
    />
  );
};

export default SystemMenuSelect;
