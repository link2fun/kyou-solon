import ActionControlAddButton from '@/components/BizButtons/ActionControlAddButton';
import PermissionButton from '@/components/BizButtons/PermissionButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import TableRowViewButton from '@/components/BizButtons/TableRowViewButton';
import EllipsisText from '@/components/EllipsisText';
import useActionControl from '@/hooks/useActionControl';
import SystemDeptSearchTree from '@/pages/system/dept/references/SystemDeptSearchTree';
import SystemUserEditModal from '@/pages/system/user/components/SystemUserEditModal';
import SystemUserImportModal from '@/pages/system/user/components/SystemUserImportModal';
import ApiSystemUser from '@/services/system/ApiSystemUser';
import { dateRangeToRequestParams } from '@/utils/utils';
import { ExportOutlined, KeyOutlined, UserOutlined } from '@ant-design/icons';
import { FormInstance, ProForm, ProFormText, ProTable } from '@ant-design/pro-components';
import { history } from '@umijs/max';
import { App, Col, Row } from 'antd';
import React, { useEffect, useRef } from 'react';

const SystemUserIndex = () => {
  const { message, modal } = App.useApp();
  const [deptIdSelected, setDeptIdSelected] = React.useState<number>(0);

  const actionControl = useActionControl({
    addAction: {
      onActionCall: (values) => ApiSystemUser.add(values),
    },
    editAction: {
      onModalOpen: (values) => ApiSystemUser.detail(values.userId),
      onActionCall: (values) => ApiSystemUser.edit(values),
    },
    removeAction: {
      onActionCall: (values) => ApiSystemUser.remove(values.userId),
    },
    viewAction: {
      onModalOpen: (values) => ApiSystemUser.detail(values.userId),
      onActionCall: Promise.resolve,
    },
  });

  const formRef = useRef<FormInstance>();

  useEffect(() => {
    actionControl.actions.reloadTableData();
  }, [deptIdSelected]);

  return (
    <div>
      <Row>
        <Col span={24} lg={6}>
          <SystemDeptSearchTree value={deptIdSelected} onChange={(val) => setDeptIdSelected(val as number)} />
        </Col>
        <Col span={24} lg={18} className={'pl-1'}>
          <ProTable
            {...actionControl.table}
            request={async (
              // 第一个参数 params 查询表单和 params 参数的结合
              // 第一个参数中一定会有 pageSize 和  current ，这两个参数是 antd 的规范
              _params: any,
            ) => {
              const { createTime, ...rest } = _params;

              // 如果需要转化参数可以在这里进行修改
              const data = await ApiSystemUser.list({
                ...rest,
                ...dateRangeToRequestParams(createTime),
                deptId: deptIdSelected,
              });
              return {
                data: data.rows,
                // success 请返回 true，
                // 不然 table 会停止解析数据，即使有数据
                success: true,
                // 不传会使用 data 的长度，如果是分页一定要传
                total: data.total,
              };
            }}
            toolBarRender={() => [
              <ActionControlAddButton
                actionControl={actionControl}
                permissionsRequired={['system:user:add']}
                key={'add'}
              />,

              <PermissionButton
                key={'export'}
                icon={<ExportOutlined />}
                loading={actionControl.loading.value}
                permissionsRequired={['system:user:export']}
                onClick={() => actionControl.loading.wrap(() => ApiSystemUser.export(actionControl.queryParams.value))}
              >
                导出
              </PermissionButton>,
              <SystemUserImportModal key={'importModal'} />,
            ]}
            rowKey={'userId'}
            columns={[
              {
                title: '用户编号',
                dataIndex: 'userId',
                hideInTable: true,
                hideInSearch: true,
              },
              { title: '用户名称', dataIndex: 'userName' },
              { title: '用户昵称', dataIndex: 'nickName', hideInSearch: true },
              { title: '部门', dataIndex: 'deptName', hideInSearch: true },
              {
                title: '手机号码',
                dataIndex: 'phonenumber',
              },
              {
                title: '用户状态',
                dataIndex: 'status',
                valueType: 'select',
                valueEnum: { '0': '正常', '1': '停用' },
              },
              {
                title: '备注',
                dataIndex: 'remark',
                hideInSearch: true,
                renderText: (text) => <EllipsisText text={text} />,
              },
              {
                title: '创建时间',
                dataIndex: 'createTime',
                hideInSearch: true,
              },
              {
                title: '创建时间',
                dataIndex: 'createTime',
                valueType: 'dateRange',
                hideInTable: true,
              },
              {
                hideInSearch: true,
                fixed: 'right',
                title: '操作',
                width: actionControl.rowAction.width,
                render: (_, record) => {
                  return (
                    <div ref={actionControl.rowAction.ref}>
                      <TableRowEditButton
                        permissionsRequired={['system:user:edit']}
                        actionControl={actionControl}
                        record={record}
                      />
                      <TableRowDelButton
                        permissionsRequired={['system:user:remove']}
                        actionControl={actionControl}
                        record={record}
                      />
                      <TableRowViewButton actionControl={actionControl} record={record} />
                      <PermissionButton
                        permissionsRequired={['system:user:resetPwd']}
                        title={'重置密码'}
                        icon={<KeyOutlined />}
                        type={'link'}
                        onClick={() => {
                          const _modal = modal.confirm({
                            content: (
                              <ProForm
                                formRef={formRef}
                                submitter={{
                                  resetButtonProps: {},
                                  submitButtonProps: {
                                    danger: true,
                                  },
                                  searchConfig: {
                                    resetText: '取消',
                                    submitText: '确认修改',
                                  },
                                }}
                                onReset={() => _modal.destroy()}
                                onFinish={async (values: any) => {
                                  await ApiSystemUser.resetPwd({
                                    userId: record.userId,
                                    password: values.password,
                                  });
                                  message.success('重置成功');
                                  _modal.destroy();
                                  return true;
                                }}
                              >
                                <ProFormText.Password
                                  label={'新密码'}
                                  name={'password'}
                                  fieldProps={{ autoFocus: true }}
                                />
                              </ProForm>
                            ),
                            footer: false,
                          });
                        }}
                      />
                      <PermissionButton
                        permissionsRequired={['system:user:edit']}
                        icon={<UserOutlined />}
                        type={'link'}
                        title={'分配角色'}
                        onClick={() => history.push(`/system/userAuthRole?userId=${record.userId}`)}
                      />
                    </div>
                  );
                },
              },
            ]}
          />
        </Col>
      </Row>
      <SystemUserEditModal {...actionControl.editModal} />
    </div>
  );
};

export default SystemUserIndex;
