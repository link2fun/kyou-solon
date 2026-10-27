import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, downloadFile, get, postJSON, putJSON,} from '@/utils/request';

/** 新增定时任务请求字段, 与后端 SysJobAddReq 对齐 */
const ADD_FIELDS: string[] = [
  'jobName',
  'jobGroup',
  'invokeTarget',
  'cronExpression',
  'misfirePolicy',
  'concurrent',
  'status',
  'remark',
];

/** 修改定时任务请求字段 */
const EDIT_FIELDS: string[] = ['jobId', ...ADD_FIELDS];

const ApiMonitorJob = {
  /** 查询定时任务列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/monitor/job/list`, params),
  /** 导出定时任务列表 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/monitor/job/export`,
      params,
      '定时任务列表.xlsx',
      {method: 'POST'},
    ),
  /** 获取定时任务详情 */
  detail: (jobId: number) => get(`${WebConst.API_PREFIX}/monitor/job/${jobId}`),
  /** 新增定时任务 */
  add: (data: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/monitor/job/add`,
      ObjectTool.pick(data, ADD_FIELDS),
    ),
  /** 修改定时任务 */
  edit: (data: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/monitor/job`,
      ObjectTool.pick(data, EDIT_FIELDS),
    ),
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
