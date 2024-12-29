type RouteProps = Array<{
  name?: string;
  icon?: string;
  hideInMenu?: boolean;
  component?: string | undefined;
  layout?: false | undefined;
  path?: string | undefined;
  redirect?: string | undefined;
  routes?: RouteProps;
  wrappers?: Array<string> | undefined;
  access?: 'permissionFilter';
  permissionRequired?: string[];
}>;

const routes: RouteProps = [
  {
    path: '/user',
    layout: false,
    routes: [
      {
        name: 'login',
        path: '/user/login',
        component: './User/Login',
      },
      {
        path: '*',
        component: './exception/404',
      },
    ],
  },
  {
    path: '/',
    redirect: '/home',
  },
  {
    name: '首页',
    path: '/home',
    component: './Home',
  },
  {
    name: '个人页',
    icon: 'user',
    path: '/account',
    hideInMenu: true,
    routes: [
      // {
      //   name: '账户绑定',
      //   icon: 'smile',
      //   path: '/account/switch-user',
      //   component: './account/switch-user/AccountSwitchUserIndex',
      // },
      {
        name: '个人中心',
        icon: 'smile',
        path: '/account/center',
        component: './system/user/SystemUserProfile',
      },
      // {
      //   name: '个人设置',
      //   icon: 'smile',
      //   path: '/account/settings',
      //   component: './account/settings/AccountSettingsIndex',
      // },
      {
        component: './exception/404',
      },
    ],
  },

  {
    name: '系统管理',
    path: '/system',
    access: 'permissionFilter',
    permissionRequired: [
      'system:user:list',
      'system:role:list',
      'system:menu:list',
      'system:dept:list',
      'system:post:list',
      'system:dict:list',
      'system:config:list',
      'system:notice:list',
      'system:log:list',
    ],
    routes: [
      {
        name: '用户管理',
        path: '/system/user',
        component: './system/user/SystemUserIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:user:list'],
      },
      {
        name: '分配角色',
        path: '/system/userAuthRole',
        component: './system/user/SystemUserAuthRole',
        permissionRequired: ['system:user:list'],
        hideInMenu: true,
      },
      {
        name: '分配用户',
        path: '/system/roleAuthUser',
        component: './system/role/SystemRoleAuthUser',
        hideInMenu: true,
      },
      {
        name: '个人中心',
        path: '/system/userProfile',
        component: './system/user/SystemUserProfile',
        hideInMenu: true,
      },
      {
        name: '角色管理',
        path: '/system/role',
        component: './system/role/SystemRoleIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:role:list'],
      },
      {
        name: '菜单管理',
        path: '/system/menu',
        component: './system/menu/SystemMenuIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:menu:list'],
      },
      {
        name: '部门管理',
        path: '/system/dept',
        component: './system/dept/SystemDeptIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:dept:list'],
      },
      {
        name: '岗位管理',
        path: '/system/post',
        component: './system/post/SystemPostIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:post:list'],
      },
      {
        name: '字典类型',
        path: '/system/dictType',
        component: './system/dictType/SystemDictTypeIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:dict:list'],
      },
      {
        name: '字典值',
        path: '/system/dictData',
        component: './system/dictData/SystemDictDataIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:dict:list'],
      },
      {
        name: '参数设置',
        path: '/system/config',
        component: './system/config/SystemConfigIndex',
        access: 'permissionFilter',
        permissionRequired: ['system:config:list'],
      },
      {
        name: '通知公告',
        path: '/system/notice',
        component: './system/notice/index',
        access: 'permissionFilter',
        permissionRequired: ['system:notice:list'],
      },
      {
        name: '日志管理',
        path: '/system/log',
        access: 'permissionFilter',
        permissionRequired: ['system:log:list'],
        routes: [
          {
            name: '操作日志',
            path: '/system/log/operlog',
            component: './monitor/operlog/index',
            access: 'permissionFilter',
            permissionRequired: ['system:log:list'],
          },
          {
            name: '登录日志',
            path: '/system/log/logininfor',
            component: './monitor/logininfor/index',
            access: 'permissionFilter',
            permissionRequired: ['system:log:list'],
          },
          {
            path: '*',
            component: './exception/404',
          },
        ],
      },
      {
        path: '*',
        component: './exception/404',
      },
    ],
  },
  {
    name: '系统监控',
    path: '/monitor',
    access: 'permissionFilter',
    permissionRequired: [
      'monitor:online:list',
      'monitor:job:list',
      'monitor:server:list',
      'monitor:cache:list',
    ],
    routes: [
      {
        name: '在线用户',
        path: '/monitor/online',
        component: './monitor/online/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:online:list'],
      },
      {
        name: '定时任务',
        path: '/monitor/job',
        component: './monitor/job/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:job:list'],
      },
      {
        name: '任务日志',
        path: '/monitor/jobLog',
        component: './monitor/jobLog/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:job:list'],
      },
      {
        name: '服务监控',
        path: '/monitor/server',
        component: './monitor/server/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:server:list'],
      },
      {
        name: '缓存监控',
        path: '/monitor/cache',
        component: './monitor/cache/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:cache:list'],
      },
      {
        name: '缓存列表',
        path: '/monitor/cacheList',
        component: './monitor/cacheList/index',
        access: 'permissionFilter',
        permissionRequired: ['monitor:cache:list'],
      },
      {
        path: '*',
        component: './exception/404',
      },
    ],
  },
  {
    name: '系统工具',
    path: '/tool',
    routes: [
      {
        name: '代码生成',
        path: '/tool/gen',
        component: './tool/gen/ToolGenIndex',
      },
      {
        name: '修改生成配置',
        path: '/tool/genEdit',
        component: './tool/gen/ToolGenEditIndex',
        hideInMenu: true,
      },
      {
        path: '*',
        component: './exception/404',
      },
    ],
  },
  {
    path: '/exception',
    name: '异常页',
    icon: 'warning',
    hideInMenu: true,
    routes: [
      {
        name: '无权限',
        icon: 'smile',
        path: '/exception/403',
        component: './exception/403',
      },
      {
        name: '页面不存在',
        icon: 'smile',
        path: '/exception/404',
        component: './exception/404',
      },
      {
        name: '系统异常',
        icon: 'smile',
        path: '/exception/500',
        component: './exception/500',
      },
    ],
  },
  {
    path: '*',
    component: './exception/404',
  },
];

export default routes;
