import WebConst from '@/constants/WebConst';
import { ErrorShowType } from '@/requestConfig';
import {
  downloadFile,
  get,
  postFile,
  postJSON,
  putForm,
  putJSON,
} from '@/utils/request';

const ApiSystemUser = {
  /** 获取用户列表 */
  list: (searchReq: any) =>
    get(`${WebConst.API_PREFIX}/system/user/list`, searchReq),
  /** 导出用户 */
  export: (params: any) =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/user/export`,
      params,
      '用户列表.xlsx',
      { method: 'POST' },
    ),
  /** 导入用户 */
  import: (file: File, updateSupport: boolean) => {
    const formData = new FormData();
    formData.append('updateSupport', updateSupport + '');
    formData.append('file', file);

    return postFile(`${WebConst.API_PREFIX}/system/user/importData`, formData, {
      showType: ErrorShowType.SILENT,
    });
  },

  /** 下载导入模板 */
  importTemplate: () =>
    downloadFile(
      `${WebConst.API_PREFIX}/system/user/importTemplate`,
      {},
      '用户导入模板.xlsx',
      { method: 'POST' },
    ),
  /** 根据用户编号获取详细信息 */
  detail: (userId: number) =>
    get(`${WebConst.API_PREFIX}/system/user/${userId}`),
  /** 添加用户 */
  add: (data: any) => postJSON(`${WebConst.API_PREFIX}/system/user`, data),

  /** 修改用户 */
  edit: (data: any) => putJSON(`${WebConst.API_PREFIX}/system/user`, data),
  /** 删除用户 */
  remove: (userId: number | number[]) =>
    get(`${WebConst.API_PREFIX}/system/user/${userId}`),
  /** 重置密码 */
  resetPwd: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/user/resetPwd`, data),
  /** 修改用户状态 */
  changeStatus: (data: any) =>
    putJSON(`${WebConst.API_PREFIX}/system/user/changeStatus`, data),
  /** 根据用户编号获取授权角色 */
  getAuthRole: (userId: string) =>
    get(`${WebConst.API_PREFIX}/system/user/authRole/${userId}`),
  /** 用户授权角色 */
  authRole: async (userId: string, roleIds: string[]) => {
    const formData = new FormData();
    formData.append('userId', userId + '');
    (roleIds || []).forEach((roleId) => {
      formData.append('roleIds', roleId);
    });
    return putForm(`${WebConst.API_PREFIX}/system/user/authRole`, formData);
  },

  /** 获取部门树列表 */
  deptTree: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/user/deptTree`, params),
};

export default ApiSystemUser;
