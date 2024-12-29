import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import TableRowViewButton from '@/components/BizButtons/TableRowViewButton';
import EllipsisText from '@/components/EllipsisText';
import useActionControl from '@/hooks/useActionControl';
import SystemRoleDataScopeModal from '@/pages/system/role/components/SystemRoleDataScopeModal';
import SystemRoleEditModal from '@/pages/system/role/components/SystemRoleEditModal';
import useSystemRoleDataScopeModal from '@/pages/system/role/hooks/useSystemRoleDataScopeModal';
import ApiSystemMenu from '@/services/system/ApiSystemMenu';
import ApiSystemRole from '@/services/system/ApiSystemRole';
import { history } from '@@/core/history';
import { TagOutlined, UserOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { App, Switch, Tag } from 'antd';

const SystemRoleIndex = () => {
  const { message } = App.useApp();
  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemRole.add(values),
    },
    editAction: {
      onModalOpen: async (values) => {
        const [roleDetail, menuTreeSelect] = await Promise.all([
          ApiSystemRole.detail(values.roleId),
          ApiSystemMenu.roleMenuTreeSelect(values.roleId),
        ]);
        return { ...roleDetail, menuIds: menuTreeSelect.checkedKeys };
      },
      onActionCall: (values) => ApiSystemRole.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemRole.remove(values.roleId),
    },
    viewAction: {
      onModalOpen: (values) => ApiSystemRole.detail(values.roleId),
      onActionCall: Promise.resolve,
    },
  });

  const dataScopeModal = useSystemRoleDataScopeModal();

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params) => {
          const response = await ApiSystemRole.list(params);
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
            permissionsRequired={['system:role:add']}
          />,
        ]}
        rowKey={'roleId'}
        columns={[
          { title: '角色编号', dataIndex: 'roleId' },
          { title: '角色名称', dataIndex: 'roleName' },
          { title: '权限字符', dataIndex: 'roleKey' },
          {
            title: '显示顺序',
            dataIndex: 'roleSort',
            hideInSearch: true,
          },
          {
            title: '状态',
            dataIndex: 'status',
            renderText: (_text, record) => {
              return (
                <Switch
                  checked={record.status === '0'}
                  title={`${record.status === '0' ? '启用(点击切换)' : '停用(点击切换)'}`}
                />
              );
            },
            valueEnum: {
              '0': (
                <Tag color={'green'} className={'cursor-pointer'} title={'点击切换状态'}>
                  正常
                </Tag>
              ),
              '1': (
                <Tag color={'red'} className={'cursor-pointer'} title={'点击切换状态'}>
                  停用
                </Tag>
              ),
            },
            onCell: (record, index) => {
              return {
                onClick: () => {
                  console.log(record, index);
                  ApiSystemRole.changeStatus({
                    roleId: record.roleId,
                    status: record.status === '0' ? '1' : '0',
                  }).then(() => {
                    message.success('修改成功');
                    actionControl?.table.actionRef?.current?.reload();
                  });
                },
              };
            },
          },
          {
            title: '备注',
            dataIndex: 'remark',
            renderText: (text) => <EllipsisText text={text} />,
          },
          { title: '创建时间', dataIndex: 'createTime', hideInSearch: true },
          {
            title: '操作',
            fixed: 'right',
            width: actionControl.rowAction.width,
            hideInSearch: true,
            render: (text, record) => [
              <div key={'operations'} ref={actionControl.rowAction.ref}>
                <TableRowViewButton actionControl={actionControl} record={record} />
                <TableRowEditButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:role:edit']}
                />
                <TableRowDelButton
                  actionControl={actionControl}
                  record={record}
                  permissionsRequired={['system:role:remove']}
                />

                <PermissionButton
                  permissionsRequired={['system:role:edit']}
                  title={'数据权限'}
                  type={'link'}
                  key={'authDataScope'}
                  icon={<TagOutlined />}
                  onClick={() => dataScopeModal.openModal(record.roleId)}
                ></PermissionButton>
                <PermissionButton
                  permissionsRequired={['system:role:edit']}
                  icon={<UserOutlined />}
                  type={'link'}
                  title={'分配用户'}
                  onClick={() => history.push(`/system/roleAuthUser?roleId=${record.roleId}`)}
                />
              </div>,
            ],
          },
        ]}
      />
      <SystemRoleEditModal {...actionControl.editModal} />
      <SystemRoleDataScopeModal
        open={dataScopeModal.open}
        loading={dataScopeModal.loadingState.value}
        roleId={dataScopeModal.roleId}
        role={dataScopeModal.role}
        deptDataSource={dataScopeModal.depts}
        onCancel={dataScopeModal.onCancel}
        formRef={dataScopeModal.formRef}
      />
    </div>
  );
};

export default SystemRoleIndex;
