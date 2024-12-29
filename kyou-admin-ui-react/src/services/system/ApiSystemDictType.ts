import WebConst from '@/constants/WebConst';
import { deleteJSON, downloadFile, get, postJSON, putJSON } from '@/utils/request';

const ApiSystemDictType = {
  /** 分页查询字典类型 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/system/dict/type/list`, params),
  /** 导出字典类型 */
  export: (params: any) =>
    downloadFile(`${WebConst.API_PREFIX}/system/dict/type/export`, params, '字典类型.xlsx', { method: 'POST' }),
  /** 查询字典类型详细 */
  detail: (dictId: number) => get(`${WebConst.API_PREFIX}/system/dict/type/${dictId}`),
  /** 新增字典类型 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/system/dict/type`, data),
  /** 修改字典类型 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/system/dict/type`, data),
  /** 删除字典类型 */
  remove: (dictId: number | number[]) => deleteJSON(`${WebConst.API_PREFIX}/system/dict/type/${dictId}`),
  /** 刷新字典缓存 */
  refreshCache: () => deleteJSON(`${WebConst.API_PREFIX}/system/dict/type/refreshCache`),
  /** 获取字典选择框列表 */
  optionSelect: () => get(`${WebConst.API_PREFIX}/system/dict/type/optionselect`),
  /** 获取字典选择框列表 ProSelectRequest */
  optionSelectProSelectRequest: async () => {
    const data = await ApiSystemDictType.optionSelect();
    return (data || []).map((item: any) => {
      return {
        label: item.dictName,
        value: item.dictType,
      };
    });
  },
};

export default ApiSystemDictType;
