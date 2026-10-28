package com.github.link2fun.system.modular.datascope;

import com.github.link2fun.KyouApp;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.system.modular.dept.model.req.SysDeptModifyReq;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.model.dto.SysRoleDetailDTO;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeDataScopeReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeStatusReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleModifyReq;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.req.SysUserChangeStatusReq;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import com.github.link2fun.system.modular.user.model.req.SysUserResetPwdReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import org.noear.solon.test.annotation.Rollback;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 行准入经 service 写入口的接线测试: DataScopeRowGuard 判定矩阵本身由 DataScopeSmokeIT 覆盖
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class DataScopeRowGuardOpsIT {

  /** 种子数据里的超管用户 ID */
  private static final long SUPER_ADMIN_ID = 1L;
  /** 种子数据里的超管部门 ID(研发部门), 不在 ry 的自定义范围内 */
  private static final long SUPER_ADMIN_DEPT_ID = 103L;
  /** 种子数据里的普通用户 ry 的用户 ID */
  private static final long ORDINARY_USER_ID = 2L;
  /** 种子数据里 ry 所属的部门 ID(测试部门) */
  private static final long ORDINARY_USER_DEPT_ID = 105L;
  /** 种子数据里自定义数据范围的角色 ID, sys_role_dept 挂 100/101/105 */
  private static final long CUSTOM_SCOPE_ROLE_ID = 2L;
  /** 库里不存在的合成 ID */
  private static final long NONEXISTENT_ID = 999999L;
  /** 范围外且无子部门、无用户的部门 ID(长沙分公司下) */
  private static final long OUT_OF_SCOPE_LEAF_DEPT_ID = 108L;

  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemDeptService deptService;

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

  private static ActionContext superAdmin() {
    return contextOf(SUPER_ADMIN_ID, SUPER_ADMIN_DEPT_ID, 1L, "1");
  }

  private static ActionContext ordinary() {
    return contextOf(ORDINARY_USER_ID, ORDINARY_USER_DEPT_ID, CUSTOM_SCOPE_ROLE_ID, "2");
  }

  /** 断言业务异常文案, 钉住「走到的是哪一级守卫」 */
  private static void assertServiceException(final Runnable action, final String expectedMessage) {
    final ServiceException error = assertThrows(ServiceException.class, action::run);
    assertEquals(expectedMessage, error.getMessage());
  }

  @Test
  @Rollback
  @DisplayName("角色写入口: 普通用户碰不到范围外角色, 超管角色对超管也不可改(operable)")
  public void roleWriteOpsGuarded() {
    // 范围外(不存在): 普通用户 → 没有权限访问角色数据
    assertServiceException(() -> roleService.updateRole(ordinary(), roleModifyReq(NONEXISTENT_ID)),
      "没有权限访问角色数据！");
    assertServiceException(() -> roleService.changeRoleStatus(ordinary(), roleStatusReq(NONEXISTENT_ID, "1")),
      "没有权限访问角色数据！");
    assertServiceException(() -> roleService.authDataScope(ordinary(), roleDataScopeReq(NONEXISTENT_ID)),
      "没有权限访问角色数据！");
    assertServiceException(() -> roleService.deleteRoleByIds(ordinary(), List.of(NONEXISTENT_ID)),
      "没有权限访问角色数据！");
    assertServiceException(() -> roleService.selectRoleDetail(ordinary(), NONEXISTENT_ID, SysRoleDetailDTO.class),
      "没有权限访问角色数据！");
    assertServiceException(() -> roleService.assignUsers(ordinary(), NONEXISTENT_ID, List.of(ORDINARY_USER_ID)),
      "没有权限访问角色数据！");

    // 超管角色: 即便是超管也受保护 → 证明写入口接的是 operable 而非 visible
    assertServiceException(() -> roleService.updateRole(superAdmin(), roleModifyReq(1L)),
      "不允许操作超级管理员角色");
    assertServiceException(() -> roleService.changeRoleStatus(superAdmin(), roleStatusReq(1L, "1")),
      "不允许操作超级管理员角色");
    assertServiceException(() -> roleService.authDataScope(superAdmin(), roleDataScopeReq(1L)),
      "不允许操作超级管理员角色");
    assertServiceException(() -> roleService.deleteRoleByIds(superAdmin(), List.of(1L)),
      "不允许操作超级管理员角色");

    // 超管放行: 可见性早退 + 不存在的角色删除是空操作
    assertDoesNotThrow(() -> roleService.selectRoleDetail(superAdmin(), CUSTOM_SCOPE_ROLE_ID, SysRoleDetailDTO.class));
    assertDoesNotThrow(() -> roleService.deleteRoleByIds(superAdmin(), List.of(NONEXISTENT_ID)));
  }

  @Test
  @Rollback
  @DisplayName("用户写入口: 普通用户碰不到范围外用户, 超管用户对超管也不可改(operable)")
  public void userWriteOpsGuarded() {
    // 范围外(不存在): 普通用户 → 没有权限访问用户数据
    assertServiceException(() -> userService.updateUser(ordinary(), userUpdateReq(NONEXISTENT_ID, "ghost")),
      "没有权限访问用户数据！");
    assertServiceException(() -> userService.updateUserStatus(ordinary(), userStatusReq(NONEXISTENT_ID, "1")),
      "没有权限访问用户数据！");
    assertServiceException(() -> userService.resetPwd(ordinary(), userResetPwdReq(NONEXISTENT_ID)),
      "没有权限访问用户数据！");
    assertServiceException(() -> userService.deleteUserByIds(ordinary(), List.of(NONEXISTENT_ID)),
      "没有权限访问用户数据！");
    // 授权角色给范围外用户(admin 在部门 103)
    assertServiceException(() -> userService.insertUserAuth(ordinary(), SUPER_ADMIN_ID, List.of(CUSTOM_SCOPE_ROLE_ID)),
      "没有权限访问用户数据！");

    // 超管用户: 受保护。删除入口先挡「当前用户不能删除」(超管删自己), 改用普通用户删超管证明 operable 接线
    assertServiceException(() -> userService.updateUser(superAdmin(), userUpdateReq(1L, "admin")),
      "不允许操作超级管理员用户");
    assertServiceException(() -> userService.updateUserStatus(superAdmin(), userStatusReq(1L, "1")),
      "不允许操作超级管理员用户");
    assertServiceException(() -> userService.resetPwd(superAdmin(), userResetPwdReq(1L)),
      "不允许操作超级管理员用户");
    assertServiceException(() -> userService.deleteUserByIds(ordinary(), List.of(SUPER_ADMIN_ID)),
      "不允许操作超级管理员用户");
    // 导入更新超管账号: 异常在行内聚合成整批失败
    final ServiceException importError = assertThrows(ServiceException.class,
      () -> userService.importUser(superAdmin(), List.of(userDto("admin")), true, "admin"));
    assertTrue(importError.getMessage().contains("不允许操作超级管理员用户"),
      "导入更新超管账号应触发行保护: " + importError.getMessage());

    // 超管放行
    assertDoesNotThrow(() -> userService.updateUserStatus(superAdmin(), userStatusReq(ORDINARY_USER_ID, "1")));
    assertDoesNotThrow(() -> userService.insertUserAuth(superAdmin(), ORDINARY_USER_ID, List.of(CUSTOM_SCOPE_ROLE_ID)));
  }

  @Test
  @Rollback
  @DisplayName("部门写入口: 普通用户碰不到范围外部门, 超管放行")
  public void deptWriteOpsGuarded() {
    assertServiceException(() -> deptService.selectDeptDetail(ordinary(), SUPER_ADMIN_DEPT_ID),
      "没有权限访问部门数据！");
    assertServiceException(() -> deptService.updateDept(ordinary(), deptModifyReq(SUPER_ADMIN_DEPT_ID)),
      "没有权限访问部门数据！");
    // 108 是叶子部门且无用户, 拒绝原因只能是范围
    assertServiceException(() -> deptService.deleteDeptById(ordinary(), OUT_OF_SCOPE_LEAF_DEPT_ID),
      "没有权限访问部门数据！");

    assertDoesNotThrow(() -> deptService.selectDeptDetail(superAdmin(), SUPER_ADMIN_DEPT_ID));
    assertDoesNotThrow(() -> deptService.deleteDeptById(superAdmin(), OUT_OF_SCOPE_LEAF_DEPT_ID));
  }

  private static SysRoleModifyReq roleModifyReq(final Long roleId) {
    final SysRoleModifyReq req = new SysRoleModifyReq();
    req.setRoleId(roleId);
    return req;
  }

  private static SysRoleChangeStatusReq roleStatusReq(final Long roleId, final String status) {
    final SysRoleChangeStatusReq req = new SysRoleChangeStatusReq();
    req.setRoleId(roleId);
    req.setStatus(status);
    return req;
  }

  private static SysRoleChangeDataScopeReq roleDataScopeReq(final Long roleId) {
    final SysRoleChangeDataScopeReq req = new SysRoleChangeDataScopeReq();
    req.setRoleId(roleId);
    req.setDataScope("1");
    return req;
  }

  private static SysUserReq.UpdateReq userUpdateReq(final Long userId, final String userName) {
    final SysUserReq.UpdateReq req = new SysUserReq.UpdateReq();
    req.setUserId(userId);
    req.setUserName(userName);
    return req;
  }

  private static SysUserChangeStatusReq userStatusReq(final Long userId, final String status) {
    final SysUserChangeStatusReq req = new SysUserChangeStatusReq();
    req.setUserId(userId);
    req.setStatus(status);
    return req;
  }

  private static SysUserResetPwdReq userResetPwdReq(final Long userId) {
    final SysUserResetPwdReq req = new SysUserResetPwdReq();
    req.setUserId(userId);
    req.setPassword("resetPwd");
    return req;
  }

  private static SysDeptModifyReq deptModifyReq(final Long deptId) {
    final SysDeptModifyReq req = new SysDeptModifyReq();
    req.setDeptId(deptId);
    return req;
  }

  private static SysUserDTO userDto(final String userName) {
    final SysUserDTO dto = new SysUserDTO();
    dto.setUserName(userName);
    return dto;
  }
}
