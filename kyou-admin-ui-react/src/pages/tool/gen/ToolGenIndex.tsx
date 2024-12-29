import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import useActionControl from '@/hooks/useActionControl';
import TableGenTableImportModal from '@/pages/tool/gen/components/TableGenTableImportModal';
import ToolGenPreviewModal from '@/pages/tool/gen/components/ToolGenPreviewModal';
import useToolGenPreview from '@/pages/tool/gen/hooks/useToolGenPreview';
import useToolGenTableImport from '@/pages/tool/gen/hooks/useToolGenTableImport';
import ApiToolGen from '@/services/tool/ApiToolGen';
import { history } from '@@/core/history';
import { CloudDownloadOutlined, EditOutlined, EyeOutlined, ImportOutlined, SyncOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';

const ToolGenIndex = () => {
  const actionControl = useActionControl({
    editAction: {
      onModalOpen: (values) => ApiToolGen.detail(values.tableId),
      onActionCall: (values) => ApiToolGen.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiToolGen.remove([values.tableId]),
    },
  });

  const previewModal = useToolGenPreview(actionControl.loading);

  const tableImportModal = useToolGenTableImport(actionControl);

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (_params) => {
          // 非首页时添加 totalRow 参数, 用于指定数据总条数, 指定后 服务端将不会执行 count 查询
          const params = actionControl.queryParams.wrapPageTotal(_params);

          const data = await ApiToolGen.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <PermissionButton
            key={'import'}
            type={'primary'}
            icon={<ImportOutlined />}
            permissionsRequired={['tool:gen:import']}
            onClick={tableImportModal.openImportModal}
          >
            导入表结构
          </PermissionButton>,
        ]}
        rowKey="tableId"
        columns={[
          { title: '表名称', dataIndex: 'tableName', key: 'tableName' },
          { title: '表描述', dataIndex: 'tableComment', key: 'tableComment' },
          { title: '实体', dataIndex: 'className', key: 'className' },
          {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            valueType: 'dateTime',
          },
          {
            title: '更新时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            valueType: 'dateTime',
          },
          {
            title: '操作',
            valueType: 'option',
            key: 'option',
            render: (text, record) => {
              return (
                <div>
                  <PermissionButton
                    title={'预览'}
                    type={'link'}
                    icon={<EyeOutlined />}
                    onClick={() => previewModal.onPreview(record.tableId)}
                  ></PermissionButton>
                  <PermissionButton
                    title={'修改生成配置'}
                    icon={<EditOutlined />}
                    type={'link'}
                    onClick={() => {
                      history.push(`/tool/genEdit?tableId=${record.tableId}`);
                    }}
                  />
                  <TableRowDelButton actionControl={actionControl} record={record} />
                  <PermissionButton
                    title={'同步'}
                    icon={<SyncOutlined />}
                    type={'link'}
                    loading={actionControl.loading.value}
                    onClick={() =>
                      actionControl.loading.wrap({
                        action: () => ApiToolGen.synchDb(record.tableName),
                        loadingMessage: '同步中...',
                        successMessage: '同步成功',
                      })
                    }
                  />
                  <PermissionButton
                    title={'生成代码'}
                    icon={<CloudDownloadOutlined />}
                    type={'link'}
                    loading={actionControl.loading.value}
                    onClick={() =>
                      actionControl.loading.wrap({
                        action: async () => {
                          const { genType } = record;
                          if (genType === '1') {
                            return await ApiToolGen.genCode(record.tableName);
                          }
                          return ApiToolGen.download(record.tableName);
                        },
                        loadingMessage: '生成中...',
                        successMessage: '生成成功',
                      })
                    }
                  />
                </div>
              );
            },
          },
        ]}
      />

      <ToolGenPreviewModal
        open={previewModal.modal.open}
        onCancel={previewModal.modal.onCancel}
        items={previewModal.modal.items}
        onRefresh={previewModal.modal.onRefresh}
      />

      <TableGenTableImportModal
        open={tableImportModal.modal.open}
        onCancel={tableImportModal.modal.onCancel}
        selection={tableImportModal.modal.selection}
        dataSource={tableImportModal.modal.dataSource}
        onConditionChange={tableImportModal.modal.onConditionChange}
        onSubmit={tableImportModal.modal.onSubmit}
      />
    </div>
  );
};

export default ToolGenIndex;
