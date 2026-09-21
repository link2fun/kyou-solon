package com.github.link2fun.system.modular.mapping;

import com.github.link2fun.KyouApp;
import com.github.link2fun.system.modular.roledept.service.ISystemRoleDeptService;
import com.github.link2fun.system.modular.roledept.service.impl.RoleDeptMapping;
import com.github.link2fun.system.modular.rolemenu.service.ISystemRoleMenuService;
import com.github.link2fun.system.modular.rolemenu.service.impl.RoleMenuMapping;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import com.github.link2fun.system.modular.userrole.service.impl.RoleUserMapping;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;
import org.noear.solon.test.annotation.Rollback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 各关联表 adapter 的集成 smoke, 宿主 ID 用合成值; 算法差异行为由 kyou-support 的 MappingSyncTest 覆盖。
 * 注意: @Rollback 经 solon AOP 代理生效, 测试方法必须为 public。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class MappingSyncSmokeTest {

  /** 库里不存在的合成宿主 ID, 与真实数据隔离 */
  private static final long OWNER_A = 990001L;
  private static final long OWNER_B = 990002L;

  @Inject
  private ISystemUserRoleService userRoleService;

  @Inject
  private ISystemUserPostService userPostService;

  @Inject
  private ISystemRoleDeptService roleDeptService;

  @Inject
  private ISystemRoleMenuService roleMenuService;

  @Inject
  private RoleDeptMapping roleDeptMapping;

  @Inject
  private RoleMenuMapping roleMenuMapping;

  @Inject
  private RoleUserMapping roleUserMapping;

  @Rollback
  @Test
  @DisplayName("用户-角色：建立、收敛、清空、批量清空走通")
  public void userRoleMappingRoundTrip() {
    userRoleService.reassignRoles(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(userRoleService.findRoleIdListByUserId(OWNER_A)));

    // 收敛到目标集合: 1 被移除, 3 被新增
    userRoleService.reassignRoles(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(userRoleService.findRoleIdListByUserId(OWNER_A)));

    // null 视同清空
    userRoleService.reassignRoles(OWNER_A, null);
    assertTrue(userRoleService.findRoleIdListByUserId(OWNER_A).isEmpty());

    userRoleService.reassignRoles(OWNER_A, List.of(5L));
    userRoleService.deleteUserRole(List.of(OWNER_A));
    assertTrue(userRoleService.findRoleIdListByUserId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("用户-岗位：建立、收敛、清空、批量清空走通")
  public void userPostMappingRoundTrip() {
    userPostService.reassignPosts(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(userPostService.findPostIdListByUserId(OWNER_A)));

    userPostService.reassignPosts(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(userPostService.findPostIdListByUserId(OWNER_A)));

    userPostService.reassignPosts(OWNER_A, List.of());
    assertTrue(userPostService.findPostIdListByUserId(OWNER_A).isEmpty());

    userPostService.reassignPosts(OWNER_A, List.of(5L));
    userPostService.deleteUserPost(List.of(OWNER_A));
    assertTrue(userPostService.findPostIdListByUserId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-部门：建立、收敛、批量删除关联走通")
  public void roleDeptMappingRoundTrip() {
    roleDeptService.reassignDepts(OWNER_A, List.of(100L, 101L));
    assertEquals(Set.of(100L, 101L), new HashSet<>(roleDeptMapping.findTargetIds(OWNER_A)));

    roleDeptService.reassignDepts(OWNER_A, List.of(101L, 102L));
    assertEquals(Set.of(101L, 102L), new HashSet<>(roleDeptMapping.findTargetIds(OWNER_A)));

    roleDeptService.removeByRoleIds(List.of(OWNER_A));
    assertTrue(roleDeptMapping.findTargetIds(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-菜单：建立、收敛、批量删除关联走通")
  public void roleMenuMappingRoundTrip() {
    roleMenuService.reassignMenus(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(roleMenuMapping.findTargetIds(OWNER_A)));

    roleMenuService.reassignMenus(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(roleMenuMapping.findTargetIds(OWNER_A)));

    roleMenuService.removeByRoleIds(List.of(OWNER_A));
    assertTrue(roleMenuMapping.findTargetIds(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-用户（反向朝向）：授权只增不删、选择性取消走通")
  public void roleUserMappingRoundTrip() {
    userRoleService.assignUsers(OWNER_A, List.of(OWNER_B, 990003L));
    assertEquals(Set.of(OWNER_B, 990003L), new HashSet<>(roleUserMapping.findTargetIds(OWNER_A)));

    // add 模式只增不删
    userRoleService.assignUsers(OWNER_A, List.of(990003L, 990004L));
    assertEquals(Set.of(OWNER_B, 990003L, 990004L), new HashSet<>(roleUserMapping.findTargetIds(OWNER_A)));

    userRoleService.unassignUsers(OWNER_A, List.of(OWNER_B));
    assertEquals(Set.of(990003L, 990004L), new HashSet<>(roleUserMapping.findTargetIds(OWNER_A)));

    userRoleService.unassignUser(OWNER_A, 990003L);
    assertEquals(Set.of(990004L), new HashSet<>(roleUserMapping.findTargetIds(OWNER_A)));
  }

}
