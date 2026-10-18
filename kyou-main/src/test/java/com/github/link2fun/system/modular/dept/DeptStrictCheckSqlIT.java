package com.github.link2fun.system.modular.dept;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.basic.api.select.Query;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.KyouApp;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
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
 * selectDeptListByRoleId 的 SQL 形状与行为验证
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class DeptStrictCheckSqlIT {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private ISystemDeptService deptService;

  /** 与 SystemDeptServiceImpl.selectDeptListByRoleId 同构的查询(隐式关联版), 供取 toSQL 或直接执行 */
  private Query<Long> checkedDeptQuery(final Long roleId, final boolean deptCheckStrictly) {
    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(dept -> {
        dept.roleDeptList().any(roleDept -> roleDept.roleId().eq(roleId));
        if (deptCheckStrictly) {
          // 严格勾选: 排除已勾选部门的父节点, 只回显该角色勾选的叶子
          Query<Long> checkedParentIds = entityQuery.queryable(SysDept.class)
            .where(_dept -> _dept.roleDeptList().any(_roleDept -> _roleDept.roleId().eq(roleId)))
            .selectColumn(SysDeptProxy::parentId);

          dept.deptId().notIn(checkedParentIds);
        }
      })
      .selectColumn(SysDeptProxy::deptId);
  }

  @Test
  @DisplayName("严格分支 SQL: 勾选 EXISTS + not in 子查询排除已勾选部门的父节点")
  void strictSQLContent() {
    assertEquals("SELECT dept.`dept_id` FROM `sys_dept` dept "
        + "WHERE EXISTS(SELECT 1 FROM `sys_role_dept` t1 WHERE t1.`dept_id` = dept.`dept_id` AND t1.`role_id` = ? LIMIT 1) "
        + "AND dept.`dept_id` NOT IN ("
        + "SELECT t2.`parent_id` FROM `sys_dept` t2 "
        + "WHERE EXISTS(SELECT 1 FROM `sys_role_dept` t3 WHERE t3.`dept_id` = t2.`dept_id` AND t3.`role_id` = ? LIMIT 1))",
      checkedDeptQuery(2L, true).toSQL());
  }

  @Test
  @DisplayName("非严格分支 SQL: 单段勾选 EXISTS 查询, 无子查询")
  void nonStrictSQLContent() {
    assertEquals("SELECT dept.`dept_id` FROM `sys_dept` dept "
        + "WHERE EXISTS(SELECT 1 FROM `sys_role_dept` t1 WHERE t1.`dept_id` = dept.`dept_id` AND t1.`role_id` = ? LIMIT 1)",
      checkedDeptQuery(2L, false).toSQL());
  }

  @Test
  @DisplayName("行为自洽: 按角色旗标分口径 — 非严格=已勾选集合, 严格=已勾选集合-已勾选部门的父节点(对库内全部真实角色成立)")
  void resultMatchesIndependentOracle() {
    final List<Long> roleIds = entityQuery.queryable(SysRoleDept.class)
      .selectColumn(SysRoleDeptProxy::roleId)
      .distinct()
      .toList();
    assertFalse(roleIds.isEmpty(), "开发库应有角色-部门关联数据");

    for (final Long roleId : roleIds) {
      final SysRole role = entityQuery.queryable(SysRole.class).whereById(roleId).singleOrNull();
      assertNotNull(role, "有角色-部门关联的角色应存在: " + roleId);
      final boolean strict = Boolean.TRUE.equals(role.getDeptCheckStrictly());

      final Set<Long> checked = entityQuery.queryable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().eq(roleId))
        .selectColumn(SysRoleDeptProxy::deptId)
        .toList().stream().collect(Collectors.toSet());
      final List<Long> actual = deptService.selectDeptListByRoleId(roleId);

      if (!strict) {
        assertEquals(checked, Set.copyOf(actual), "角色 " + roleId + " 非严格勾选应等于已勾选部门集合");
        continue;
      }

      // 独立推导: 已勾选部门的父 id(0 是虚拟根, 不参与排除)
      final Set<Long> checkedParentIds = entityQuery.queryable(SysDept.class)
        .where(dept -> dept.deptId().in(checked))
        .selectColumn(SysDeptProxy::parentId)
        .distinct()
        .toList().stream()
        .filter(parentId -> parentId != 0L)
        .collect(Collectors.toSet());
      final Set<Long> expectedStrict = checked.stream()
        .filter(deptId -> !checkedParentIds.contains(deptId))
        .collect(Collectors.toSet());

      assertEquals(expectedStrict, Set.copyOf(actual), "角色 " + roleId + " 的严格勾选应只含叶子勾选");
      assertTrue(checked.containsAll(actual), "角色 " + roleId + " 的严格勾选应是已勾选集合的子集");
    }
  }

  @Test
  @DisplayName("无勾选的角色: 两个分支都返回空(同构查询执行; 真方法对不存在角色会 NPE, 属既有行为)")
  void emptyRoleReturnsEmpty() {
    final Long absentRoleId = 987654321L;

    assertTrue(checkedDeptQuery(absentRoleId, false).toList().isEmpty());
    assertTrue(checkedDeptQuery(absentRoleId, true).toList().isEmpty());
  }
}
