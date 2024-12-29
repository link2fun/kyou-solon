import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import SystemDeptTreeSelect from '@/pages/system/dept/references/SystemDeptTreeSelect';
import SystemPostSelect from '@/pages/system/post/references/SystemPostSelect';
import SystemRoleSelect from '@/pages/system/role/references/SystemRoleSelect';
import { EditModalProps } from '@/typing';
import {
  ProFormItem,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import React from 'react';

const SystemUserEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props}>
      <ProFormText
        label={'用户昵称'}
        name={'nickName'}
        rules={[{ required: true }]}
      />
      <ProFormItem name={'deptId'} label={'归属部门'}>
        <SystemDeptTreeSelect readonly={props.readonly} />
      </ProFormItem>
      <ProFormText label={'手机号码'} name={'phonenumber'} />
      <ProFormText label={'邮箱'} name={'email'} />
      <ProFormSelect
        label={'性别'}
        name={'sex'}
        request={useProFormSelectDictRequest({ typeCode: 'sys_user_sex' })}
      />
      <ProFormSelect
        label={'状态'}
        name={'status'}
        request={useProFormSelectDictRequest({
          typeCode: 'sys_normal_disable',
        })}
      />
      <ProFormItem label={'岗位'} name={'postIds'}>
        <SystemPostSelect mode={'multiple'} readonly={props.readonly} />
      </ProFormItem>
      <ProFormItem label={'角色'} name={'roleIds'}>
        <SystemRoleSelect mode={'multiple'} readonly={props.readonly} />
      </ProFormItem>

      {props.action === 'add' && (
        <>
          <ProFormText
            label={'用户名称'}
            name={'userName'}
            rules={[{ required: true, message: '请输入用户名称' }]}
          />
          <ProFormText.Password
            label={'密码'}
            name={'password'}
            rules={[{ required: true }]}
          />
        </>
      )}
      <ProFormTextArea label={'备注'} name={'remark'} />
    </EditModalForm>
  );
};

export default SystemUserEditModal;
