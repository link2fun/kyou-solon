import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import EllipsisText from '@/components/EllipsisText';
import useActionControl from '@/hooks/useActionControl';
import SystemConfigEditModal from '@/pages/system/config/components/SystemConfigEditModal';
import ApiSystemConfig from '@/services/system/ApiSystemConfig';
import { DeleteOutlined, ExportOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';

const SystemConfigIndex = () => {
  const actionControl = useActionControl({
    editAction: {
      onModalOpen: (values) => ApiSystemConfig.detail(values.configId),
      onActionCall: (values) => ApiSystemConfig.edit(values),
    },
    addAction: {
      onActionCall: (values) => ApiSystemConfig.add(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemConfig.remove(values.configId),
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (_params: any) => {
          const params = actionControl.queryParams.wrapPageTotal(_params);

          const data = await ApiSystemConfig.list(params);
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
            permissionsRequired={['system:config:add']}
          />,
          <PopconfirmButton
            key={'refreshCache'}
            permissionsRequired={['system:config:remove']}
            buttonText={'刷新缓存'}
            title={'刷新缓存后, 会清空所有缓存, 确认刷新?'}
            buttonProps={{
              loading: actionControl.loading.value,
              danger: true,
              icon: <DeleteOutlined />,
            }}
            onConfirm={() =>
              actionControl.loading.wrap({
                action: () => ApiSystemConfig.refreshCache(),
                loadingMessage: '刷新缓存中...',
                successMessage: '刷新缓存成功',
              })
            }
          />,
          <PermissionButton
            key={'export'}
            permissionsRequired={['system:config:export']}
            icon={<ExportOutlined />}
            loading={actionControl.loading.value}
            onClick={() =>
              actionControl.loading.wrap({
                action: () => ApiSystemConfig.export({}),
                loadingMessage: '数据导出中，请稍后...',
              })
            }
          >
            导出
          </PermissionButton>,
        ]}
        scroll={{ x: 'max-content' }}
        rowKey={'configId'}
        columns={[
          {
            title: '参数主键',
            dataIndex: 'configId',
            hideInTable: true,
            hideInSearch: true,
          },
          { title: '参数名称', dataIndex: 'configName' },
          { title: '参数键名', dataIndex: 'configKey' },
          { title: '参数键值', dataIndex: 'configValue', hideInSearch: true },
          {
            title: '系统内置',
            dataIndex: 'configType',
            valueType: 'select',
            valueEnum: { N: '否', Y: '是' },
          },
          {
            title: '备注',
            dataIndex: 'remark',
            renderText: (text) => <EllipsisText text={text} />,
          },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            hideInSearch: true,
          },
          {
            title: '操作',
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_, record) => {
              return (
                <div ref={actionControl.rowAction.ref}>
                  <TableRowEditButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['system:config:edit']}
                  />
                  <TableRowDelButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['system:config:remove']}
                  />
                </div>
              );
            },
          },
        ]}
      />

      <SystemConfigEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemConfigIndex;
