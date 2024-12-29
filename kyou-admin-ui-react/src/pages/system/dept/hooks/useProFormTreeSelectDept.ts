import ApiSystemDept from '@/services/system/ApiSystemDept';
import { handleTree } from '@/utils/utils';

const useProFormTreeSelectDept = () => {
  const fieldProps = {
    fieldNames: {
      value: 'deptId',
      label: 'deptName',
      children: 'children',
    },
  };
  const request = async () => {
    const listData = await ApiSystemDept.list({});
    const treeData = handleTree(listData, 'deptId');

    const menuTree = { deptId: 0, deptName: '根目录', children: [] } as any;
    menuTree.children = treeData;
    return [menuTree];
  };

  return {
    fieldProps,
    request,
  };
};

export default useProFormTreeSelectDept;
