package com.github.link2fun.framework.aop;

import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.enums.BusinessType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 控制台请求日志的 URL 行。
 *
 * <p>入库的 {@code oper_url} 早已走 {@code maskPathSecrets}，控制台这一行曾经漏掉：
 * {@code /monitor/online/{tokenId}} 的会话凭据会明文进 stdout / docker logs，
 * 读日志的人同样可以重放接管会话。这里钉住两处同口径。
 */
class LogConsoleRequestLogTest {

  private final LogInterceptor interceptor = new LogInterceptor();

  /** 仅用于提供带 @Log 的方法与注解 */
  @Log(title = "在线用户", businessType = BusinessType.FORCE)
  @SuppressWarnings("unused")
  void forceLogoutLikeMethod() {
  }

  @Test
  @DisplayName("控制台 URL 行里的会话 token 与入库同口径打码")
  void masksTokenInConsoleUrl() throws Exception {
    String token = "8f14e45fceea167a5a36dedd4bea2543";

    String log = requestLog("DELETE", "/monitor/online/" + token);

    assertFalse(log.contains(token), log);
    assertTrue(log.contains("URL\t: DELETE /monitor/online/8f14************************2543"), log);
  }

  @Test
  @DisplayName("普通 ID 与动作名在控制台 URL 行保持原样")
  void keepsShortPathSegments() throws Exception {
    assertTrue(requestLog("GET", "/system/dept/100").contains("URL\t: GET /system/dept/100"));
    assertTrue(requestLog("PUT", "/system/user/changeStatus")
      .contains("URL\t: PUT /system/user/changeStatus"));
  }

  /** 在构造出的 Context 下拼一次控制台请求日志 */
  private String requestLog(String method, String path) throws Exception {
    Method target = LogConsoleRequestLogTest.class.getDeclaredMethod("forceLogoutLikeMethod");
    Log apiLog = target.getAnnotation(Log.class);
    Context context = new ContextEmpty() {
      @Override
      public String method() {
        return method;
      }

      @Override
      public String path() {
        return path;
      }
    };
    return Context.currentWith(context, () -> interceptor.buildRequestLog(target, apiLog)).toString();
  }
}
