import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import { EditModalProps } from '@/typing';
import { ProFormDigit, ProFormSelect, ProFormText, ProFormTextArea } from '@ant-design/pro-components';
import React from 'react';

const SystemPostEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props}>
      <ProFormText name="postName" label="岗位名称" rules={[{ required: true }]} />
      <ProFormText name={'postCode'} label="岗位编码" rules={[{ required: true }]} />
      <ProFormDigit name="postSort" label="显示顺序" rules={[{ required: true }]} min={0} max={9999} />
      <ProFormSelect
        name={'status'}
        label="状态"
        rules={[{ required: true }]}
        request={useProFormSelectDictRequest({ typeCode: 'sys_normal_disable' })}
      />
      <ProFormTextArea name={'remark'} label="备注" />
    </EditModalForm>
  );
};

export default SystemPostEditModal;
