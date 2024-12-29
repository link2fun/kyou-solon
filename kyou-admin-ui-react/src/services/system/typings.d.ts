interface SysUserDTO {
  /** 用户编号 */
  userId: string;
  /** 部门编号 */
  deptId: string;
  /** 部门名称 */
  deptName: string;
  /** 用户账号 */
  userName: string;
  /** 用户昵称 */
  nickName: string;
  /** 用户邮箱 */
  email: string;
  /** 手机号码 */
  phonenumber: string;
  /** 性别 */
  sex: string;
  /** 用户头像 */
  avatar: string;
  /** 帐号状态 */
  status: string;
  /** 最后登录IP */
  loginIp: string;
  /** 最后登录时间 */
  loginDate: string;

  roles?: SysRoleDTO[];
}

interface DeptDTO {
  /** 部门ID */
  deptId: number;
  /** 部门名称 */
  deptName: string;
  /** 上级ID */
  parentId: string;
  /** 祖级列表 */
  ancestors: string;
  /** 排序序号 */
  orderNum: number;
  /** 部门领导人 */
  leader: string;
  /** 状态 */
  status: string;
}

interface CurrentUser {
  /** 用户信息 */
  user: Partial<SysUserDTO>;
  /** 角色列表 */
  roles: string[];
  /** 权限列表 */
  permissions: string[];
}

interface AjaxResult<T> {
  code: number;
  msg: string;
  data: T;
  success: boolean;
  warn: boolean;
  error: boolean;
}

type ExtendAjaxResult<T> = AjaxResult<T> & any;

interface SysMenu {
  /** 菜单ID */
  menuId: number;
  /** 菜单名称 */
  menuName: string;
  /** 父菜单名称 */
  parentName: string;
  /** 父菜单ID */
  parentId: number;
  /** 显示顺序 */
  orderNum: number;

  /** 路由地址 */
  path: string;
  //
  /** 组件路径 */
  component: string;

  /** 路由参数 */
  query: string;

  /** 是否为外链（0是 1否） */
  isFrame: string;

  /** 是否缓存（0缓存 1不缓存） */
  isCache: string;
  //
  /** 类型（M目录 C菜单 F按钮） */
  menuType: string;

  /** 显示状态（0显示 1隐藏） */
  visible: string;

  /** 菜单状态（0正常 1停用） */
  status: string;
  /** 权限字符串 */
  perms: string;
  /** 菜单图标 */
  icon: string;
  /** 备注 */
  remark: string;
  /** 子菜单 */
  children?: SysMenu[];
}

interface SysRoleDTO {
  /** 角色ID */
  roleId: string;
  /** 角色名称 */
  roleName: string;
  /** 角色权限 */
  roleKey: string;
  /** 角色排序 */
  roleSort: number;
  /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
  dataScope: string;
  /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
  menuCheckStrictly: number;
  /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
  deptCheckStrictly: number;
  /** 角色状态（0正常 1停用） */
  status: string;
  /** 删除标志（0代表存在 2代表删除） */
  delFlag: string;
  /** 备注 */
  remark: string;
  /** 用户是否存在此角色标识 默认不存在 */
  flag: boolean;
  /** 菜单组 */
  menuIds: string[];
}
