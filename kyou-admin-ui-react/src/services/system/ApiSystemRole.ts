import WebConst from '@/constants/WebConst';
import {
  deleteJSON,
  downloadFile,
  get,
  postJSON,
  putForm,
  putJSON,
} from '@/utils/request';

const ApiSystemRole = {
  /** 角色列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/system/role/list`, params),
  /** 导出角色 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/role/export`,
      params,
      '角色数据.xlsx',
      { method: 'POST' },
    ),
  /** 根据角色编号获取详细信息 */
  detail: (roleId: string) =>
    get(`${WebConst.API_PREFIX}/system/role/${roleId}`),
  /** 新增角色 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/system/role`, data),
  /** 修改角色 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/system/role`, data),
  /** 修改角色数据权限 */
  dataScope: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/role/dataScope`, data),
  /** 状态修改 */
  changeStatus: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/role/changeStatus`, data),
  /** 删除角色 */
  remove: (roleId: number | number[]) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/role/${roleId}`),

  /** 获取角色选择框列表 */
  optionSelect: () => get(`${WebConst.API_PREFIX}/system/role/optionselect`),

  /** 查询已分配用户角色列表 */
  authUserAllocatedList: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/role/authUser/allocatedList`, params),

  /** 查询未分配用户角色列表 */
  authUserUnallocatedList: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/role/authUser/unallocatedList`, params),

  /** 取消授权用户 */
  authUserCancel: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/role/authUser/cancel`, data),

  /** 批量取消授权用户 */
  authUserCancelAll: (roleId: string, userIds: string[]) =>
    putForm(`${WebConst.API_PREFIX}/system/role/authUser/cancelAll`, {
      roleId,
      userIds,
    }),
  /** 批量选择用户授权 */
  authUserSelectAll: (roleId: string, userIds: string[]) =>
    putForm(`${WebConst.API_PREFIX}/system/role/authUser/selectAll`, {
      roleId,
      userIds,
    }),

  /** 获取对应角色部门树列表 */
  deptTree: (roleId: string) =>
    get(`${WebConst.API_PREFIX}/system/role/deptTree/${roleId}`),
};

export default ApiSystemRole;
