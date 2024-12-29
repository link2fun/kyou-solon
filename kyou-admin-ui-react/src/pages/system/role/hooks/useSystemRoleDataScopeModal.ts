import useLoadingState from '@/hooks/useLoadingState';
import ApiSystemRole from '@/services/system/ApiSystemRole';
import { FormInstance } from '@ant-design/pro-components';
import { useGetState } from 'ahooks';
import { useRef, useState } from 'react';

const useSystemRoleDataScopeModal = () => {
  const loadingState = useLoadingState();

  // state open
  const [open, setOpen] = useState<boolean>(false);
  const [roleId, setRoleId] = useState<string>('');
  const [checkedKeys, setCheckedKeys, getCheckedKeys] = useGetState<string[]>(
    [],
  );
  const [depts, setDepts] = useState<any[]>([]);
  const [role, setRole] = useState<Partial<SysRoleDTO>>({});

  const formRef = useRef<FormInstance>();

  const loadModalData = async (_roleId: string) => {
    const data = await ApiSystemRole.deptTree(_roleId);
    const { checkedKeys: _checkedKeys, depts } = data;
    // checkedKeys 是 Array<number> 需要转为 Array<string>
    let checkedKeys = _checkedKeys.map(String);
    setCheckedKeys(checkedKeys);
    // setCheckedKeys(checkedKeys);
    setDepts(depts);
    const role = await ApiSystemRole.detail(_roleId);
    setRole(role);

    setTimeout(() => {
      formRef.current?.setFieldsValue({
        ...role,
        deptIds: getCheckedKeys(),
        dataScopeOptions: [], // 不能默认加上 'deptCheckStrictly', 不然如果增加了子部门, 界面上会显示成已勾选实际未勾选
      });
    }, 200);
  };

  const openModal = async (roleId: string) => {
    setRoleId(roleId);
    loadingState.begin();
    setOpen(true);
    await loadModalData(roleId);
    loadingState.end();
  };

  const onCancel = () => {
    setOpen(false);
    setCheckedKeys([]);
    setDepts([]);
  };

  return {
    open,
    loadingState,
    openModal,
    onCancel,
    depts,
    checkedKeys,
    roleId,
    role,
    formRef,
  };
};

export default useSystemRoleDataScopeModal;
