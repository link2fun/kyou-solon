import WebConst from '@/constants/WebConst';
import {
  deleteJSON,
  downloadFile,
  get,
  postJSON,
  putJSON,
} from '@/utils/request';

const ApiSystemConfig = {
  /** 获取参数配置列表 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/config/list`, params),

  /** 导出参数管理 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/config/export`,
      params,
      '参数配置.xlsx',
      { method: 'POST' },
    ),
  /** 根据参数编号获取详细信息 */
  detail: (configId: number) =>
    get(`${WebConst.API_PREFIX}/system/config/${configId}`),
  /** 根据参数键名查询参数值 */
  getByConfigKey: (configKey: string) =>
    get(`${WebConst.API_PREFIX}/system/config/configKey/${configKey}`),
  /** 新增参数配置 */
  add: (addReq: any) =>
    postJSON(`${WebConst.API_PREFIX}/system/config`, addReq),
  /** 修改参数配置 */
  edit: (editReq: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/config`, editReq),
  /** 删除参数配置 */
  remove: (configIds: number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/config/${configIds}`),
  /** 刷新参数缓存 */
  refreshCache: () =>
    deleteJSON(`${WebConst.API_PREFIX}/system/config/refreshCache`),
};

export default ApiSystemConfig;
