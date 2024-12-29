import WebConst from '@/constants/WebConst';
import {
  deleteJSON,
  downloadFile,
  get,
  postForm,
  putJSON,
} from '@/utils/request';

const ApiToolGen = {
  /** 查询代码生成列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/tool/gen/list`, params),

  /** 查询详情 */
  detail: (tableId: string) =>
    get(`${WebConst.API_PREFIX}/tool/gen/${tableId}`),

  /** 查询数据库列表 */
  dbList: (params: any) =>
    get(`${WebConst.API_PREFIX}/tool/gen/db/list`, params),

  /** 查询数据表字段列表 */
  columnList: (tableId: string) =>
    get(`${WebConst.API_PREFIX}/tool/gen/column/${tableId}`),

  /** 导入表结构（保存）*/
  importTable: (tables: string) =>
    postForm(`${WebConst.API_PREFIX}/tool/gen/importTable`, { tables }),

  /** 修改保存代码生成业务*/
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/tool/gen`, data),

  /** 删除代码生成 */
  remove: (tableIds: string[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/tool/gen/${tableIds}`),

  /** 预览代码 */
  preview: (tableId: string) =>
    get(`${WebConst.API_PREFIX}/tool/gen/preview/${tableId}`),

  /** 生成代码（下载方式）*/
  download: (tableName: string) =>
    downloadFile(
      `${WebConst.API_PREFIX}/tool/gen/download/${tableName}`,
      {},
      `${tableName}.zip`,
      { method: 'GET' },
    ),

  /** 生成代码（自定义路径）*/
  genCode: (tableName: string) =>
    get(`${WebConst.API_PREFIX}/tool/gen/genCode/${tableName}`),

  /** 同步数据库 */
  synchDb: (tableName: string) =>
    get(`${WebConst.API_PREFIX}/tool/gen/synchDb/${tableName}`),

  /** 批量生成代码 */
  batchGenCode: (tableNames: string[]) =>
    postForm(`${WebConst.API_PREFIX}/tool/gen/batchGenCode`, { tableNames }),
};

export default ApiToolGen;
