import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import SystemNoticeEditModal from '@/pages/system/notice/components/SystemNoticeEditModal';
import ApiSystemNotice from '@/services/system/ApiSystemNotice';
import { ProTable } from '@ant-design/pro-components';

const SystemNoticeIndex = () => {
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemNotice.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemNotice.detail(values.noticeId),
      onActionCall: (values) => ApiSystemNotice.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemNotice.remove(values.noticeId),
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params) => {
          const data = await ApiSystemNotice.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            actionControl={actionControl}
            key={'add'}
            permissionsRequired={['system:notice:add']}
          />,
        ]}
        rowKey={'noticeId'}
        columns={[
          { title: '序号', dataIndex: 'noticeId' },
          { title: '公告标题', dataIndex: 'noticeTitle', ellipsis: true },
          {
            title: '公告类型',
            dataIndex: 'noticeType',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_notice_type',
            }),
          },
          {
            title: '状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_notice_status',
            }),
          },
          { title: '创建者', dataIndex: 'createBy' },
          { title: '创建时间', dataIndex: 'createTime', valueType: 'dateTime' },
          {
            title: '操作',
            key: 'operations',
            fixed: 'right',
            width: actionControl.rowAction.width,
            renderText: (_text, record) => {
              return (
                <div ref={actionControl.rowAction.ref}>
                  <TableRowEditButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['system:notice:edit']}
                  />
                  <TableRowDelButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['system:notice:remove']}
                  />
                </div>
              );
            },
          },
        ]}
      />

      <SystemNoticeEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemNoticeIndex;
