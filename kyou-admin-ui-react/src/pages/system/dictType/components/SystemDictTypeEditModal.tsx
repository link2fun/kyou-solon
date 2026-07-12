import {
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import React from 'react';
import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import type { EditModalProps } from '@/typing';

const SystemDictTypeEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props}>
      <ProFormText
        name={'dictName'}
        label={'字典名称'}
        rules={[{ required: true }]}
      />
      <ProFormText
        name={'dictType'}
        label={'字典类型'}
        rules={[
          { required: true },
          {
            pattern: /^[A-Za-z0-9_]+$/,
            message: '字典类型只能包含字母、数字、下划线',
          },
        ]}
      />
      <ProFormSelect
        name={'status'}
        label={'状态'}
        request={useProFormSelectDictRequest({
          typeCode: 'sys_normal_disable',
        })}
      />
      <ProFormTextArea name={'remark'} label={'备注'} />
    </EditModalForm>
  );
};

export default SystemDictTypeEditModal;
