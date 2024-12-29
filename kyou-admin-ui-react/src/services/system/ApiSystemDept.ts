import WebConst from '@/constants/WebConst';
import { deleteJSON, get, postJSON, putJSON } from '@/utils/request';

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
    postJSON(`${WebConst.API_PREFIX}/system/dept`, createReq),
  /** 修改部门 */
  edit: (updateReq: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/dept`, updateReq),
  /** 删除部门 */
  remove: (deptId: number) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/dept/${deptId}`),
};

export default ApiSystemDept;
