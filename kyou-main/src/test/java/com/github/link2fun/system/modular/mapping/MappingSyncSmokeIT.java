package com.github.link2fun.system.modular.mapping;

import com.github.link2fun.KyouApp;
import com.github.link2fun.system.modular.roledept.RoleDeptRelation;
import com.github.link2fun.system.modular.rolemenu.RoleMenuRelation;
import com.github.link2fun.system.modular.userpost.UserPostRelation;
import com.github.link2fun.system.modular.userrole.UserRoleRelation;
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
 * 各关联 module 的集成 smoke, 宿主 ID 用合成值; 算法差异行为由 kyou-support 的 MappingSyncTest 覆盖。
 * 注意: @Rollback 经 solon AOP 代理生效, 测试方法必须为 public。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class MappingSyncSmokeIT {

  /** 库里不存在的合成宿主 ID, 与真实数据隔离 */
  private static final long OWNER_A = 990001L;
  private static final long OWNER_B = 990002L;

  @Inject
  private UserRoleRelation userRoleRelation;

  @Inject
  private UserPostRelation userPostRelation;

  @Inject
  private RoleDeptRelation roleDeptRelation;

  @Inject
  private RoleMenuRelation roleMenuRelation;

  @Rollback
  @Test
  @DisplayName("用户-角色：建立、收敛、清空、批量清空走通")
  public void userRoleRelationRoundTrip() {
    userRoleRelation.reassignRoles(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(userRoleRelation.findRoleIdsByUserId(OWNER_A)));

    // 收敛到目标集合: 1 被移除, 3 被新增
    userRoleRelation.reassignRoles(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(userRoleRelation.findRoleIdsByUserId(OWNER_A)));

    // null 视同清空
    userRoleRelation.reassignRoles(OWNER_A, null);
    assertTrue(userRoleRelation.findRoleIdsByUserId(OWNER_A).isEmpty());

    userRoleRelation.reassignRoles(OWNER_A, List.of(5L));
    userRoleRelation.removeByUserIds(List.of(OWNER_A));
    assertTrue(userRoleRelation.findRoleIdsByUserId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("用户-岗位：建立、收敛、清空、批量清空走通")
  public void userPostRelationRoundTrip() {
    userPostRelation.reassignPosts(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(userPostRelation.findPostIdsByUserId(OWNER_A)));

    userPostRelation.reassignPosts(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(userPostRelation.findPostIdsByUserId(OWNER_A)));

    userPostRelation.reassignPosts(OWNER_A, List.of());
    assertTrue(userPostRelation.findPostIdsByUserId(OWNER_A).isEmpty());

    userPostRelation.reassignPosts(OWNER_A, List.of(5L));
    userPostRelation.removeByUserIds(List.of(OWNER_A));
    assertTrue(userPostRelation.findPostIdsByUserId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-部门：建立、收敛、批量删除关联走通")
  public void roleDeptRelationRoundTrip() {
    roleDeptRelation.reassignDepts(OWNER_A, List.of(100L, 101L));
    assertEquals(Set.of(100L, 101L), new HashSet<>(roleDeptRelation.findDeptIdsByRoleId(OWNER_A)));

    roleDeptRelation.reassignDepts(OWNER_A, List.of(101L, 102L));
    assertEquals(Set.of(101L, 102L), new HashSet<>(roleDeptRelation.findDeptIdsByRoleId(OWNER_A)));

    roleDeptRelation.removeByRoleIds(List.of(OWNER_A));
    assertTrue(roleDeptRelation.findDeptIdsByRoleId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-菜单：建立、收敛、批量删除关联走通")
  public void roleMenuRelationRoundTrip() {
    roleMenuRelation.reassignMenus(OWNER_A, List.of(1L, 2L));
    assertEquals(Set.of(1L, 2L), new HashSet<>(roleMenuRelation.findMenuIdsByRoleId(OWNER_A)));

    roleMenuRelation.reassignMenus(OWNER_A, List.of(2L, 3L));
    assertEquals(Set.of(2L, 3L), new HashSet<>(roleMenuRelation.findMenuIdsByRoleId(OWNER_A)));

    roleMenuRelation.removeByRoleIds(List.of(OWNER_A));
    assertTrue(roleMenuRelation.findMenuIdsByRoleId(OWNER_A).isEmpty());
  }

  @Rollback
  @Test
  @DisplayName("角色-用户（反向朝向）：授权只增不删、选择性取消走通")
  public void roleUserRelationRoundTrip() {
    userRoleRelation.assignUsers(OWNER_A, List.of(OWNER_B, 990003L));
    assertEquals(Set.of(OWNER_B, 990003L), new HashSet<>(userRoleRelation.findUserIdsByRoleId(OWNER_A)));

    // add 模式只增不删
    userRoleRelation.assignUsers(OWNER_A, List.of(990003L, 990004L));
    assertEquals(Set.of(OWNER_B, 990003L, 990004L), new HashSet<>(userRoleRelation.findUserIdsByRoleId(OWNER_A)));

    userRoleRelation.unassignUsers(OWNER_A, List.of(OWNER_B));
    assertEquals(Set.of(990003L, 990004L), new HashSet<>(userRoleRelation.findUserIdsByRoleId(OWNER_A)));

    userRoleRelation.unassignUsers(OWNER_A, List.of(990003L));
    assertEquals(Set.of(990004L), new HashSet<>(userRoleRelation.findUserIdsByRoleId(OWNER_A)));
  }

}
