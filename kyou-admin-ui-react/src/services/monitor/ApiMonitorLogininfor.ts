import WebConst from '@/constants/WebConst';
import { deleteJSON, downloadFile, get } from '@/utils/request';

const ApiMonitorLogininfor = {
  /** 查看访问日志列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/monitor/logininfor/list`, params),
  /** 导出登录日志 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/monitor/logininfor/export`,
      params,
      '登录日志.xlsx',
      { method: 'POST' },
    ),
  /** 删除登录日志 */
  remove: (infoIds: string | string[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/monitor/logininfor/${infoIds}`),
  /** 清空登录日志 */
  clean: () => deleteJSON(`${WebConst.API_PREFIX}/monitor/logininfor/clean`),
  /** 账户解锁 */
  unlockUser: (userName: string) =>
    get(`${WebConst.API_PREFIX}/monitor/logininfor/unlock/${userName}`),
};

export default ApiMonitorLogininfor;
