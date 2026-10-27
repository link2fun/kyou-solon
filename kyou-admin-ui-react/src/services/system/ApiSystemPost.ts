import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, downloadFile, get, postJSON, putJSON,} from '@/utils/request';

/** 新增岗位请求字段, 与后端 SysPostAddReq 对齐 */
const ADD_FIELDS: string[] = ['postCode', 'postName', 'postSort', 'status', 'remark'];

/** 修改岗位请求字段 */
const EDIT_FIELDS: string[] = ['postId', ...ADD_FIELDS];

const ApiSystemPost = {
  /** 获取岗位列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/system/post/list`, params),
  /** 获取岗位详情 */
  detail: (postId: number) =>
    get(`${WebConst.API_PREFIX}/system/post/${postId}`),
  /** 添加岗位 */
  add: (data: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/post`,
      ObjectTool.pick(data, ADD_FIELDS),
    ),
  /** 修改岗位 */
  edit: (data: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/post`,
      ObjectTool.pick(data, EDIT_FIELDS),
    ),
  /** 删除岗位 */
  remove: (postId: number) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/post/${postId}`),
  /** 导出岗位 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/post/export`,
      params,
      '岗位信息.xlsx',
      {method: 'POST'},
    ),
  /** 获取岗位选择框列表 */
  optionSelect: () => get(`${WebConst.API_PREFIX}/system/post/optionselect`),
};

export default ApiSystemPost;
