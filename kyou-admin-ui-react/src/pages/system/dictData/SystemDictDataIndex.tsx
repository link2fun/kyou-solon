import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import useActionControl from '@/hooks/useActionControl';
import SystemDictDataEditModal from '@/pages/system/dictData/components/SystemDictDataEditModal';
import ApiSystemDictData from '@/services/system/ApiSystemDictData';
import ApiSystemDictType from '@/services/system/ApiSystemDictType';
import { ExportOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { Tag } from 'antd';

const SystemDictDataIndex = () => {
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemDictData.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemDictData.detail(values.dictCode),
      onActionCall: (values) => ApiSystemDictData.edit(values),
    },
    extraActionMap: {
      remove: {
        onActionCall: (values) => ApiSystemDictData.remove(values.dictCode),
      },
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params) => {
          const data = await ApiSystemDictData.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            key={'add'}
            actionControl={actionControl}
            permissionsRequired={['system:dict:add']}
          />,
          <PermissionButton
            key={'export'}
            permissionsRequired={['system:dict:export']}
            loading={actionControl.loading.value}
            icon={<ExportOutlined />}
            onClick={() =>
              actionControl.loading.wrap(() => ApiSystemDictData.export({}))
            }
          >
            导出
          </PermissionButton>,
        ]}
        rowKey={'dictCode'}
        columns={[
          {
            title: '字典类型',
            dataIndex: 'dictType',
            valueType: 'select',
            request: async () => {
              const data = await ApiSystemDictType.list({});
              return data.rows.map(
                (item: { dictName: string; dictType: string }) => ({
                  label: item.dictName,
                  value: item.dictType,
                }),
              );
            },
          },
          { title: '字典编码', dataIndex: 'dictCode', hideInSearch: true },
          { title: '字典标签', dataIndex: 'dictLabel' },
          { title: '字典键值', dataIndex: 'dictValue', hideInSearch: true },
          {
            title: '状态',
            dataIndex: 'status',
            valueEnum: {
              '0': <Tag color={'green'}>正常</Tag>,
              '1': <Tag color={'red'}>停用</Tag>,
            },
          },
          {
            title: '备注',
            dataIndex: 'remark',
            ellipsis: true,
            hideInSearch: true,
          },
          { title: '创建时间', dataIndex: 'createTime', hideInSearch: true },
          {
            title: '操作',
            hideInSearch: true,
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_text, record) => [
              <div key={'operations'} ref={actionControl.rowAction.ref}>
                <TableRowEditButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:dict:edit']}
                />
                <TableRowDelButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:dict:remove']}
                />
              </div>,
            ],
          },
        ]}
      />

      <SystemDictDataEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemDictDataIndex;
