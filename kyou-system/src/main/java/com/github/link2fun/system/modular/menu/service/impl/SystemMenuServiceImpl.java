package com.github.link2fun.system.modular.menu.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysMenuProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleProxy;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.domain.vo.MetaVo;
import com.github.link2fun.system.domain.vo.RouterVo;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.system.modular.menu.util.MenuUtils;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.system.modular.rolemenu.service.ISystemRoleMenuService;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Tran;
import org.noear.solon.data.tran.TranPolicy;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SystemMenuServiceImpl implements ISystemMenuService {

  @Inject
  private ISystemMenuService self;

  @Inject
  private ISystemRoleMenuService roleMenuService;

  @Inject
  private ISystemRoleService roleService;

  @Db
  private EasyEntityQuery entityQuery;


  /**
   * 根据用户查询系统菜单列表
   *
   * @param userId 用户ID
   * @return 菜单列表
   */
  @Override
  public List<SysMenu> selectMenuList(final Long userId) {
    return self.selectMenuList(new SysMenu(), userId);
  }

  /**
   * 根据用户查询系统菜单列表
   *
   * @param searchReq 菜单信息
   * @param userId    用户ID
   * @return 菜单列表
   */
  @Override
  public List<SysMenu> selectMenuList(final SysMenu searchReq, final Long userId) {
    if (SysUser.isAdmin(userId)) {
      // 如果是管理员，返回所有菜单
      return entityQuery.queryable(SysMenu.class)
        .where(menu -> {
          menu.menuName().like(StrUtil.isNotBlank(searchReq.getMenuName()), searchReq.getMenuName());
          menu.visible().eq(StrUtil.isNotBlank(searchReq.getVisible()), searchReq.getVisible());
          menu.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        })
        .toList();
    }


    searchReq.getParams().put("userId", userId);

    return entityQuery.queryable(SysMenu.class).asAlias("menu")
      .where(menu -> {
        menu.menuName().like(StrUtil.isNotBlank(searchReq.getMenuName()), searchReq.getMenuName());
        menu.visible().eq(StrUtil.isNotBlank(searchReq.getVisible()), searchReq.getVisible());
        menu.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
      })
      .where(menu -> {
        if (IdUtils.isIdValid(userId)) {
          menu.expression().exists(() -> entityQuery.queryable(SysRoleMenu.class).asAlias("roleMenu")
            .leftJoin(SysUserRole.class, (roleMenu, userRole) -> roleMenu.roleId().eq(userRole.roleId()))
            .where((roleMenu, userRole) -> {
              userRole.userId().eq(userId);
              roleMenu.menuId().eq(menu.menuId());
            }));

        }

      })
      .select(SysMenu.class)
      .toList();

  }

  /**
   * 根据用户ID查询权限
   *
   * @param userId 用户ID
   * @return 权限列表
   */
  @Override
  public List<String> selectMenuPermsByUserId(final Long userId) {

    List<String> permsList = entityQuery.queryable(SysMenu.class)
      .leftJoin(SysRoleMenu.class, (menu, roleMenu) -> menu.menuId().eq(roleMenu.menuId()))
      .leftJoin(SysUserRole.class, (menu, roleMenu, userRole) -> roleMenu.roleId().eq(userRole.roleId()))
      .leftJoin(SysRole.class, (menu, roleMenu, userRole, role) -> userRole.roleId().eq(role.roleId()))
      .where((menu, roleMenu, userRole, role) -> {
        userRole.userId().eq(userId);
        menu.status().eq(UserConstants.NORMAL);
        role.status().eq(UserConstants.NORMAL);
      })
      .selectColumn((menu, roleMenu, userRole, role) -> menu.perms())
      .distinct()
      .toList();

    return permsList.stream()
      .filter(StrUtil::isNotBlank)
      .flatMap(_perms -> StrUtil.split(_perms, StrUtil.COMMA).stream())
      .filter(StrUtil::isNotBlank)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   * 根据角色ID查询权限
   *
   * @param roleId 角色ID
   * @return 权限列表
   */
  @Override
  public List<String> selectMenuPermsByRoleId(final Long roleId) {

    List<String> permsList = entityQuery.queryable(SysMenu.class)
      .leftJoin(SysRoleMenu.class, (menu, roleMenu) -> menu.menuId().eq(roleMenu.menuId()))
      .where((menu, roleMenu) -> {
        roleMenu.roleId().eq(roleId);
        menu.status().eq(UserConstants.NORMAL);
      })
      .selectColumn((menu, roleMenu) -> menu.perms())
      .distinct()
      .toList();

    return permsList.stream()
      .filter(StrUtil::isNotBlank)
      .flatMap(_perms -> StrUtil.split(_perms, StrUtil.COMMA).stream())
      .filter(StrUtil::isNotBlank)
      .distinct()
      .collect(Collectors.toList());
  }

  /** 根据角色Key查询权限 */
  @Override
  public List<String> selectMenuPermsByRoleKey(final String roleKey) {

    final SysRole role = roleService.selectRoleByRoleKey(roleKey);
    return self.selectMenuPermsByRoleId(Optional.ofNullable(role).map(SysRole::getRoleId).orElseThrow());
  }

  /**
   * 根据用户ID查询菜单树信息
   *
   * @param userId 用户ID
   * @return 菜单列表
   */
  @Override
  public List<SysMenu> selectMenuTreeByUserId(final Long userId) {
    final List<SysMenu> menuList;
    if (SysUser.isAdmin(userId)) {

      menuList = self.selectMenuTreeAll();

    } else {
      menuList = self.selectMenuList(userId);
    }

    return MenuUtils.getChildPerms(menuList, 0);
  }

  /**
   * 根据角色ID查询菜单树信息
   *
   * @param roleId 角色ID
   * @return 选中菜单列表
   */
  @Override
  public List<Long> selectMenuListByRoleId(final Long roleId) {

    Boolean menuCheckStrictly = entityQuery.queryable(SysRole.class)
      .whereById(roleId)
      .select(SysRoleProxy::menuCheckStrictly)
      .singleNotNull();

    return self.selectMenuListByRoleId(roleId, menuCheckStrictly);

  }

  /**
   * 构建前端路由所需要的菜单
   *
   * @param menus 菜单列表
   * @return 路由列表
   */
  @Override
  public List<RouterVo> buildMenus(final List<SysMenu> menus) {
    List<RouterVo> routers = new LinkedList<>();
    for (SysMenu menu : menus) {
      RouterVo router = new RouterVo();
      router.setHidden("1".equals(menu.getVisible()));
      router.setName(MenuUtils.getRouteName(menu));
      router.setPath(MenuUtils.getRouterPath(menu));

      router.setComponent(MenuUtils.getComponent(menu));
      router.setQuery(menu.getQuery());
      router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
      List<SysMenu> cMenus = menu.getChildren();
      if (StringUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
        router.setAlwaysShow(true);
        router.setRedirect("noRedirect");
        router.setChildren(buildMenus(cMenus));
      } else if (MenuUtils.isMenuFrame(menu)) {
        router.setMeta(null);
        List<RouterVo> childrenList = new ArrayList<>();
        RouterVo children = new RouterVo();
        children.setPath(menu.getPath());
        children.setComponent(menu.getComponent());
        children.setName(StringUtils.capitalize(menu.getPath()));
        children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache()), menu.getPath()));
        children.setQuery(menu.getQuery());
        childrenList.add(children);
        router.setChildren(childrenList);
      } else if (menu.getParentId().intValue() == 0 && MenuUtils.isInnerLink(menu)) {
        router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
        router.setPath("/");
        List<RouterVo> childrenList = new ArrayList<>();
        RouterVo children = new RouterVo();
        String routerPath = MenuUtils.innerLinkReplaceEach(menu.getPath());
        children.setPath(routerPath);
        children.setComponent(UserConstants.INNER_LINK);
        children.setName(StringUtils.capitalize(routerPath));
        children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
        childrenList.add(children);
        router.setChildren(childrenList);
      }
      routers.add(router);
    }
    return routers;
  }

  /**
   * 构建前端所需要树结构
   *
   * @param menus 菜单列表
   * @return 树结构列表
   */
  @Override
  public List<SysMenu> buildMenuTree(final List<SysMenu> menus) {
    List<SysMenu> returnList = new ArrayList<>();
    List<Long> tempList = menus.stream().map(SysMenu::getMenuId).toList();
    for (SysMenu menu : menus) {
      // 如果是顶级节点, 遍历该父节点的所有子节点
      if (!tempList.contains(menu.getParentId())) {
        MenuUtils.recursionFn(menus, menu);
        returnList.add(menu);
      }
    }
    if (returnList.isEmpty()) {
      returnList = menus;
    }
    return returnList;
  }

  /**
   * 构建前端所需要下拉树结构
   *
   * @param menus 菜单列表
   * @return 下拉树结构列表
   */
  @Override
  public List<TreeSelect> buildMenuTreeSelect(final List<SysMenu> menus) {
    List<SysMenu> menuTrees = buildMenuTree(menus);
    return menuTrees.stream().map(TreeSelect::new).collect(Collectors.toList());
  }

  /**
   * 根据菜单ID查询信息
   *
   * @param menuId 菜单ID
   * @return 菜单信息
   */
  @Override
  public SysMenu selectMenuById(final Long menuId) {
    return entityQuery.queryable(SysMenu.class).whereById(menuId).singleOrNull();
  }

  /**
   * 是否存在菜单子节点
   *
   * @param menuId 菜单ID
   * @return 结果 true 存在 false 不存在
   */
  @Override
  public boolean hasChildByMenuId(final Long menuId) {
    return entityQuery.queryable(SysMenu.class)
      .where(menu -> menu.parentId().eq(menuId))
      .any();
  }

  /**
   * 查询菜单是否存在角色
   *
   * @param menuId 菜单ID
   * @return 结果 true 存在 false 不存在
   */
  @Override
  public boolean checkMenuExistRole(final Long menuId) {
    return roleMenuService.countByMenuId(menuId) > 0L;
  }

  /**
   * 新增保存菜单信息
   *
   * @param menu 菜单信息
   * @return 结果
   */
  @Override
  public long insertMenu(final SysMenu menu) {
    return entityQuery.insertable(menu)
      .setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS)
      .executeRows(true);
  }

  /**
   * 修改保存菜单信息
   *
   * @param menu 菜单信息
   * @return 结果
   */
  @Override
  public long updateMenu(final SysMenu menu) {
    return entityQuery.updatable(menu)
      .setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS)
      .executeRows();
  }

  /**
   * 删除菜单管理信息
   *
   * @param menuId 菜单ID
   * @return 结果
   */
  @Override
  public long deleteMenuById(final Long menuId) {
    return entityQuery.deletable(SysMenu.class)
      .allowDeleteStatement(true)
      .where(menu -> menu.menuId().eq(menuId))
      .executeRows();
  }

  /**
   * 校验菜单名称是否唯一
   *
   * @param menu 菜单信息
   * @return 结果
   */
  @Override
  public boolean checkMenuNameUnique(final SysMenu menu) {

    final SysMenu temp = entityQuery.queryable(SysMenu.class)
      .where(menu1 -> {
        menu1.parentId().eq(menu.getParentId());
        menu1.menuName().eq(menu.getMenuName());
      })
      .singleOrNull();
    if (Objects.nonNull(temp) && !Objects.equals(menu.getMenuId(), temp.getMenuId())) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }

  @Tran(policy = TranPolicy.supports)
  @Override
  public List<SysMenu> selectMenuTreeAll() {
    return entityQuery.queryable(SysMenu.class)
      .where(menu -> {
        menu.menuType().in(new String[]{"M", "C"});
        menu.status().eq(UserConstants.NORMAL);
      })
      .orderBy(menu -> {
        menu.parentId();
        menu.orderNum();
      })
      .toList();
  }

  @Override
  public List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly) {
//    return List.of();
    //	select m.menu_id
    //		from sys_menu m
    //            left join sys_role_menu rm on m.menu_id = rm.menu_id
    //        where rm.role_id = #{roleId}
    //            <if test="menuCheckStrictly">
    //              and m.menu_id not in (select m.parent_id from sys_menu m inner join sys_role_menu rm on m.menu_id = rm.menu_id and rm.role_id = #{roleId})
    //            </if>
    //		order by m.parent_id, m.order_num
    return entityQuery.queryable(SysMenu.class)
      .leftJoin(SysRoleMenu.class, (menu, roleMenu) -> menu.menuId().eq(roleMenu.menuId()))
      .where((menu, roleMenu) -> roleMenu.roleId().eq(roleId))
      .where((menu, roleMenu) -> {
        if (menuCheckStrictly) {
          entityQuery.queryable(SysMenu.class)
            .leftJoin(SysRoleMenu.class, (m, rm) -> m.menuId().eq(rm.menuId()))
            .where((m, rm) -> rm.roleId().eq(roleId))
            .selectColumn(SysMenuProxy::parentId)
            .distinct()
            .toList();
        }
      })
      .selectColumn((menu, roleMenu) -> menu.menuId())
      .distinct()
      .toList();
  }


}
