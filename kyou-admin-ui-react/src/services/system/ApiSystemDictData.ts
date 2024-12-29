import WebConst from '@/constants/WebConst';
import {
  deleteJSON,
  downloadFile,
  get,
  postJSON,
  putJSON,
} from '@/utils/request';

const ApiSystemDictData = {
  /** 分页查询字典值 */
  list: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/dict/data/list`, params),
  /** 导出字典值 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/dict/data/export`,
      params,
      '字典数据.xlsx',
      { method: 'POST' },
    ),

  /** 查询字典值 */
  detail: (dictCode: number) =>
    get(`${WebConst.API_PREFIX}/system/dict/data/${dictCode}`),

  /** 根据字典类型查询字典数据 */
  listByType: (dictType: string) =>
    get(`${WebConst.API_PREFIX}/system/dict/data/type/${dictType}`),

  /** 新增字典值 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/system/dict/data`, data),

  /** 修改字典值 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/system/dict/data`, data),

  /** 删除字典值 */
  remove: (dictCode: number | number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/dict/data/${dictCode}`),
};

export default ApiSystemDictData;
