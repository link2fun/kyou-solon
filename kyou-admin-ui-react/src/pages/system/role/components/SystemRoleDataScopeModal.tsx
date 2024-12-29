import EditModalForm from '@/components/EditModalForm';
import SystemDeptSearchTree from '@/pages/system/dept/references/SystemDeptSearchTree';
import ApiSystemRole from '@/services/system/ApiSystemRole';
import {
  FormInstance,
  ProFormCheckbox,
  ProFormDependency,
  ProFormItem,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { useGetState, usePrevious } from 'ahooks';
import React, { useMemo } from 'react';

interface SystemRoleDataScopeModalProps {
  open?: boolean;
  loading?: boolean;
  roleId?: string;
  role?: Partial<SysRoleDTO>;
  deptDataSource?: any[];
  onCancel: () => void;
  formRef?: React.MutableRefObject<FormInstance | undefined>;
}

const SystemRoleDataScopeModal = ({ open, loading, roleId, onCancel, formRef }: SystemRoleDataScopeModalProps) => {
  const dataScopeOptions = [
    { value: '1', label: '全部数据权限' },
    { value: '2', label: '自定数据权限' },
    { value: '3', label: '本部门数据权限' },
    { value: '4', label: '本部门及以下数据权限' },
    { value: '5', label: '仅本人数据权限' },
  ];

  const [formValues, setFormValues, getFormValues] = useGetState<any>({});
  const previousFormValues = usePrevious(getFormValues());

  const action = useMemo(() => {
    const prevOptions = previousFormValues?.dataScopeOptions || [];
    const nowOptions = formValues?.dataScopeOptions || [];

    if (prevOptions.includes('deptExpand') && !nowOptions.includes('deptExpand')) {
      return 'expandAll';
    } else if (!prevOptions.includes('deptExpand') && nowOptions.includes('deptExpand')) {
      return 'collapseAll';
    } else if (prevOptions.includes('deptNodeAll') && !nowOptions.includes('deptNodeAll')) {
      return 'checkAll';
    } else if (!prevOptions.includes('deptNodeAll') && nowOptions.includes('deptNodeAll')) {
      return 'uncheckAll';
    }
    return undefined;
  }, [formValues]);

  return (
    <EditModalForm
      open={open}
      formRef={formRef}
      loading={loading || false}
      action={'edit'}
      initData={{}}
      onCancel={onCancel}
      onSubmit={async (values: any) => {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        const { dataScopeOptions, ...formValues } = values;
        await ApiSystemRole.dataScope({ roleId, ...formValues });
        onCancel();
      }}
      onValuesChange={(changedValues, values) => {
        setFormValues(values);
      }}
    >
      <ProFormText label={'角色名称'} proFieldProps={{ mode: 'read' }} name={'roleName'} />
      <ProFormText label={'权限字符'} proFieldProps={{ mode: 'read' }} name={'roleKey'} />
      <ProFormSelect label={'权限范围'} name={'dataScope'} options={dataScopeOptions} />

      <ProFormDependency name={['dataScope', 'dataScopeOptions']}>
        {({ dataScope, dataScopeOptions }) => {
          if (dataScope !== '2') {
            // 不是 自定数据权限 就不显示
            return undefined;
          }
          const deptCheckStrictly = dataScopeOptions?.includes('deptCheckStrictly');
          // deptCheckStrictly 的语义跟 ant-design tree checkStrictly 相反
          return (
            <>
              <ProFormCheckbox.Group
                label={'数据权限'}
                name={'dataScopeOptions'}
                options={[
                  { label: '折叠', value: 'deptExpand' },
                  { label: '取消全选', value: 'deptNodeAll' },
                  { label: '父子联动', value: 'deptCheckStrictly' },
                ]}
              />
              <ProFormItem label={'部门权限'} name={'deptIds'}>
                <SystemDeptSearchTree action={action} checkable={true} checkStrictly={!deptCheckStrictly} />
              </ProFormItem>
            </>
          );
        }}
      </ProFormDependency>

      <ProFormTextArea label={'备注'} name={'remark'} />
    </EditModalForm>
  );
};

export default SystemRoleDataScopeModal;
