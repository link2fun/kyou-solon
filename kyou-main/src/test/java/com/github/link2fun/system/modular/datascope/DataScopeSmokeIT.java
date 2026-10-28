package com.github.link2fun.system.modular.datascope;

import com.github.link2fun.KyouApp;
import com.github.link2fun.framework.auth.DataScopeRowGuard;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.model.req.SysRoleSearchReq;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.req.SysUserSearchReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 数据范围与真实查询拼装的集成 smoke：判定矩阵与 SQL 形状由 DataScopeSqlIT 覆盖，
 * 这里只钉「别名与 asAlias 对齐」与「条件真的进了查询结果」这类纯单测看不到的故障。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class DataScopeSmokeIT {

  /** 种子数据里的超管用户 ID */
  private static final long SUPER_ADMIN_ID = 1L;
  /** 种子数据里的超管部门 ID(研发部门) */
  private static final long SUPER_ADMIN_DEPT_ID = 103L;
  /** 种子数据里的普通用户 ry 的用户 ID */
  private static final long ORDINARY_USER_ID = 2L;
  /** 种子数据里 ry 所属的部门 ID(测试部门), 也是其自定义数据范围内 */
  private static final long ORDINARY_USER_DEPT_ID = 105L;
  /** 种子数据里自定义数据范围的角色 ID, sys_role_dept 挂 100/101/105 */
  private static final long CUSTOM_SCOPE_ROLE_ID = 2L;
  /** 不在 ry 自定义数据范围内的部门 ID(研发部门) */
  private static final long OUT_OF_SCOPE_DEPT_ID = 103L;

  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemDeptService deptService;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private DataScopeRowGuard dataScopeRowGuard;

  /** 造一个持有指定数据范围角色的操作上下文 */
  private static ActionContext contextOf(final Long userId, final Long deptId, final Long roleId, final String dataScope) {
    final RoleDTO role = new RoleDTO();
    role.setRoleId(roleId);
    role.setDataScope(dataScope);

    final SysUserDTO user = new SysUserDTO();
    user.setUserId(userId);
    user.setDeptId(deptId);
    user.setRoles(List.of(role));

    final SessionUser sessionUser = new SessionUser();
    sessionUser.setUserId(userId);
    sessionUser.setDeptId(deptId);
    sessionUser.setUser(user);

    return ActionContext.builder().withUserId(userId).withDeptId(deptId).withSessionUser(sessionUser).build();
  }

  @Test
  @DisplayName("超管查用户列表不过滤, 两个种子用户都看得到")
  public void superAdminSeesAllUsers() {
    final ActionContext context = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
    assertTrue(context.isSuperAdmin());

    final Page<SysUserDTO> page = userService.selectUserList(context, Page.of(1L, 10L), new SysUserSearchReq(), SysUserDTO.class);
    assertEquals(2, page.getRecords().size());
  }

  @Test
  @DisplayName("自定义数据范围只看到范围内部门的用户")
  public void customScopeSeesOnlyUsersInScope() {
    final ActionContext context = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");
    assertFalse(context.isSuperAdmin());

    final Page<SysUserDTO> page = userService.selectUserList(context, Page.of(1L, 10L), new SysUserSearchReq(), SysUserDTO.class);
    assertEquals(1, page.getRecords().size(), "只有 ry(部门 105) 在 role 2 的自定义范围内");
    assertEquals("ry", page.getRecords().get(0).getUserName());
  }

  @Test
  @DisplayName("非超管持有全部数据角色时看全部, 不再拼出空括号谓词")
  public void allScopeRoleDoesNotFilterForOrdinaryUser() {
    final ActionContext context = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, 999L, "1");

    final Page<SysUserDTO> page = userService.selectUserList(context, Page.of(1L, 10L), new SysUserSearchReq(), SysUserDTO.class);
    assertEquals(2, page.getRecords().size(), "「全部数据」角色应当看全部, 修复前会拼出 AND () 报 SQL 错");
  }

  @Test
  @DisplayName("仅本人数据范围只看到自己")
  public void selfScopeSeesOnlySelf() {
    final ActionContext context = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, 999L, "5");

    final Page<SysUserDTO> page = userService.selectUserList(context, Page.of(1L, 10L), new SysUserSearchReq(), SysUserDTO.class);
    assertEquals(1, page.getRecords().size(), "仅本人范围只看得到 ry 自己");
    assertEquals("ry", page.getRecords().get(0).getUserName());
  }

  @Test
  @DisplayName("仅本人数据范围查部门列表: 查询没有用户列, 本人条件救不了场, 恒假看不到任何部门")
  public void selfScopeSeesNoDeptOnDeptList() {
    final ActionContext context = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, 999L, "5");

    assertTrue(deptService.selectDeptList(context, new SysDeptSearchReq()).isEmpty(), "本人轴在部门查询上给不出条件, 应过滤到空");
  }

  @Test
  @DisplayName("部门行准入: 超管放行, 自定义范围外的部门拒绝")
  public void deptVisibleFollowsDataScope() {
    final ActionContext superAdmin = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertDeptVisible(superAdmin, OUT_OF_SCOPE_DEPT_ID), "超管不受部门限制");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertDeptVisible(superAdmin, ORDINARY_USER_DEPT_ID), "超管不受部门限制");

    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertDeptVisible(ordinary, ORDINARY_USER_DEPT_ID), "范围内部门应放行");
    assertThrows(ServiceException.class, () -> dataScopeRowGuard.assertDeptVisible(ordinary, OUT_OF_SCOPE_DEPT_ID),
      "范围外部门应拒绝");
  }

  @Test
  @DisplayName("角色列表: 按「角色下有范围内用户」过滤, exists 子查询里的数据范围走通")
  public void roleListFollowsDataScope() {
    final ActionContext superAdmin = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
    assertEquals(2, roleService
      .selectRoleList(superAdmin, Page.of(1L, 10L), new SysRoleSearchReq(), SysRole.class)
      .getRecords().size(), "超管看全部两个种子角色");

    // 角色 2(自定义 100/101/105) 下只有 ry(部门 105)在范围内; 角色 1 下只有 admin(部门 103)不在
    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");
    final Page<SysRole> visible = roleService
      .selectRoleList(ordinary, Page.of(1L, 10L), new SysRoleSearchReq(), SysRole.class);
    assertEquals(1, visible.getRecords().size(), "只看得到有范围内用户的角色");
    assertEquals(CUSTOM_SCOPE_ROLE_ID, visible.getRecords().get(0).getRoleId());
  }

  @Test
  @DisplayName("用户行准入: 点查路径走通; 可见不等于可改, 超管用户只可见")
  public void userRowVisibilityAndOperability() {
    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");

    assertDoesNotThrow(() -> dataScopeRowGuard.assertUserVisible(ordinary, ORDINARY_USER_ID),
      "ry(部门 105) 在角色 2 的自定义范围内");
    assertThrows(ServiceException.class, () -> dataScopeRowGuard.assertUserVisible(ordinary, 999999L),
      "不在范围内的用户应报没有权限访问用户数据");
    assertThrows(ServiceException.class, () -> dataScopeRowGuard.assertUserVisible(ordinary, SUPER_ADMIN_ID),
      "admin(部门 103) 不在普通用户的自定义范围内");

    // 可改: 超管用户受保护, 即便操作者是超管也不能改
    final ActionContext superAdmin = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertUserVisible(superAdmin, SUPER_ADMIN_ID),
      "超管上下文不过滤, 看自己是放行的");
    final ServiceException protectedError = assertThrows(ServiceException.class,
      () -> dataScopeRowGuard.assertUserOperable(superAdmin, SUPER_ADMIN_ID));
    assertEquals("不允许操作超级管理员用户", protectedError.getMessage());

    // 仅本人范围查自己也应放行(本人轴救场, 与部门无关)
    final ActionContext selfOnly = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, 999L, "5");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertUserVisible(selfOnly, ORDINARY_USER_ID));
  }

  @Test
  @DisplayName("角色行准入: 点查与角色列表同口径; 超管角色只可见不可改")
  public void roleRowVisibilityAndOperability() {
    final ActionContext superAdmin = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertRoleVisible(superAdmin, CUSTOM_SCOPE_ROLE_ID));
    assertDoesNotThrow(() -> dataScopeRowGuard.assertRoleVisible(superAdmin, 1L), "超管看超管角色: 可见性放行");

    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");
    assertDoesNotThrow(() -> dataScopeRowGuard.assertRoleVisible(ordinary, CUSTOM_SCOPE_ROLE_ID),
      "角色 2 下有范围内用户 ry");
    assertThrows(ServiceException.class, () -> dataScopeRowGuard.assertRoleVisible(ordinary, 999999L),
      "不存在的角色不可见");
    assertThrows(ServiceException.class, () -> dataScopeRowGuard.assertRoleVisible(ordinary, 1L),
      "角色 1 下只有 admin(部门 103), 不在范围内");

    final ServiceException protectedError = assertThrows(ServiceException.class,
      () -> dataScopeRowGuard.assertRoleOperable(superAdmin, 1L));
    assertEquals("不允许操作超级管理员角色", protectedError.getMessage());
  }

  @Test
  @DisplayName("已分配用户列表: 多表 join + distinct 分页下数据范围照常过滤, ry 在范围内查得出来")
  public void allocatedListFollowsDataScope() {
    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");

    final SysUserSearchReq searchReq = new SysUserSearchReq();
    searchReq.setRoleId(CUSTOM_SCOPE_ROLE_ID);
    final Page<AllocatedUserDTO> page = userService.selectAllocatedList(ordinary, Page.of(1L, 10L), searchReq);

    assertEquals(1, page.getRecords().size(), "角色 2 下只有 ry, 且 ry 的部门在范围内");
    assertEquals("ry", page.getRecords().get(0).getUserName());
  }

  @Test
  @DisplayName("未分配用户列表: 范围内没有未分配用户时查回空, 查询可正常执行")
  public void unallocatedListFollowsDataScope() {
    final ActionContext ordinary = contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");

    final SysUserSearchReq searchReq = new SysUserSearchReq();
    searchReq.setRoleId(CUSTOM_SCOPE_ROLE_ID);
    final Page<AllocatedUserDTO> page = userService.selectUnallocatedList(ordinary, Page.of(1L, 10L), searchReq);

    // 未分配角色 2 的用户只有 admin(部门 103), 不在白名单 {100,101,105} 内
    assertTrue(page.getRecords().isEmpty());
  }

  @Test
  @DisplayName("未分配用户列表: 不带 roleId 时守卫生效, 语义为「无任何角色的用户」而非「全部用户」")
  public void unallocatedListWithoutRoleIdGuardsToRolelessUsers() {
    final ActionContext admin = contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");

    // roleId 保持 null, 模拟入口 GET 不带 roleId(Controller 对 roleId 无校验, null 直达 service)
    final SysUserSearchReq searchReq = new SysUserSearchReq();
    final Page<AllocatedUserDTO> page = userService.selectUnallocatedList(admin, Page.of(1L, 10L), searchReq);

    // 种子数据里两个用户都持有角色(sys_user_role: 1→1, 2→2), 无任何角色的用户是空集。
    // 若 none(eq(...)) 的 IdUtils.isIdValid 守卫丢失, eq(null) 生成 role_id = null 恒 UNKNOWN,
    // EXISTS 恒假 → 退化为「全部未删用户」, 超管下会查出 2 条, 本断言即失败。
    assertTrue(page.getRecords().isEmpty(),
      "roleId=null 应返回无任何角色的用户(种子数据下为空), 而不是全部用户");
  }
}
