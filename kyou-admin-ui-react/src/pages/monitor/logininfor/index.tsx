import PermissionButton from '@/components/BizButtons/PermissionButton';
import PopconfirmButton from '@/components/BizButtons/PopconfirmButton';
import useActionControl from '@/hooks/useActionControl';
import useProFormSelectDictRequest from '@/hooks/useProFormSelectDictRequest';
import ApiMonitorLogininfor from '@/services/monitor/ApiMonitorLogininfor';
import {
  DeleteFilled,
  ExportOutlined,
  UnlockOutlined,
} from '@ant-design/icons';
import { ProTable } from '@ant-design/pro-components';
import { App } from 'antd';

const SystemLogLogininforIndex = () => {
  const actionControl = useActionControl({
    selection: {
      rowKey: 'infoId',
    },
  });

  const { message } = App.useApp();

  return (
    <div>
      <ProTable
        {...actionControl.table}
        request={async (_params: any) => {
          const { loginTime, ...rest } = _params;
          let beginTime = loginTime ? loginTime[0] : '';
          let endTime = loginTime ? loginTime[1] : '';
          const params = {
            ...rest,
            'params[beginTime]': beginTime,
            'params[endTime]': endTime,
          };

          const data = await ApiMonitorLogininfor.list(params);
          return {
            data: data.rows,
            success: true,
            total: data.total,
          };
        }}
        toolBarRender={() => [
          <PopconfirmButton
            permissionsRequired={['monitor:logininfor:remove']}
            key={'remove'}
            buttonText={'删除'}
            title={'删除选中操作日志?'}
            buttonProps={{
              danger: true,
              icon: <DeleteFilled />,
              disabled: actionControl.selection.selectNone,
            }}
            onConfirm={() =>
              actionControl.loading.wrap(async () => {
                const hide = message.info('删除中...');
                await ApiMonitorLogininfor.remove(
                  actionControl.selection.selectedRowKeys as string[],
                );
                hide();
                message.info('清空成功').then(() => {});
                actionControl.selection.clean();
                return actionControl.actions.reloadTableData();
              })
            }
          ></PopconfirmButton>,
          <PopconfirmButton
            permissionsRequired={['monitor:logininfor:remove']}
            key={'removeAll'}
            buttonText={'清空'}
            buttonProps={{
              danger: true,
              icon: <DeleteFilled />,
              loading: actionControl.loading.value,
            }}
            onConfirm={() =>
              actionControl.loading.wrap({
                action: () =>
                  ApiMonitorLogininfor.clean().then(() => {
                    actionControl.selection.clean();
                    actionControl.actions.reloadTableData();
                  }),
                loadingMessage: '清空中...',
                successMessage: '清空成功',
              })
            }
            title={'清空登录日志'}
          ></PopconfirmButton>,
          <PermissionButton
            permissionsRequired={['monitor:logininfor:unlock']}
            key={'unlock'}
            loading={actionControl.loading.value}
            icon={<UnlockOutlined />}
            disabled={!actionControl.selection.selectSingle}
            onClick={() =>
              actionControl.loading.wrap({
                action: async () => {
                  const selectedRowObjs =
                    actionControl.selection.selectedRowObjs;
                  const selectedObj = selectedRowObjs[0];
                  await ApiMonitorLogininfor.unlockUser(selectedObj.userName);
                  actionControl.selection.clean();
                  return actionControl.actions.reloadTableData();
                },
                loadingMessage: '解锁中...',
                successMessage: '解锁成功',
                failMessage: '解锁失败',
              })
            }
          >
            解锁
          </PermissionButton>,
          <PermissionButton
            permissionsRequired={['monitor:logininfor:export']}
            key={'export'}
            loading={actionControl.loading.value}
            icon={<ExportOutlined />}
            onClick={() =>
              actionControl.loading.wrap({
                action: () =>
                  ApiMonitorLogininfor.export({
                    ...actionControl.queryParams.value,
                  }),
                loadingMessage: '导出中...',
                successMessage: '导出成功',
                failMessage: '导出失败',
              })
            }
          >
            导出
          </PermissionButton>,
        ]}
        columns={[
          { title: '访问编号', dataIndex: 'infoId', hideInSearch: true },
          { title: '用户名称', dataIndex: 'userName' },
          { title: '登录地址', dataIndex: 'ipaddr' },
          { title: '登录地点', dataIndex: 'loginLocation', hideInSearch: true },
          { title: '操作系统', dataIndex: 'os', hideInSearch: true },
          { title: '浏览器', dataIndex: 'browser', hideInSearch: true },
          {
            title: '登录状态',
            dataIndex: 'status',
            valueType: 'select',
            request: useProFormSelectDictRequest({
              typeCode: 'sys_common_status',
            }),
          },
          {
            title: '描述',
            dataIndex: 'msg',
            ellipsis: true,
            hideInSearch: true,
          },
          {
            title: '访问时间',
            dataIndex: 'loginTime',
            valueType: 'dateTime',
            hideInSearch: true,
          },
          {
            title: '访问时间',
            dataIndex: 'loginTime',
            valueType: 'dateRange',
            hideInTable: true,
          },
        ]}
      />
    </div>
  );
};

export default SystemLogLogininforIndex;
