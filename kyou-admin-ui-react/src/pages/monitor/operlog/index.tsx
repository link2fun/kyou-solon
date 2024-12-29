import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import ApiMonitorOperlog from '@/services/monitor/ApiMonitorOperlog';
import { DeleteOutlined, ExportOutlined } from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { App } from 'antd';

const SystemLogOperlogIndex = () => {
  const { message } = App.useApp();

  const actionControl = useActionControl({
    viewAction: {
      onActionCall: Promise.resolve,
    },
    selection: {
      rowKey: 'operId',
    },
  });

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (_params: any) => {
          const { operTime, ...rest } = _params;
          let beginTime = operTime ? operTime[0] : '';
          let endTime = operTime ? operTime[1] : '';
          const params = {
            ...rest,
            'params[beginTime]': beginTime,
            'params[endTime]': endTime,
          };
          const data = await ApiMonitorOperlog.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <PopconfirmButton
            buttonText={'删除'}
            title={'删除选中的操作日志'}
            buttonProps={{
              danger: true,
              icon: <DeleteOutlined />,
              disabled: actionControl.selection.selectNone,
            }}
            onConfirm={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.info('删除中...');
                await ApiMonitorOperlog.remove(
                  actionControl.selection.selectedRowKeys as string[],
                );
                hide();
                message.success('删除成功');
                actionControl.actions.reloadTableData();
                actionControl.selection.clean();
              })
            }
            permissionsRequired={['monitor:operlog:remove']}
            key={'remove'}
          />,
          <PopconfirmButton
            buttonText={'清空'}
            title={'清空操作日志'}
            key={'removeAll'}
            buttonProps={{ danger: true, icon: <DeleteOutlined /> }}
            onConfirm={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.info('清空中...', 120_000);
                await ApiMonitorOperlog.clean();
                hide();
                message.success('清空成功');
                actionControl.actions.reloadTableData();
                actionControl.selection.clean();
              })
            }
            permissionsRequired={['monitor:operlog:remove']}
          />,
          <PermissionButton
            key={'export'}
            permissionsRequired={['monitor:operlog:export']}
            icon={<ExportOutlined />}
            onClick={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.info('导出中, 请稍后...');
                await ApiMonitorOperlog.export({
                  ...actionControl.queryParams.value,
                });
                hide();
              })
            }
          >
            导出
          </PermissionButton>,
        ]}
        columns={[
          { title: '日志编号', dataIndex: 'operId', hideInSearch: true },
          { title: '系统模块', dataIndex: 'title' },
          {
            title: '操作类型',
            dataIndex: 'businessType',
            valueType: 'select',
            request: useProFormSelectDictRequest({ typeCode: 'sys_oper_type' }),
          },
          { title: '操作人员', dataIndex: 'operName' },
          { title: '操作地址', dataIndex: 'operIp' },
          {
            title: '操作状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_common_status',
            }),
          },

          {
            title: '操作日期',
            dataIndex: 'operTime',
            valueType: 'dateTime',
            hideInSearch: true,
          },
          {
            title: '操作日期',
            dataIndex: 'operTime',
            valueType: 'dateRange',
            hideInTable: true,
          },
          {
            title: '操作耗时',
            dataIndex: 'costTime',
            hideInSearch: true,
            renderText: (text: number) => {
              if (text < 500) {
                return <span className={'text-green-600'}>{text} ms</span>;
              } else if (text < 3000) {
                return <span className={'text-orange-500'}>{text} ms</span>;
              }
              return <span className={'text-red-500'}>{text} ms</span>;
            },
          },
        ]}
      />
    </div>
  );
};

export default SystemLogOperlogIndex;
