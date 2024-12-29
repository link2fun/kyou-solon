import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import useActionControl from '@/hooks/useActionControl';
import useManualTreeExpandControl from '@/hooks/useManualTreeExpandControl';
import SystemDeptEditModal from '@/pages/system/dept/components/SystemDeptEditModal';
import ApiSystemDept from '@/services/system/ApiSystemDept';
import { handleTree } from '@/utils/utils';
import {
  ExpandAltOutlined,
  PlusOutlined,
  ShrinkOutlined,
} from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { Button } from 'antd';

const SystemDeptIndex = () => {
  const actionControl = useActionControl({
    editAction: {
      onModalOpen: (values) => ApiSystemDept.detail(values.deptId),
      onActionCall: (values) => ApiSystemDept.edit(values),
    },
    addAction: {
      onModalOpen: async (values) => {
        return { parentId: values?.deptId };
      },
      onActionCall: (values) => ApiSystemDept.add(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemDept.remove(values.deptId),
    },
  });

  const expandControl = useManualTreeExpandControl({
    treeProps: {
      id: 'deptId',
      children: 'children',
    },
    // 数据加载完成后展开全部
    onAfterDataLoad: () => expandControl.expandAll(),
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        defaultSize={'small'}
        pagination={false}
        request={async (params) => {
          // eslint-disable-next-line @typescript-eslint/no-unused-vars
          const { current, pageSize, ...restParams } = params;
          const data = await ApiSystemDept.list(restParams);

          const treeData = handleTree(data, 'deptId');
          expandControl.cacheTreeData(treeData);
          return {
            data: treeData,
            success: true,
            total: data.total,
          };
        }}
        rowKey={'deptId'}
        expandable={{
          onExpand: expandControl.onTableExpand,
          expandedRowKeys: expandControl.expandedKeys,
        }}
        toolBarRender={() => [
          <ActionControlAddButton
            actionControl={actionControl}
            key={'add'}
            permissionsRequired={['system:dept:add']}
          />,
          <Button
            key={'expandOrCollapseAll'}
            icon={
              expandControl.isExpandAll ? (
                <ShrinkOutlined />
              ) : (
                <ExpandAltOutlined />
              )
            }
            title={'点击切换展开/折叠全部'}
            onClick={() => {
              expandControl.toggleExpandAll();
            }}
          >
            {expandControl.isExpandAll ? '折叠全部' : '展开全部'}
          </Button>,
        ]}
        scroll={{ x: 'max-content' }}
        columns={[
          {
            title: '部门名称',
            dataIndex: 'deptName',
            key: 'deptName',
          },
          {
            title: '领导人',
            dataIndex: 'leader',
            key: 'leader',
          },
          {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            valueType: 'select',
            valueEnum: {
              0: { text: '正常', status: 'Success' },
              1: { text: '停用', status: 'Error' },
            },
          },
          {
            title: '排序',
            dataIndex: 'orderNum',
            key: 'orderNum',
            hideInSearch: true,
          },

          {
            title: '操作',
            hideInSearch: true,
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_, record) => [
              <div key={'operations'} ref={actionControl.rowAction.ref}>
                <PermissionButton
                  title={'新增下级'}
                  permissionsRequired={['system:dept:add']}
                  type="link"
                  icon={<PlusOutlined />}
                  onClick={() => {
                    actionControl.actions.openAddModal({
                      parentId: record.deptId,
                    });
                  }}
                ></PermissionButton>
                <TableRowEditButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:dept:edit']}
                />
                <TableRowDelButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:dept:remove']}
                />
              </div>,
            ],
          },
        ]}
      />

      <SystemDeptEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemDeptIndex;
