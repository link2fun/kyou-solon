import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import useActionControl from '@/hooks/useActionControl';
import ApiSystemDictType from '@/services/system/ApiSystemDictType';
import { ClearOutlined, ExportOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { Tag } from 'antd';

const SystemDictTypeIndex = () => {
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemDictType.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemDictType.detail(values.dictTypeId),
      onActionCall: (values) => ApiSystemDictType.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemDictType.remove(values.dictTypeId),
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params) => {
          const response = await ApiSystemDictType.list(params);
          return {
            data: response.rows,
            success: true,
            total: response.total,
          };
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            actionControl={actionControl}
            key={'add'}
            permissionsRequired={['system:dict:add']}
          />,
          <PermissionButton
            key={'export'}
            permissionsRequired={['system:dict:export']}
            icon={<ExportOutlined />}
            loading={actionControl.loading.value}
            onClick={() =>
              actionControl.loading.wrap(() => ApiSystemDictType.export({}))
            }
          >
            导出
          </PermissionButton>,
          <PopconfirmButton
            key={'clearCache'}
            permissionsRequired={['system:dict:remove']}
            buttonText={'清除缓存'}
            title={'确认清除字典缓存?'}
            buttonProps={{
              icon: <ClearOutlined />,
              danger: true,
              loading: actionControl.loading.value,
            }}
            onConfirm={() =>
              actionControl.loading.wrap(() => ApiSystemDictType.refreshCache())
            }
          />,
        ]}
        rowKey={'dictType'}
        columns={[
          { title: '字典名称', dataIndex: 'dictName' },
          { title: '字典类型', dataIndex: 'dictType' },
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
    </div>
  );
};

export default SystemDictTypeIndex;
