package com.github.link2fun.framework.auth;

import java.util.Collection;

/**
 * 会话缓存写端口: 让哪份缓存作废, 不关心 sa-token 的键格式。
 */
public interface SessionCache {

  /** 失效用户的会话缓存, 下次请求从库重建; 不在线时跳过 */
  void invalidateUsers(Collection<Long> userIds);

  /** 失效角色的共享权限缓存, 对全部持有者生效 */
  void invalidateRoles(Collection<String> roleKeys);

  /** 强退账号的全部在线会话, 旧 token 立即不可用 */
  void kickUsers(Collection<Long> userIds);
}
