package com.github.link2fun.framework.auth;

import com.github.link2fun.system.modular.operlog.model.SysOperLog;

/**
 * 审计事件的异步落库入口: 登录日志与操作日志
 */
public interface AuditSink {

  /**
   * 记录一条登录/注销/注册审计事件
   *
   * @param username 用户名
   * @param event    事件类型(Constants.LOGIN_SUCCESS / LOGIN_FAIL / LOGOUT / REGISTER)
   * @param success  事件结果
   * @param message  消息
   */
  void loginEvent(String username, String event, boolean success, String message);

  /**
   * 记录一条操作日志, 补全操作地点后异步落库
   */
  void operEvent(SysOperLog operLog);

  /**
   * 停机时等待在途任务排空
   */
  void shutdown();
}
