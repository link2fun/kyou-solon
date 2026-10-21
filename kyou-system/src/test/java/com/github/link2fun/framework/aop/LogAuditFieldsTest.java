package com.github.link2fun.framework.aop;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 审计行里两个「脱敏管不到」的字段。
 *
 * <ul>
 *   <li>{@code oper_url}：{@code @Log(mask=true)} 只作用于参数与响应体，
 *       {@code /monitor/online/{tokenId}} 的会话凭据会原样落库，读日志的人可重放接管会话；</li>
 *   <li>{@code error_msg}：{@code StackOverflowError} 没有 message，直接写库等于留空。</li>
 * </ul>
 */
class LogAuditFieldsTest {

  @Test
  @DisplayName("路径里的会话 token 打码并保留前后各 4 位，普通 ID 与动作名保持原样")
  void masksTokenInPath() {
    String token = "8f14e45fceea167a5a36dedd4bea2543";

    String masked = LogInterceptor.maskPathSecrets("/monitor/online/" + token);
    assertFalse(masked.contains(token), masked);
    // 保留前 4 后 4：够核对是哪一次会话，token 本体不可还原
    assertEquals("/monitor/online/8f14************************2543", masked);

    // 短段不受影响：数字 ID、动作名、列表端点
    assertEquals("/system/dept/100", LogInterceptor.maskPathSecrets("/system/dept/100"));
    assertEquals("/system/user/changeStatus", LogInterceptor.maskPathSecrets("/system/user/changeStatus"));
    assertEquals("/monitor/online/list", LogInterceptor.maskPathSecrets("/monitor/online/list"));
  }

  @Test
  @DisplayName("JWT 形态的 token 同样打码并保留前后各 4 位")
  void masksJwtToken() {
    String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJsb2dpbklkIjoxfQ.abc123";

    String masked = LogInterceptor.maskPathSecrets("/api/session/" + jwt);
    assertFalse(masked.contains(jwt), masked);
    assertTrue(masked.startsWith("/api/session/eyJh"), masked);
    assertTrue(masked.endsWith("c123"), masked);
  }

  @Test
  @DisplayName("空路径安全")
  void handlesEmptyPath() {
    assertNull(LogInterceptor.maskPathSecrets(null));
    assertEquals("", LogInterceptor.maskPathSecrets(""));
  }

  @Test
  @DisplayName("没有 message 的 Error 用类名兜底，审计行不留空白")
  void errorMessageFallsBackToClassName() {
    assertEquals("StackOverflowError", LogInterceptor.errorMessageOf(new StackOverflowError()));
    assertEquals("OutOfMemoryError", LogInterceptor.errorMessageOf(new OutOfMemoryError()));
    assertEquals("boom", LogInterceptor.errorMessageOf(new IllegalStateException("boom")));
  }
}
