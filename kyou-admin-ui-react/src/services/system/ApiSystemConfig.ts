import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, downloadFile, get, postJSON, putJSON,} from '@/utils/request';

/** 新增参数配置请求字段, 与后端 SysConfigAddReq 对齐; configType 由服务端决定, 不接受客户端指定 */
const ADD_FIELDS: string[] = [
  'configName',
  'configKey',
  'configValue',
  'remark',
];

/** 修改参数配置请求字段 */
const EDIT_FIELDS: string[] = ['configId', ...ADD_FIELDS];

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
      {method: 'POST'},
    ),
  /** 根据参数编号获取详细信息 */
  detail: (configId: number) =>
    get(`${WebConst.API_PREFIX}/system/config/${configId}`),
  /** 根据参数键名查询参数值 */
  getByConfigKey: (configKey: string) =>
    get(`${WebConst.API_PREFIX}/system/config/configKey/${configKey}`),
  /** 新增参数配置 */
  add: (addReq: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/config`,
      ObjectTool.pick(addReq, ADD_FIELDS),
    ),
  /** 修改参数配置 */
  edit: (editReq: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/config`,
      ObjectTool.pick(editReq, EDIT_FIELDS),
    ),
  /** 删除参数配置 */
  remove: (configIds: number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/config/${configIds}`),
  /** 刷新参数缓存 */
  refreshCache: () =>
    deleteJSON(`${WebConst.API_PREFIX}/system/config/refreshCache`),
};

export default ApiSystemConfig;
