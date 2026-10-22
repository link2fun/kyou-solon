package com.github.link2fun.framework.auth;

import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import org.noear.solon.annotation.Component;

import java.util.Collection;

/**
 * SessionCache 的 sa-token 实现: 转发 {@link SaSessionBizTool}。
 */
@Component
public class SaTokenSessionCache implements SessionCache {

  /** 失效指定用户的会话缓存, 不在线时跳过 */
  @Override
  public void invalidateUsers(final Collection<Long> userIds) {
    userIds.forEach(SaSessionBizTool::invalidateUser);
  }

  /** 失效指定角色的共享权限缓存 */
  @Override
  public void invalidateRoles(final Collection<String> roleKeys) {
    roleKeys.forEach(SaSessionBizTool::invalidateRole);
  }

  /** 强退指定账号的全部在线会话 */
  @Override
  public void kickUsers(final Collection<Long> userIds) {
    userIds.forEach(SaSessionBizTool::logoutByLoginId);
  }
}
