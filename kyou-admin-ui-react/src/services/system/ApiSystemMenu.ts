import WebConst from '@/constants/WebConst';
import ObjectTool from '@/utils/ObjectTool';
import {deleteJSON, get, postJSON, putJSON} from '@/utils/request';

/** 新增菜单请求字段, 与后端 SysMenuAddReq 对齐 */
const ADD_FIELDS: string[] = [
  'menuName',
  'parentId',
  'orderNum',
  'path',
  'component',
  'query',
  'isFrame',
  'isCache',
  'menuType',
  'visible',
  'status',
  'perms',
  'icon',
  'remark',
];

/** 修改菜单请求字段 */
const EDIT_FIELDS: string[] = ['menuId', ...ADD_FIELDS];

const ApiSystemMenu = {
  /** 获取菜单列表 */
  list: (params: any) => get(`${WebConst.API_PREFIX}/system/menu/list`, params),
  /**根据菜单编号获取详细信息 */
  getInfo: (menuId: string) =>
    get(`${WebConst.API_PREFIX}/system/menu/${menuId}`),
  /** 获取菜单下拉树列表 */
  treeSelect: (params: any) =>
    get(`${WebConst.API_PREFIX}/system/menu/treeselect`, params),
  /** 加载对应角色菜单列表树 */
  roleMenuTreeSelect: (roleId: string) =>
    get(`${WebConst.API_PREFIX}/system/menu/roleMenuTreeselect/${roleId}`),
  /** 新增菜单 */
  add: (data: any) =>
    postJSON(
      `${WebConst.API_PREFIX}/system/menu`,
      ObjectTool.pick(data, ADD_FIELDS),
    ),
  /** 修改菜单 */
  edit: (data: any) =>
    putJSON(
      `${WebConst.API_PREFIX}/system/menu`,
      ObjectTool.pick(data, EDIT_FIELDS),
    ),
  /**删除菜单 */
  remove: (menuId: string) =>
    deleteJSON(`${WebConst.API_PREFIX}/system/menu/${menuId}`),
};

export default ApiSystemMenu;
