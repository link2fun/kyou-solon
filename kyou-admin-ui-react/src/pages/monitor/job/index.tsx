import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import TableRowDelButton from '@/components/BizButtons/TableRowDelButton';
import TableRowEditButton from '@/components/BizButtons/TableRowEditButton';
import FormSwitch from '@/components/ProFormItem/FormSwitch';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import MonitorJobEditModal from '@/pages/monitor/job/components/MonitorJobEditModal';
import ApiMonitorJob from '@/services/monitor/ApiMonitorJob';
import { history, useModel } from '@@/exports';
import {
  EyeOutlined,
  HistoryOutlined,
  PlayCircleOutlined,
} from '@ant-design/icons';
import { ProFormSelect, ProTable } from '@ant-design/pro-components';
import { message, Tooltip } from 'antd';
import ButtonGroup from 'antd/es/button/button-group';

const MonitorJobIndex = () => {
  const { updateTab } = useModel('global');

  const actionControl = useActionControl({
    addAction: {
      onActionCall: (data) => ApiMonitorJob.add(data),
    },
    editAction: {
      onModalOpen: (data) => ApiMonitorJob.detail(data.jobId),
      onActionCall: (data) => ApiMonitorJob.edit(data),
    },
    removeAction: {
      onActionCall: (data) => ApiMonitorJob.remove(data.jobId),
    },
  });

  const sysJobStatusSelectRequest = useProFormSelectDictRequest({
    typeCode: 'sys_job_status',
  });

  const navToJobLog = (record: { jobName: any }) => {
    updateTab({
      key: '/monitor/jobLog',
      pathname: '/monitor/jobLog',
      search: '',
      queryParams: { jobName: record.jobName },
      content: '',
      closable: true,
      title: '任务日志',
    });
    history.push('/monitor/jobLog');
  };

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (params: any) => {
          const data = await ApiMonitorJob.list(params);

          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        rowKey={'jobId'}
        toolBarRender={() => {
          return [
            <ButtonGroup key={'operations'}>
              <PermissionButton
                permissionsRequired={['monitor:job:add']}
                loading={actionControl.loading.value}
                onClick={() => actionControl.actions.openAddModal({})}
              >
                新增
              </PermissionButton>
            </ButtonGroup>,
          ];
        }}
        columns={[
          {
            title: '任务编号',
            dataIndex: 'jobId',
            hideInSearch: true,
          },
          {
            title: '任务名称',
            dataIndex: 'jobName',
            ellipsis: true,
          },
          {
            title: '任务组名',
            dataIndex: 'jobGroup',
            valueType: 'select',
            request: useProFormSelectDictRequest({ typeCode: 'sys_job_group' }),
          },
          {
            title: '调用目标字符串',
            dataIndex: 'invokeTarget',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: 'cron表达式',
            dataIndex: 'cronExpression',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '状态',
            dataIndex: 'status',
            renderText: (_, record) => {
              return (
                <Tooltip title={'点击切换状态'}>
                  <FormSwitch
                    value={record.status}
                    loading={actionControl.loading.value}
                    activeValue={'0'}
                    inactiveValue={'1'}
                    onChange={async (value) => {
                      actionControl.loading.begin();
                      try {
                        await ApiMonitorJob.changeStatus({
                          ...record,
                          status: value,
                        });
                        message.success('操作成功');
                      } catch (e) {
                      } finally {
                        actionControl.loading.end();
                      }
                      actionControl.actions.reloadTableData();
                    }}
                  />
                </Tooltip>
              );
            },
            renderFormItem: () => (
              <ProFormSelect request={sysJobStatusSelectRequest} />
            ),
          },
          {
            title: '操作',
            dataIndex: 'operations',
            hideInSearch: true,
            fixed: 'right',
            width: actionControl.rowAction.width,
            render: (_, record) => {
              return (
                <div ref={actionControl.rowAction.ref}>
                  <TableRowEditButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['monitor:job:edit']}
                  />
                  <TableRowDelButton
                    actionControl={actionControl}
                    record={record}
                    permissionsRequired={['monitor:job:remove']}
                  />
                  <PopconfirmButton
                    permissionsRequired={['monitor:job:changeStatus']}
                    tooltip={'运行一次'}
                    buttonText={''}
                    title={'确认运行?'}
                    buttonProps={{
                      icon: <PlayCircleOutlined />,
                      type: 'link',
                      loading: actionControl.loading.value,
                    }}
                    onConfirm={async () => {
                      message.info('执行结果请稍后查看任务日志');
                      ApiMonitorJob.run(record);
                      return;
                    }}
                  />
                  <PermissionButton
                    permissionsRequired={['monitor:job:query']}
                    icon={<EyeOutlined />}
                    type={'link'}
                    onClick={() => actionControl.actions.openViewModal(record)}
                  />
                  <PermissionButton
                    permissionsRequired={['monitor:job:query']}
                    type={'link'}
                    icon={<HistoryOutlined />}
                    onClick={() => navToJobLog(record)}
                    title={'查看历史日志'}
                  ></PermissionButton>
                </div>
              );
            },
          },
        ]}
      />
      <MonitorJobEditModal
        {...actionControl.editModal}
        open={actionControl.editModal.action !== undefined}
      />
    </div>
  );
};

export default MonitorJobIndex;
