import useLoadingState from '@/hooks/useLoadingState';
import useTableRowSelection from '@/hooks/useTableRowSelection';
import ApiSystemUser from '@/services/system/ApiSystemUser';
import { ProTable } from '@ant-design/pro-components';
import { useLocation } from '@umijs/max';
import { App, Button, Descriptions, Divider, Popconfirm, Space } from 'antd';
import { useEffect, useMemo, useState } from 'react';

interface PageData {
  roles: SysRoleDTO[];
  user: Partial<SysUserDTO>;
}

type getAuthRoleProps = PageData & AjaxResult<never>;

/** 给用户分配角色 */
const SystemUserAuthRole = () => {
  const { search } = useLocation();
  const { message } = App.useApp();
  const loadingState = useLoadingState();

  const userId: string = useMemo(() => {
    const searchParams = new URLSearchParams(search);
    if (searchParams.has('userId')) {
      return searchParams.get('userId') as string;
    }
    return '';
  }, [search]);

  const [pageData, setPageData] = useState<PageData>({ user: {}, roles: [] });

  const list = useMemo(() => {
    return pageData.roles;
  }, [pageData]);

  const { rowSelection, setSelected } = useTableRowSelection<SysRoleDTO>(list, {
    itemKey: 'roleId',
  });

  const loadData = async () => {
    loadingState.begin();
    try {
      const data: getAuthRoleProps = await ApiSystemUser.getAuthRole(userId);
      setPageData(data);
      setSelected(data.user.roles || []);
    } catch (e) {
    } finally {
      loadingState.end();
    }
  };

  const handleAuthRole = async () => {
    if (typeof rowSelection === 'boolean') {
      return;
    }
    const roleIds = rowSelection.selectedRowKeys as string[];
    try {
      loadingState.begin();
      await ApiSystemUser.authRole(userId, roleIds);
      message.success('分配成功');
      await loadData();
    } catch (e) {
    } finally {
      loadingState.end();
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  return (
    <div>
      <Descriptions
        layout={'horizontal'}
        className={'w-full'}
        title={<Divider orientation={'left'}>基本信息</Divider>}
      >
        <Descriptions.Item label={'用户昵称'}>
          {pageData.user.nickName}
        </Descriptions.Item>
        <Descriptions.Item label={'登录账号'}>
          {pageData.user.userName}
        </Descriptions.Item>
        <Descriptions.Item label={'所属部门'}>
          {pageData.user.deptName}
        </Descriptions.Item>
      </Descriptions>
      <Descriptions
        layout={'horizontal'}
        column={1}
        className={''}
        title={<Divider orientation={'left'}>角色信息</Divider>}
      >
        <Descriptions.Item>
          <ProTable
            dataSource={pageData.roles}
            loading={loadingState.value}
            className={'w-full'}
            search={false}
            options={false}
            rowSelection={rowSelection}
            tableAlertOptionRender={() => {
              return (
                <Space size={16}>
                  <Button.Group>
                    <Popconfirm
                      title={'请问是否继续?'}
                      okText={'继续'}
                      cancelText={'取消'}
                      onConfirm={handleAuthRole}
                    >
                      <Button type={'primary'} loading={loadingState.value}>
                        分配
                      </Button>
                    </Popconfirm>
                  </Button.Group>
                </Space>
              );
            }}
            rowKey={'roleId'}
            columns={[
              { title: '角色编号', dataIndex: 'roleId' },
              { title: '角色名称', dataIndex: 'roleName' },
              { title: '权限字符', dataIndex: 'roleKey' },
              {
                title: '创建时间',
                dataIndex: 'createTime',
                valueType: 'dateTime',
              },
            ]}
          />
        </Descriptions.Item>
      </Descriptions>
    </div>
  );
};

export default SystemUserAuthRole;
