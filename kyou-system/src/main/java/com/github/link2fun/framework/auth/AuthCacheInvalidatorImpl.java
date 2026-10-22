package com.github.link2fun.framework.auth;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 失效图的唯一裁定点: 声明 → 查事实 → 提交后执行。
 *
 * <p>事实在声明时刻读取(可见本事务变更、关联行尚在), 失效动作挂 afterCommit; 无事务立即执行。
 */
@Slf4j
@Component
public class AuthCacheInvalidatorImpl implements AuthCacheInvalidator {

  @Inject
  private AuthFacts facts;

  @Inject
  private SessionCache sessionCache;

  private TransactionScope transactionScope = SolonTransactionScope.INSTANCE;

  public AuthCacheInvalidatorImpl() {
  }

  AuthCacheInvalidatorImpl(final AuthFacts facts, final SessionCache sessionCache,
                           final TransactionScope transactionScope) {
    this.facts = facts;
    this.sessionCache = sessionCache;
    this.transactionScope = transactionScope;
  }

  /** 失效这些用户的会话缓存 */
  @Override
  public void onUsersChanged(final Collection<Long> userIds) {
    final List<Long> ids = normalizeIds(userIds);
    if (ids.isEmpty()) {
      return;
    }
    schedule("失效用户会话", ids, () -> sessionCache.invalidateUsers(ids));
  }

  /** 强退这些账号的全部在线会话 */
  @Override
  public void onUsersKicked(final Collection<Long> userIds) {
    final List<Long> ids = normalizeIds(userIds);
    if (ids.isEmpty()) {
      return;
    }
    schedule("强退用户会话", ids, () -> sessionCache.kickUsers(ids));
  }

  /** 角色变更: 失效新旧 roleKey 与全部持有者; roleIds 为空(删除场景)只按旧标识失效 */
  @Override
  public void onRolesChanged(final Collection<Long> roleIds, final Collection<String> obsoleteRoleKeys) {
    final List<Long> ids = normalizeIds(roleIds);
    final List<String> roleKeys = new ArrayList<>(normalizeKeys(obsoleteRoleKeys));
    final List<Long> holders = new ArrayList<>();

    if (!ids.isEmpty()) {
      roleKeys.addAll(facts.roleKeysByRoleIds(ids));
      holders.addAll(facts.holderUserIdsByRoleIds(ids));
    }

    final List<String> distinctKeys = normalizeKeys(roleKeys);
    final List<Long> distinctHolders = normalizeIds(holders);
    if (distinctKeys.isEmpty() && distinctHolders.isEmpty()) {
      return;
    }
    schedule("失效角色与持有者会话", distinctKeys, () -> {
      if (!distinctKeys.isEmpty()) {
        sessionCache.invalidateRoles(distinctKeys);
      }
      if (!distinctHolders.isEmpty()) {
        sessionCache.invalidateUsers(distinctHolders);
      }
    });
  }

  /** 菜单变更: 失效引用了这些菜单的角色 */
  @Override
  public void onMenusChanged(final Collection<Long> menuIds) {
    final List<Long> ids = normalizeIds(menuIds);
    if (ids.isEmpty()) {
      return;
    }
    final List<String> roleKeys = normalizeKeys(facts.roleKeysReferencingMenus(ids));
    if (roleKeys.isEmpty()) {
      return;
    }
    schedule("失效菜单关联角色会话", roleKeys, () -> sessionCache.invalidateRoles(roleKeys));
  }

  /** 事务内注册提交后执行, 无事务立即执行 */
  private void schedule(final String action, final Object target, final Runnable effect) {
    if (transactionScope.active()) {
      try {
        transactionScope.afterCommit(() -> run(action, target, effect));
        return;
      } catch (IllegalStateException e) {
        // 判定与注册之间事务已结束: 退化为立即执行
        log.warn("[会话失效] 事务已结束, 改为立即执行: action={}, target={}", action, target);
      }
    }
    run(action, target, effect);
  }

  /** Solon 的 afterCommit 会吞掉监听器异常, 这里自行兜底让脏缓存可观测 */
  private void run(final String action, final Object target, final Runnable effect) {
    try {
      effect.run();
    } catch (Exception e) {
      log.error("[会话失效] {} 失败, 缓存可能陈旧至下次登录: target={}", action, target, e);
    }
  }

  /** 去重并剔除 null */
  private static List<Long> normalizeIds(final Collection<Long> ids) {
    if (CollectionUtil.isEmpty(ids)) {
      return List.of();
    }
    return ids.stream().filter(Objects::nonNull).distinct().toList();
  }

  /** 去重、剔除空白 */
  private static List<String> normalizeKeys(final Collection<String> keys) {
    if (CollectionUtil.isEmpty(keys)) {
      return List.of();
    }
    return keys.stream()
      .filter(Objects::nonNull)
      .filter(key -> !key.isBlank())
      .distinct()
      .toList();
  }
}
