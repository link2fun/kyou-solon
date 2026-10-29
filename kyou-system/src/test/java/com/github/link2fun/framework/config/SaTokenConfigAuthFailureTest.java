package com.github.link2fun.framework.config;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import com.github.link2fun.support.core.domain.Resp;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 认证失败的状态码映射: 无权限 403, 未登录 401, 其余认证异常 403, 非认证异常 500。
 */
public class SaTokenConfigAuthFailureTest {

  @Test
  @DisplayName("无权限归 403")
  void notPermissionMapsTo403() {
    final Resp<Void> result = SaTokenConfig.authFailure(new NotPermissionException("system:user:list"));
    assertEquals(403, result.getCode());
    assertEquals("无此权限：system:user:list", result.getMsg());
  }

  @Test
  @DisplayName("未登录归 401")
  void notLoginMapsTo401() {
    final Resp<Void> result = SaTokenConfig.authFailure(new NotLoginException("未能读取到有效 token", "login", "token"));
    assertEquals(401, result.getCode());
    assertEquals("未能读取到有效 token", result.getMsg());
  }

  @Test
  @DisplayName("通用认证异常归 403")
  void otherSaTokenExceptionMapsTo403() {
    final Resp<Void> result = SaTokenConfig.authFailure(new SaTokenException("认证异常"));
    assertEquals(403, result.getCode());
    assertEquals("认证异常", result.getMsg());
  }

  @Test
  @DisplayName("非认证异常归 500")
  void nonAuthExceptionMapsTo500() {
    final Resp<Void> result = SaTokenConfig.authFailure(new IllegalStateException("炸了"));
    assertEquals(500, result.getCode());
    assertEquals("炸了", result.getMsg());
  }
}
