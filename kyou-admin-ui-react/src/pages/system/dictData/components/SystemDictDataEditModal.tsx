import EditModalForm from '@/components/EditModalForm';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import ApiSystemDictType from '@/services/system/ApiSystemDictType';
import { EditModalProps } from '@/typing';
import {
  ProFormDigit,
  ProFormSelect,
  ProFormText,
  ProFormTextArea,
} from '@ant-design/pro-components';
import React from 'react';

const SystemDictDataEditModal: React.FC<EditModalProps> = (props) => {
  return (
    <EditModalForm {...props}>
      <ProFormSelect
        name={'dictType'}
        label={'字典类型'}
        rules={[{ required: true }]}
        request={async () => {
          const data = await ApiSystemDictType.list({});
          return data.rows.map(
            (item: { dictName: string; dictType: string }) => ({
              label: item.dictName,
              value: item.dictType,
            }),
          );
        }}
      />
      <ProFormText
        name={'dictLabel'}
        label={'字典标签'}
        rules={[{ required: true }]}
      />
      <ProFormText
        name={'dictValue'}
        label={'字典键值'}
        rules={[{ required: true }]}
      />
      <ProFormText name={'cssClass'} label={'CSS CLASS'} />
      <ProFormDigit
        name={'dictSort'}
        label={'字典排序'}
        rules={[{ required: true }]}
      />
      <ProFormSelect
        name={'listClass'}
        label={'回显样式'}
        options={[
          { label: '默认', value: 'default' },
          { label: '主要', value: 'primary' },
          { label: '成功', value: 'success' },
          { label: '信息', value: 'info' },
          { label: '警告', value: 'warning' },
          { label: '危险', value: 'danger' },
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

export default SystemDictDataEditModal;
