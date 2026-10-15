package com.github.link2fun.framework.auth;

import com.github.link2fun.support.core.domain.model.LoginBody;
import com.github.link2fun.support.core.domain.model.RegisterBody;

/**
 * 登录与注册的准入守卫: 验证码、凭证与前置规则在此校验。
 */
public interface LoginGuard {

  /**
   * 登录: 验证码 → 前置检查 → 凭证校验 → 建立会话
   *
   * @return 会话 token
   */
  String login(LoginBody loginBody);

  /**
   * 注销当前会话并记录审计
   */
  void logout();

  /**
   * 注册: 注册开关 → 验证码 → 账号规则 → 落库 → 审计, 失败一律抛异常
   */
  void register(RegisterBody registerBody);

  /**
   * 解锁登录: 清除指定账号的密码重试计数(管理端操作)
   */
  void unlock(String username);
}
