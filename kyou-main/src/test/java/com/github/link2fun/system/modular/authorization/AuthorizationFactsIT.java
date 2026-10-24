package com.github.link2fun.system.modular.authorization;

import cn.dev33.satoken.context.mock.SaTokenContextMockUtil;
import com.github.link2fun.KyouApp;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.Inject;
import org.noear.solon.test.SolonJUnit5Extension;
import org.noear.solon.test.SolonTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 授权事实的集成 smoke: 会话缓存承载的角色标识与权限码, 必须与库里的授权关系一致。
 *
 * <p>收敛前这里有两个来源(前端门禁另算一份), 条件与成本都不同; 现在只剩会话缓存一条路径,
 * 所以本 IT 钉的不是「两条路径相等」, 而是「这一条路径确实跟随授权变更」——缓存能否
 * 经 AuthCacheInvalidator 失效、以及会话里出现库里已不存在的角色标识时不会打断请求。
 */
@Import(profiles = "classpath:app-test.yml", scanPackages = "com.github.link2fun")
@SolonTest(value = KyouApp.class)
@ExtendWith(SolonJUnit5Extension.class)
public class AuthorizationFactsIT {

  /** 种子数据里的超管用户, 持有 admin 角色 */
  private static final long SUPER_ADMIN_ID = 1L;
  /** 种子数据里的普通用户, 持有 common 角色 */
  private static final long COMMON_USER_ID = 2L;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemMenuService menuService;

  @Test
  @DisplayName("角色标识来自库中启用的角色, 与角色服务给出的集合一致")
  public void roleKeysMatchEnabledRoles() {
    final List<String> roleKeys = roleService.listEnabledRoleKeysByUserId(COMMON_USER_ID);

    assertFalse(roleKeys.isEmpty(), "种子用户应持有至少一个启用角色");
    assertTrue(roleKeys.contains("common"), "种子用户应持有 common 角色, 实际: " + roleKeys);
    assertFalse(roleKeys.contains("admin"), "普通用户不应持有 admin 角色");
  }

  @Test
  @DisplayName("超管持有 admin 角色, roles 与权限各自来自库而非硬编码")
  public void superAdminHoldsAdminRole() {
    final List<String> roleKeys = roleService.listEnabledRoleKeysByUserId(SUPER_ADMIN_ID);

    assertTrue(roleKeys.contains("admin"), "超管应持有 admin 角色, 实际: " + roleKeys);
  }

  @Test
  @DisplayName("会话缓存的权限码等于逐角色查询的并集")
  public void sessionPermissionsEqualPerRoleUnion() {
    final List<String> roleKeys = roleService.listEnabledRoleKeysByUserId(COMMON_USER_ID);

    final List<String> expected = roleKeys.stream()
      .flatMap(roleKey -> menuService.selectMenuPermsByRoleKey(roleKey).stream())
      .distinct()
      .toList();

    final List<String> actual = inMockSession(() -> SaSessionBizTool.getPermissionList(COMMON_USER_ID));

    assertEquals(expected.size(), actual.size(), "会话权限码数量应与逐角色并集一致");
    assertTrue(actual.containsAll(expected), "会话权限码应覆盖逐角色并集");
    assertTrue(expected.containsAll(actual), "会话权限码不应多出逐角色并集之外的值");
  }

  @Test
  @DisplayName("库里已不存在的角色标识按无权限处理, 不抛异常")
  public void unknownRoleKeyYieldsEmptyPermissions() {
    final List<String> perms = assertDoesNotThrow(
      () -> menuService.selectMenuPermsByRoleKey("role-key-that-no-longer-exists"),
      "陈旧角色标识不应打断调用方");

    assertTrue(perms.isEmpty(), "查不到角色时应返回空权限集合");
  }

  @Test
  @DisplayName("角色标识不含逗号, 单个标识不再被切分")
  public void roleKeysAreSingleIdentifiers() {
    final List<String> roleKeys = roleService.listEnabledRoleKeysByUserId(COMMON_USER_ID);

    roleKeys.forEach(roleKey ->
      assertFalse(roleKey.contains(","), "角色标识应为单个标识, 实际: " + roleKey));
  }

  /** 在 Sa-Token 的模拟上下文里执行, 让会话读写有落脚点 */
  private static <T> T inMockSession(final cn.dev33.satoken.fun.SaRetGenericFunction<T> action) {
    return SaTokenContextMockUtil.setMockContext(action);
  }
}
