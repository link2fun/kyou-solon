import WebConst from '@/constants/WebConst';
import { deleteJSON, downloadFile, get } from '@/utils/request';

const ApiMonitorJobLog = {
  /** 定时任务日志列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/monitor/jobLog/list`, params),
  /** 导出定时任务日志 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/monitor/jobLog/export`,
      params,
      '定时任务日志.xlsx',
      { method: 'POST' },
    ),
  /** 获取定时任务日志详情 */
  detail: (jobLogId: string) =>
    get(`${WebConst.API_PREFIX}/monitor/jobLog/${jobLogId}`),
  /** 删除定时任务日志 */
  remove: (jobLogId: string | string[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/jobLog/${jobLogId}`),
  /** 清空定时任务日志 */
  clean: () => deleteJSON(`${WebConst.API_PREFIX}/monitor/jobLog/clean`),
};

export default ApiMonitorJobLog;
