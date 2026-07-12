import { LogoutOutlined } from '@ant-design/icons';
import { PageContainer, ProTable } from '@ant-design/pro-components';
import { App } from 'antd';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useActionControl from '@/hooks/useActionControl';
import ApiMonitorOnline from '@/services/monitor/ApiMonitorOnline';

const MonitorOnlineIndex = () => {
  const { message } = App.useApp();
  const actionControl = useActionControl({});

  return (
    <PageContainer>
      <ProTable
        {...actionControl.table}
        request={async (params: any) => {
          const data = await ApiMonitorOnline.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        rowKey={'tokenId'}
        columns={[
          {
            title: '会话编号',
            dataIndex: 'tokenId',
            ellipsis: true,
            search: false,
          },
          { title: '登录名称', dataIndex: 'userName' },
          { title: '所属部门', dataIndex: 'deptName', search: false },
          { title: '主机', dataIndex: 'ipaddr' },
          { title: '登录地点', dataIndex: 'loginLocation', search: false },
          {
            title: '操作系统',
            dataIndex: 'os',
            ellipsis: true,
            search: false,
          },
          { title: '浏览器', dataIndex: 'browser', search: false },
          {
            title: '登录时间',
            dataIndex: 'loginTime',
            valueType: 'dateTime',
            search: false,
          },
          {
            title: '操作',
            search: false,
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_, record) => {
              return (
                <div ref={actionControl.rowAction.ref}>
                  <PopconfirmButton
                    buttonText={'强退'}
                    permissionsRequired={['monitor:online:forceLogout']}
                    buttonProps={{
                      danger: true,
                      type: 'link',
                      icon: <LogoutOutlined />,
                    }}
                    title={'确定要强退吗？'}
                    onConfirm={async () => {
                      await ApiMonitorOnline.forceLogout(record.tokenId);
                      message.success(
                        `用户 ${record.userName} 会话 ${record.tokenId} 已强退`,
                      );
                      actionControl.table.actionRef.current?.reload();
                    }}
                  />
                </div>
              );
            },
          },
        ]}
      />
    </PageContainer>
  );
};

export default MonitorOnlineIndex;
