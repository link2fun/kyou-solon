package com.github.link2fun.system.modular.menu;

import com.easy.query.api.proxy.base.LongProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.KyouApp;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.support.core.domain.entity.proxy.SysMenuProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleMenuProxy;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * selectMenuListByRoleId 的 SQL 形状与行为验证:
 * 形状用同构查询取 toSQL 不执行, 钉「严格分支 not in 子查询」的翻译;
 * 行为直接跑真方法, 用库内数据自洽推导期望(严格勾选 = 已勾选集合 - 已勾选菜单的父节点),
 * 不依赖具体种子值。种子数据来自 env/docker 开发库。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class MenuStrictCheckSqlTest {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private ISystemMenuService menuService;

  /** 与 SystemMenuServiceImpl.selectMenuListByRoleId 同构的查询, 取 toSQL 不执行 */
  private String checkedMenuSQL(final Long roleId, final boolean menuCheckStrictly) {
    return entityQuery.queryable(SysMenu.class)
      .leftJoin(SysRoleMenu.class, (menu, roleMenu) -> menu.menuId().eq(roleMenu.menuId()))
      .where((menu, roleMenu) -> roleMenu.roleId().eq(roleId))
      .where((menu, roleMenu) -> {
        if (menuCheckStrictly) {
          EntityQueryable<LongProxy, Long> checkedParentIds = entityQuery.queryable(SysMenu.class)
            .innerJoin(SysRoleMenu.class, (_menu, _roleMenu) -> {
              _menu.menuId().eq(_roleMenu.menuId());
              _roleMenu.roleId().eq(roleId);
            })
            .select((_menu, _roleMenu) -> new LongProxy(_menu.parentId()));
          menu.menuId().notIn(checkedParentIds);
        }
      })
      .selectColumn((menu, roleMenu) -> menu.menuId())
      .distinct()
      .toSQL();
  }

  @Test
  @DisplayName("严格分支 SQL: 主查询勾选 + not in 子查询排除已勾选菜单的父节点")
  void strictSQLContent() {
    assertEquals("SELECT DISTINCT t.`menu_id` FROM `sys_menu` t "
        + "LEFT JOIN `sys_role_menu` t1 ON t.`menu_id` = t1.`menu_id` "
        + "WHERE t1.`role_id` = ? "
        + "AND t.`menu_id` NOT IN ("
        + "SELECT t2.`parent_id` FROM `sys_menu` t2 "
        + "INNER JOIN `sys_role_menu` t3 ON t2.`menu_id` = t3.`menu_id` AND t3.`role_id` = ?)",
      checkedMenuSQL(2L, true));
  }

  @Test
  @DisplayName("非严格分支 SQL: 单段勾选查询, 无子查询")
  void nonStrictSQLContent() {
    assertEquals("SELECT DISTINCT t.`menu_id` FROM `sys_menu` t "
        + "LEFT JOIN `sys_role_menu` t1 ON t.`menu_id` = t1.`menu_id` "
        + "WHERE t1.`role_id` = ?",
      checkedMenuSQL(2L, false));
  }

  @Test
  @DisplayName("行为自洽: 严格勾选 = 已勾选集合 - 已勾选菜单的父节点(对库内全部真实角色成立)")
  void strictResultMatchesIndependentOracle() {
    final List<Long> roleIds = entityQuery.queryable(SysRoleMenu.class)
      .selectColumn(SysRoleMenuProxy::roleId)
      .distinct()
      .toList();
    assertFalse(roleIds.isEmpty(), "开发库应有角色-菜单关联数据");

    for (final Long roleId : roleIds) {
      final List<Long> checked = menuService.selectMenuListByRoleId(roleId, false);
      final List<Long> strict = menuService.selectMenuListByRoleId(roleId, true);

      // 独立推导: 已勾选菜单的父 id(0 是虚拟根, 不参与排除)
      final Set<Long> checkedParentIds = entityQuery.queryable(SysMenu.class)
        .where(menu -> menu.menuId().in(checked))
        .selectColumn(SysMenuProxy::parentId)
        .distinct()
        .toList().stream()
        .filter(parentId -> parentId != 0L)
        .collect(Collectors.toSet());
      final Set<Long> expectedStrict = checked.stream()
        .filter(menuId -> !checkedParentIds.contains(menuId))
        .collect(Collectors.toSet());

      assertEquals(expectedStrict, Set.copyOf(strict), "角色 " + roleId + " 的严格勾选应只含叶子勾选");
      assertTrue(checked.containsAll(strict), "角色 " + roleId + " 的严格勾选应是已勾选集合的子集");
    }
  }

  @Test
  @DisplayName("无勾选的角色: 两个分支都返回空")
  void emptyRoleReturnsEmpty() {
    final Long absentRoleId = 987654321L;

    assertTrue(menuService.selectMenuListByRoleId(absentRoleId, false).isEmpty());
    assertTrue(menuService.selectMenuListByRoleId(absentRoleId, true).isEmpty());
  }
}
