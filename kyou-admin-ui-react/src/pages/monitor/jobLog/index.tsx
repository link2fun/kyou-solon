import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import ApiMonitorJob from '@/services/monitor/ApiMonitorJob';
import ApiMonitorJobLog from '@/services/monitor/ApiMonitorJobLog';
import { DeleteOutlined, ExportOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { message } from 'antd';

const MonitorJobLogIndex = () => {
  const actionControl = useActionControl({
    selection: {
      rowKey: 'jobLogId',
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params: any) => {
          const data = await ApiMonitorJobLog.list(params);

          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <PopconfirmButton
            permissionsRequired={['monitor:job:remove']}
            key={'remove'}
            buttonText={'删除'}
            title={'删除任务日志'}
            buttonProps={{
              danger: true,
              icon: <DeleteOutlined />,
              disabled: actionControl.selection.selectNone,
            }}
            onConfirm={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.loading('删除中...');
                await ApiMonitorJobLog.remove(
                  actionControl.selection.selectedRowKeys as string[],
                );
                hide();
                message.success('删除成功');
                actionControl.actions.reloadTableData();
                actionControl.selection.clean();
              })
            }
          />,
          <PopconfirmButton
            buttonText={'清空'}
            title={'清空任务日志'}
            permissionsRequired={['monitor:job:remove']}
            key={'removeAll'}
            buttonProps={{
              danger: true,
              icon: <DeleteOutlined />,
            }}
            onConfirm={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.loading('清空中...');
                await ApiMonitorJobLog.clean();
                hide();
                message.success('清空成功');
                actionControl.actions.reloadTableData();
                actionControl.selection.clean();
              })
            }
          />,
          <PermissionButton
            key={'export'}
            permissionsRequired={['monitor:job:export']}
            icon={<ExportOutlined />}
            onClick={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.loading('导出中, 请稍后...');
                await ApiMonitorJobLog.export({
                  ...actionControl.queryParams.value,
                });
                message.success('导出成功');
                hide();
              })
            }
          >
            导出
          </PermissionButton>,
        ]}
        rowKey={'jobLogId'}
        columns={[
          { title: '日志编号', dataIndex: 'jobLogId', hideInSearch: true },
          {
            title: '任务名称',
            dataIndex: 'jobName',
            ellipsis: true,
            valueType: 'select',
            request: async () => {
              const data = await ApiMonitorJob.list({});
              const { rows } = data;
              return rows.map((item: any) => ({
                label: item.jobName,
                value: item.jobName,
              }));
            },
          },
          {
            title: '任务组名',
            dataIndex: 'jobGroup',
            valueType: 'select',
            request: useProFormSelectDictRequest({ typeCode: 'sys_job_group' }),
          },
          {
            title: '调用不妙字符串',
            dataIndex: 'invokeTarget',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '日志信息',
            dataIndex: 'jobMessage',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '执行状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_common_status',
            }),
          },
          {
            title: '执行时间',
            dataIndex: 'createTime',
            valueType: 'dateTime',
            hideInSearch: true,
          },
        ]}
      />
    </div>
  );
};

export default MonitorJobLogIndex;
