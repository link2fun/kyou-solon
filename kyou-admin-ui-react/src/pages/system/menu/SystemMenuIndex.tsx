import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import TableRowViewButton from '@/components/BizButtons/TableRowViewButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import SystemMenuEditModal from '@/pages/system/menu/components/SystemMenuEditModal';
import ApiSystemMenu from '@/services/system/ApiSystemMenu';
import { handleTree } from '@/utils/utils';
import { PlusOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';

const SystemMenuIndex = () => {
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemMenu.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemMenu.getInfo(values.menuId),
      onActionCall: (values) => ApiSystemMenu.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemMenu.remove(values.menuId),
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params) => {
          const response = await ApiSystemMenu.list(params);
          const treeData = handleTree(response, 'menuId');
          return {
            data: treeData,
            success: true,
            total: treeData.length,
          };
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            actionControl={actionControl}
            key={'add'}
            permissionsRequired={['system:menu:add']}
          />,
        ]}
        rowKey={'menuId'}
        columns={[
          { title: '菜单名称', dataIndex: 'menuName' },
          { title: '图标', dataIndex: 'icon', width: 150, hideInSearch: true },
          {
            title: '排序',
            dataIndex: 'orderNum',
            width: 60,
            hideInSearch: true,
          },
          {
            title: '权限标识',
            dataIndex: 'perms',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '组件路径',
            dataIndex: 'component',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '菜单状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_normal_disable',
            }),
          },

          { title: '创建时间', dataIndex: 'createTime', hideInSearch: true },
          {
            title: '操作',
            fixed: 'right',
            width: actionControl.rowAction.width,
            hideInSearch: true,
            render: (text, record) => [
              <div key={'operations'} ref={actionControl.rowAction.ref}>
                <TableRowViewButton
                  actionControl={actionControl}
                  record={record}
                />
                <TableRowEditButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:menu:edit']}
                />
                <TableRowDelButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:menu:remove']}
                />
                <PermissionButton
                  permissionsRequired={['system:menu:add']}
                  icon={<PlusOutlined />}
                  type={'link'}
                  title={'新增下级'}
                  onClick={() => {
                    actionControl.actions.openAddModal({
                      parentId: record.menuId,
                    });
                  }}
                />
              </div>,
            ],
          },
        ]}
      />

      <SystemMenuEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemMenuIndex;
