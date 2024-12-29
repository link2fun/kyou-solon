import WebConst from '@/constants/WebConst';
import { deleteJSON, downloadFile, get } from '@/utils/request';

const ApiMonitorOperlog = {
  /** 查看日志列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/monitor/operlog/list`, params),
  /** 导出日志 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/monitor/operlog/export`,
      params,
      '操作日志.xlsx',
      { method: 'POST' },
    ),
  /** 删除日志 */
  remove: (operIds: string | string[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/operlog/${operIds}`),

  /** 清空日志 */
  clean: () => deleteJSON(`${WebConst.API_PREFIX}/monitor/operlog/clean`),
};

export default ApiMonitorOperlog;
