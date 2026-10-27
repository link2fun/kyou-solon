import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, get, postJSON, putJSON} from '@/utils/request';

/** 新增部门请求字段, 与后端 SysDeptAddReq 对齐 */
const ADD_FIELDS: string[] = [
  'parentId',
  'deptName',
  'orderNum',
  'leader',
  'phone',
  'email',
  'status',
];

/** 修改部门请求字段; ancestors/delFlag 由服务端维护, 客户端不发 */
const EDIT_FIELDS: string[] = ['deptId', ...ADD_FIELDS];

const ApiSystemDept = {
  /**获取部门列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/system/dept/list`, params),
  /** 查询部门列表（排除节点） */
  listExcludeChild: (deptId: number) =>
    get(`${WebConst.API_PREFIX}/system/dept/list/exclude/${deptId}`),
  /** 根据部门编号获取详细信息 */
  detail: (deptId: number) =>
    get(`${WebConst.API_PREFIX}/system/dept/${deptId}`),
  /** 新增部门 */
  add: (createReq: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/dept`,
      ObjectTool.pick(createReq, ADD_FIELDS),
    ),
  /** 修改部门 */
  edit: (updateReq: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/dept`,
      ObjectTool.pick(updateReq, EDIT_FIELDS),
    ),
  /** 删除部门 */
  remove: (deptId: number) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/dept/${deptId}`),
};

export default ApiSystemDept;
