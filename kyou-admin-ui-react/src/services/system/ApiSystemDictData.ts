import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, downloadFile, get, postJSON, putJSON,} from '@/utils/request';

/** 新增字典值请求字段, 与后端 SysDictDataAddReq 对齐 */
const ADD_FIELDS: string[] = [
  'dictSort',
  'dictLabel',
  'dictValue',
  'dictType',
  'cssClass',
  'listClass',
  'isDefault',
  'status',
  'remark',
];

/** 修改字典值请求字段 */
const EDIT_FIELDS: string[] = ['dictCode', ...ADD_FIELDS];

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
      {method: 'POST'},
    ),

  /** 查询字典值 */
  detail: (dictCode: number) =>
    get(`${WebConst.API_PREFIX}/system/dict/data/${dictCode}`),

  /** 根据字典类型查询字典数据 */
  listByType: (dictType: string) =>
    get(`${WebConst.API_PREFIX}/system/dict/data/type/${dictType}`),

  /** 新增字典值 */
  add: (data: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/dict/data`,
      ObjectTool.pick(data, ADD_FIELDS),
    ),

  /** 修改字典值 */
  edit: (data: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/dict/data`,
      ObjectTool.pick(data, EDIT_FIELDS),
    ),

  /** 删除字典值 */
  remove: (dictCode: number | number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/dict/data/${dictCode}`),
};

export default ApiSystemDictData;
