import { ExportOutlined } from '@ant-design/icons';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import SystemPostEditModal from '@/pages/system/post/components/SystemPostEditModal';
import ApiSystemPost from '@/services/system/ApiSystemPost';

const SystemPostIndex = () => {
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemPost.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemPost.detail(values.postId),
      onActionCall: (values) => ApiSystemPost.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemPost.remove(values.postId),
    },
  });

  return (
    <PageContainer>
      <ProTable
        {...actionControl.table}
        request={async (params: any) => {
          const data = await ApiSystemPost.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            actionControl={actionControl}
            key={'actionAdd'}
            permissionsRequired={['system:post:add']}
          />,
          <PermissionButton
            permissionsRequired={['system:post:export']}
            icon={<ExportOutlined />}
            key={'actionExport'}
            onClick={() => ApiSystemPost.export({})}
          >
            导出
          </PermissionButton>,
        ]}
        rowKey={'postId'}
        scroll={{ x: 'max-content' }}
        columns={[
          { title: '岗位编号', dataIndex: 'postId', search: false },
          { title: '岗位编码', dataIndex: 'postCode' },
          { title: '岗位名称', dataIndex: 'postName' },
          { title: '岗位排序', dataIndex: 'postSort', search: false },
          {
            title: '状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_normal_disable',
            }),
          },
          { title: '备注', dataIndex: 'remark', search: false },
          {
            title: '操作',
            search: false,
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_text, record) => [
              <div key={'operations'} ref={actionControl.rowAction.ref}>
                <TableRowEditButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:post:edit']}
                />
                <TableRowDelButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:post:remove']}
                />
              </div>,
            ],
          },
        ]}
      />

      <SystemPostEditModal {...actionControl.editModal} />
    </PageContainer>
  );
};

export default SystemPostIndex;
