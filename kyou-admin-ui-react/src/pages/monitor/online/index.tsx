import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useActionControl from '@/hooks/useActionControl';
import ApiMonitorOnline from '@/services/monitor/ApiMonitorOnline';
import { LogoutOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { message } from 'antd';

const MonitorOnlineIndex = () => {
  const actionControl = useActionControl({});

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params: any) => {
          let data = await ApiMonitorOnline.list(params);
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
            hideInSearch: true,
          },
          { title: '登录名称', dataIndex: 'userName' },
          { title: '所属部门', dataIndex: 'deptName', hideInSearch: true },
          { title: '主机', dataIndex: 'ipaddr' },
          { title: '登录地点', dataIndex: 'loginLocation', hideInSearch: true },
          {
            title: '操作系统',
            dataIndex: 'os',
            ellipsis: true,
            hideInSearch: true,
          },
          { title: '浏览器', dataIndex: 'browser', hideInSearch: true },
          {
            title: '登录时间',
            dataIndex: 'loginTime',
            valueType: 'dateTime',
            hideInSearch: true,
          },
          {
            title: '操作',
            hideInSearch: true,
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
    </div>
  );
};

export default MonitorOnlineIndex;
