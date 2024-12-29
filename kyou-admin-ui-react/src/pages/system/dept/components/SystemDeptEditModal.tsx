import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import useProFormTreeSelectDept from '@/pages/system/dept/hooks/useProFormTreeSelectDept';
import { EditModalProps } from '@/typing';
import {
  ProFormDigit,
  ProFormRadio,
  ProFormText,
  ProFormTreeSelect,
} from '@ant-design/pro-components';
import React from 'react';

const SystemDeptEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props} grid={true} labelCol={{}} wrapperCol={{}}>
      <ProFormTreeSelect
        name={'parentId'}
        label={'上级部门'}
        rules={[{ required: true }]}
        {...useProFormTreeSelectDept()}
      />
      <ProFormText
        colProps={{ md: 12 }}
        label={'部门名称'}
        name={'deptName'}
        rules={[{ required: true }]}
      />
      <ProFormDigit
        colProps={{ md: 12 }}
        label={'显示排序'}
        name={'orderNum'}
        rules={[{ required: true }]}
      />
      <ProFormText colProps={{ md: 12 }} label={'负责人'} name={'leader'} />
      <ProFormText colProps={{ md: 12 }} label={'联系电话'} name={'phone'} />
      <ProFormText colProps={{ md: 12 }} label={'邮箱'} name={'email'} />
      <ProFormRadio.Group
        colProps={{ md: 12 }}
        label={'状态'}
        name={'status'}
        request={useProFormSelectDictRequest({
          typeCode: 'sys_normal_disable',
        })}
      />
    </EditModalForm>
  );
};

export default SystemDeptEditModal;
