import WebConst from '@/constants/WebConst';
import {
  deleteJSON,
  downloadFile,
  get,
  postJSON,
  putJSON,
} from '@/utils/request';

const ApiMonitorJob = {
  /** 查询定时任务列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/monitor/job/list`, params),
  /** 导出定时任务列表 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/monitor/job/export`,
      params,
      '定时任务列表.xlsx',
      { method: 'POST' },
    ),
  /** 获取定时任务详情 */
  detail: (jobId: number) => get(`${WebConst.API_PREFIX}/monitor/job/${jobId}`),
  /** 新增定时任务 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/monitor/job/add`, data),
  /** 修改定时任务 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/monitor/job`, data),
  /** 删除定时任务 */
  remove: (jobId: number | number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/job/${jobId}`),
  /** 修改定时任务执行状态 */
  changeStatus: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/monitor/job/changeStatus`, data),
  /** 执行定时任务 */
  run: (data: any) => putJSON(`${WebConst.API_PREFIX}/monitor/job/run`, data),
};

export default ApiMonitorJob;
