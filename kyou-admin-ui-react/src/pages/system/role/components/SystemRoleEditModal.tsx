import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import SystemMenuSearchTree from '@/pages/system/menu/references/SystemMenuSearchTree';
import { EditModalProps } from '@/typing';
import {
  ProFormCheckbox,
  ProFormDependency,
  ProFormDigit,
  ProFormItem,
  ProFormRadio,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import { useGetState, usePrevious } from 'ahooks';
import React, { useMemo } from 'react';

const SystemRoleEditModal: React.FC<EditModalProps> = (props) => {
  const [formValues, setFormValues, getFormValues] = useGetState<any>({});
  const previousFormValues = usePrevious(getFormValues());

  const action = useMemo(() => {
    const prevOptions = previousFormValues?.dataScopeOptions || [];
    const nowOptions = formValues?.dataScopeOptions || [];

    if (prevOptions.includes('menuExpand') && !nowOptions.includes('menuExpand')) {
      return 'expandAll';
    } else if (!prevOptions.includes('menuExpand') && nowOptions.includes('menuExpand')) {
      return 'collapseAll';
    } else if (prevOptions.includes('menuNodeAll') && !nowOptions.includes('menuNodeAll')) {
      return 'checkAll';
    } else if (!prevOptions.includes('menuNodeAll') && nowOptions.includes('menuNodeAll')) {
      return 'uncheckAll';
    }
    return undefined;
  }, [formValues]);

  return (
    <EditModalForm
      {...props}
      onValuesChange={(changedValues, values) => {
        setFormValues(values);
      }}
    >
      <ProFormText label={'角色名称'} name={'roleName'} rules={[{ required: true, message: '请输入角色名称！' }]} />
      <ProFormText
        label={'权限字符'}
        name={'roleKey'}
        tooltip={'控制器中定义的权限字符，如：@PreAuthorize("@ss.hasRole(\'admin\')")'}
        rules={[{ required: true, message: '请输入权限字符！' }]}
      />
      <ProFormDigit label={'角色顺序'} name={'roleSort'} min={0} max={9999} rules={[{ required: true }]} />

      <ProFormRadio.Group
        label={'状态'}
        name={'status'}
        rules={[{ required: true }]}
        request={useProFormSelectDictRequest({
          typeCode: 'sys_normal_disable',
        })}
      />

      <ProFormDependency name={['dataScopeOptions']}>
        {({ dataScopeOptions }) => {
          const menuCheckStrictly = dataScopeOptions?.includes('menuCheckStrictly');
          // menuCheckStrictly 的语义跟 ant-design tree checkStrictly 相反
          return (
            <>
              <ProFormCheckbox.Group
                label={'数据权限'}
                name={'dataScopeOptions'}
                options={[
                  { label: '折叠', value: 'menuExpand' },
                  { label: '取消全选', value: 'menuNodeAll' },
                  { label: '父子联动', value: 'menuCheckStrictly' },
                ]}
              />

              <ProFormItem name={'menuIds'} label={'菜单'}>
                <SystemMenuSearchTree action={action} checkable={true} checkStrictly={!menuCheckStrictly} />
              </ProFormItem>
            </>
          );
        }}
      </ProFormDependency>

      <ProFormTextArea label={'备注'} name={'remark'} />
    </EditModalForm>
  );
};

export default SystemRoleEditModal;
