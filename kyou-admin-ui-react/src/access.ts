import { InitialState } from '@/typing';
import CollectionTool from '@/utils/CollectionTool';

export default (initialState: InitialState) => {
  const { currentUser } = initialState || {};
  const permissionMenuList = (currentUser || {}).permissions || [];
  const permissionRoleList = (currentUser || {}).roles || [];

  // 在这里按照初始化数据定义项目中的权限，统一管理
  // 参考文档 https://umijs.org/docs/max/access

  return {
    /**
     * 菜单按钮权限检查
     * @param routeItem 路由配置 读取的是 routes.ts 的配置项
     */
    permissionMenu: (routeItem: any) => {
      // 从路由配置中获取菜单权限
      const { authority } = routeItem;

      // 如果当前用户没有任何菜单权限，则不显示菜单
      if (CollectionTool.isEmpty(permissionMenuList)) {
        return false;
      }
      // 如果当前菜单没有配置权限, 则默认允许访问
      if (CollectionTool.isEmpty(authority || [])) {
        // 没有配置权限，默认允许访问
        return true;
      }
      // 看看是否有权限, permissionMenuList 是当前用户的菜单权限, 如果任一权限在 authority 中，则允许访问
      return CollectionTool.containsAny(
        permissionMenuList || [],
        authority || [],
      );
    },
    /**
     * 包含任意角色时，允许访问
     * @param rolesRequired 角色编码 满足任意一个即认为有权限
     */
    permissionRoleContainsAny: (rolesRequired: string[]) => {
      if (CollectionTool.isEmpty(rolesRequired)) {
        // 不需要任何角色 => 任意角色可访问
        return true;
      }
      if (CollectionTool.isEmpty(permissionRoleList)) {
        // 当前用户没有任何角色 => 不允许访问
        return false;
      }
      if (CollectionTool.containsAny(permissionRoleList, rolesRequired)) {
        // 当前用户有任何一个角色在 rolesRequired 中 => 允许访问
        return true;
      }
      return false;
    },
    /**
     * 进行角色权限检查<br/>
     * 如果 permissionRoleList 出现在 rolesRequired 中，则允许访问<br/>
     * 如果 permissionRoleList 出现在 rolesForbidden 中，则不允许访问<br/>
     * @param rolesRequired
     * @param rolesForbidden
     */
    permissionRoleFilter: (
      rolesRequired: string[],
      rolesForbidden: string[],
    ) => {
      // 看看是否有禁止访问的角色
      if (CollectionTool.isNotEmpty(rolesForbidden)) {
        // 如果有禁止访问的角色，则检查是否有禁止访问的角色
        return !CollectionTool.containsAny(permissionRoleList, rolesForbidden);
      }
      if (CollectionTool.isEmpty(rolesRequired)) {
        // 不需要任何角色 => 任意角色可访问
        return true;
      }
      if (CollectionTool.isEmpty(permissionRoleList)) {
        // 当前用户没有任何角色 => 不允许访问
        return false;
      }
      if (CollectionTool.containsAny(permissionRoleList, rolesRequired)) {
        // 当前用户有任何一个角色在 rolesRequired 中 => 允许访问
        return true;
      }
      return false;
    },
    permissionFilter: ({
      permissionRequired,
    }: {
      permissionRequired: string[];
    }) => {
      if (permissionRequired === undefined) {
        return true;
      }
      if (CollectionTool.isEmpty(permissionRequired)) {
        // 不需要任何权限 => 任意权限可访问
        return true;
      }
      if (CollectionTool.isEmpty(permissionMenuList)) {
        // 当前用户没有任何权限 => 不允许访问
        return false;
      }
      //
      if (CollectionTool.containsAny(permissionMenuList, permissionRequired)) {
        // 当前用户有任何一个权限在 permissionsRequired 中 => 允许访问
        return true;
      }

      if (CollectionTool.containsAny(permissionMenuList, ['*:*:*'])) {
        // 如果有任意权限，则允许访问
        return true;
      }
      return false;
    },
    /** 判断当前登录用户是否有指定的权限
     * permissionsRequired 权限列表, 满足任意一个即认为有权限
     * */
    hasPermission: (permissionsRequired: string[] | undefined) => {
      if (permissionsRequired === undefined) {
        return true;
      }
      if (CollectionTool.isEmpty(permissionsRequired)) {
        // 不需要任何权限 => 任意权限可访问
        return true;
      }
      if (CollectionTool.isEmpty(permissionMenuList)) {
        // 当前用户没有任何权限 => 不允许访问
        return false;
      }
      //
      if (CollectionTool.containsAny(permissionMenuList, permissionsRequired)) {
        // 当前用户有任何一个权限在 permissionsRequired 中 => 允许访问
        return true;
      }

      if (CollectionTool.containsAny(permissionMenuList, ['*:*:*'])) {
        // 如果有任意权限，则允许访问
        return true;
      }
      return false;
    },
  };
};
