import EditModalForm from '@/components/EditModalForm';
import { EditModalProps } from '@/typing';
import { ProFormText } from '@ant-design/pro-components';
import React from 'react';

const SystemConfigEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props}>
      <ProFormText
        label={'参数名称'}
        name={'configName'}
        rules={[{ required: true, message: '请输入参数名称！' }]}
      />
      <ProFormText
        label={'参数键名'}
        name={'configKey'}
        rules={[{ required: true, message: '请输入参数键名！' }]}
      />
      <ProFormText
        label={'参数键值'}
        name={'configValue'}
        rules={[{ required: true, message: '请输入参数键值！' }]}
      />
      <ProFormText
        label={'系统内置'}
        name={'configType'}
        rules={[{ required: true, message: '请输入系统内置！' }]}
      />
      <ProFormText
        label={'备注'}
        name={'remark'}
        rules={[{ required: true, message: '请输入备注！' }]}
      />
    </EditModalForm>
  );
};

export default SystemConfigEditModal;
