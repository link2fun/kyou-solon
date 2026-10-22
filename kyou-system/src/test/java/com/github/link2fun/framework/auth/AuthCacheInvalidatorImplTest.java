package com.github.link2fun.framework.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 失效图语义测试: fake 端口记录调用, 不碰 DB/Redis; 写路径的声明位置由 kyou-main 集成测试覆盖 */
class AuthCacheInvalidatorImplTest {

  private final RecordingSessionCache cache = new RecordingSessionCache();

  private final FakeAuthFacts facts = new FakeAuthFacts();

  /** 默认无事务: 立即执行 */
  private AuthCacheInvalidator invalidator() {
    return new AuthCacheInvalidatorImpl(facts, cache, new TransactionScope() {
      @Override
      public boolean active() {
        return false;
      }

      @Override
      public void afterCommit(final Runnable action) {
        throw new AssertionError("无事务时不应注册提交后动作");
      }
    });
  }

  @Test
  @DisplayName("用户变更只失效指定用户, 不牵连其他用户")
  void usersChangedInvalidatesOnlyListedUsers() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onUsersChanged(List.of(7L, 8L));

    assertEquals(List.of(List.of(7L, 8L)), cache.invalidatedUsers);
    assertTrue(cache.invalidatedRoles.isEmpty());
    assertTrue(cache.kickedUsers.isEmpty());
  }

  @Test
  @DisplayName("强制下线只登出账号, 不额外失效缓存")
  void usersKickedOnlyLogsOut() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onUsersKicked(List.of(9L));

    assertEquals(List.of(List.of(9L)), cache.kickedUsers);
    assertTrue(cache.invalidatedUsers.isEmpty());
    assertTrue(cache.invalidatedRoles.isEmpty());
  }

  @Test
  @DisplayName("角色变更失效新 roleKey、旧 roleKey 与全部持有者")
  void rolesChangedInvalidatesBothKeysAndHolders() {
    facts.roleKeysByRoleIds.put(3L, List.of("new_key"));
    facts.holdersByRoleIds.put(3L, List.of(11L, 12L));
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onRolesChanged(List.of(3L), List.of("old_key"));

    assertEquals(List.of(List.of("old_key", "new_key")), cache.invalidatedRoles);
    assertEquals(List.of(List.of(11L, 12L)), cache.invalidatedUsers);
  }

  @Test
  @DisplayName("删除角色只按传入的旧标识失效, 不查持有者")
  void rolesDeletedUsesObsoleteKeysOnly() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onRolesChanged(List.of(), List.of("gone_key"));

    assertEquals(List.of(List.of("gone_key")), cache.invalidatedRoles);
    assertTrue(cache.invalidatedUsers.isEmpty(), "无 roleIds 时不应查持有者");
    assertTrue(facts.queriedRoleIds.isEmpty(), "无 roleIds 时不应查库");
  }

  @Test
  @DisplayName("菜单变更只失效引用它的角色")
  void menusChangedInvalidatesReferencingRolesOnly() {
    facts.roleKeysByMenuIds.put(5L, List.of("ops"));
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onMenusChanged(List.of(5L));

    assertEquals(List.of(List.of("ops")), cache.invalidatedRoles);
    assertTrue(cache.invalidatedUsers.isEmpty(), "菜单变更不动用户级缓存");
  }

  @Test
  @DisplayName("没有任何角色引用菜单时不产生失效调用")
  void menusChangedWithoutRolesDoesNothing() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onMenusChanged(List.of(404L));

    assertTrue(cache.invalidatedRoles.isEmpty());
  }

  @Test
  @DisplayName("空集合与 null 入参不产生任何动作, 也不查库")
  void emptyInputsAreNoOps() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onUsersChanged(List.of());
    invalidator.onUsersKicked(null);
    invalidator.onRolesChanged(List.of(), List.of());
    invalidator.onMenusChanged(List.of());

    assertTrue(cache.isEmpty(), "空入参不应产生任何失效");
    assertTrue(facts.isEmpty(), "空入参不应查库");
  }

  @Test
  @DisplayName("集合内 null 与重复值被归一化")
  void duplicatesAndNullsAreNormalized() {
    final AuthCacheInvalidator invalidator = invalidator();

    invalidator.onUsersChanged(java.util.Arrays.asList(1L, null, 1L));

    assertEquals(List.of(List.of(1L)), cache.invalidatedUsers);
  }

  @Test
  @DisplayName("处于事务中时推迟到提交后执行")
  void defersUntilAfterCommit() {
    final List<Runnable> pending = new ArrayList<>();
    final AuthCacheInvalidator invalidator = new AuthCacheInvalidatorImpl(facts, cache, new TransactionScope() {
      @Override
      public boolean active() {
        return true;
      }

      @Override
      public void afterCommit(final Runnable action) {
        pending.add(action);
      }
    });

    invalidator.onUsersChanged(List.of(4L));

    assertTrue(cache.isEmpty(), "提交前不应执行");
    assertEquals(1, pending.size());
    pending.forEach(Runnable::run);
    assertEquals(List.of(List.of(4L)), cache.invalidatedUsers);
  }

  @Test
  @DisplayName("失效动作抛异常只记日志, 不向上抛")
  void invalidationFailureIsSwallowed() {
    final SessionCache failing = new RecordingSessionCache() {
      @Override
      public void invalidateUsers(final Collection<Long> userIds) {
        throw new IllegalStateException("redis down");
      }
    };
    final AuthCacheInvalidator invalidator = new AuthCacheInvalidatorImpl(facts, failing, new TransactionScope() {
      @Override
      public boolean active() {
        return false;
      }

      @Override
      public void afterCommit(final Runnable action) {
        throw new AssertionError("unexpected");
      }
    });

    invalidator.onUsersChanged(List.of(1L));
  }

  /** 记录失效调用的假缓存端口 */
  private static class RecordingSessionCache implements SessionCache {

    private final List<List<Long>> invalidatedUsers = new ArrayList<>();
    private final List<List<String>> invalidatedRoles = new ArrayList<>();
    private final List<List<Long>> kickedUsers = new ArrayList<>();

    @Override
    public void invalidateUsers(final Collection<Long> userIds) {
      invalidatedUsers.add(List.copyOf(userIds));
    }

    @Override
    public void invalidateRoles(final Collection<String> roleKeys) {
      invalidatedRoles.add(List.copyOf(roleKeys));
    }

    @Override
    public void kickUsers(final Collection<Long> userIds) {
      kickedUsers.add(List.copyOf(userIds));
    }

    boolean isEmpty() {
      return invalidatedUsers.isEmpty() && invalidatedRoles.isEmpty() && kickedUsers.isEmpty();
    }
  }

  /** 固定返回值的假事实端口 */
  private static class FakeAuthFacts implements AuthFacts {

    private final Map<Long, List<String>> roleKeysByRoleIds = new HashMap<>();
    private final Map<Long, List<Long>> holdersByRoleIds = new HashMap<>();
    private final Map<Long, List<String>> roleKeysByMenuIds = new HashMap<>();
    private final List<Collection<Long>> queriedRoleIds = new ArrayList<>();

    @Override
    public List<String> roleKeysByRoleIds(final Collection<Long> roleIds) {
      queriedRoleIds.add(List.copyOf(roleIds));
      return roleIds.stream().flatMap(id -> roleKeysByRoleIds.getOrDefault(id, List.of()).stream()).toList();
    }

    @Override
    public List<Long> holderUserIdsByRoleIds(final Collection<Long> roleIds) {
      return roleIds.stream().flatMap(id -> holdersByRoleIds.getOrDefault(id, List.of()).stream()).toList();
    }

    @Override
    public List<String> roleKeysReferencingMenus(final Collection<Long> menuIds) {
      return menuIds.stream().flatMap(id -> roleKeysByMenuIds.getOrDefault(id, List.of()).stream()).toList();
    }

    boolean isEmpty() {
      return queriedRoleIds.isEmpty();
    }
  }
}
