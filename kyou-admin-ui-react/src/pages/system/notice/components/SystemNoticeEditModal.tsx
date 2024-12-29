import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import { EditModalProps } from '@/typing';

import RichEditor from '@/components/ProFormItem/RichEditor';
import {
  ProFormItem,
  ProFormSelect,
  ProFormText,
} from '@ant-design/pro-components';
import React from 'react';

const SystemNoticeEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props} labelCol={{ span: 4 }} wrapperCol={{ span: 19 }}>
      <ProFormText
        name={'noticeTitle'}
        label={'公告标题'}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        name={'noticeType'}
        label={'公告类型'}
        rules={[{ required: true }]}
        request={useProFormSelectDictRequest({ typeCode: 'sys_notice_type' })}
      />

      <ProFormSelect
        name={'status'}
        label={'状态'}
        rules={[{ required: true }]}
        request={useProFormSelectDictRequest({ typeCode: 'sys_notice_status' })}
      />

      <ProFormItem
        name={'noticeContent'}
        label={'内容'}
        rules={[{ required: true }]}
      >
        <RichEditor />
      </ProFormItem>
    </EditModalForm>
  );
};

export default SystemNoticeEditModal;
