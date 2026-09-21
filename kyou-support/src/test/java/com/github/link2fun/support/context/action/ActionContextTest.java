package com.github.link2fun.support.context.action;

import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 非 Web 线程（定时任务、单元测试）里取操作上下文不能抛异常。
 *
 * <p>EasyQuery 审计拦截器按 {@code Optional.ofNullable(ActionContext.current())...orElse("system")}
 * 的契约调用 —— 未登录时必须返回空上下文；这条约定一旦被破坏，定时任务写库就会崩溃。
 */
public class ActionContextTest {

  @Test
  @DisplayName("无 Sa-Token 上下文时 current() 返回空上下文而非抛异常")
  void currentWithoutWebContextReturnsEmptyContext() {
    final ActionContext context = assertDoesNotThrow(ActionContext::current,
      "非 Web 线程取上下文不应抛异常");

    assertNotNull(context, "应返回空上下文而不是 null, 调用方无需判空");
    assertNull(context.getUserId(), "未登录时用户ID应为空");
    assertNull(context.getSessionUser(), "未登录时不应有会话用户");
  }

  @Test
  @DisplayName("空上下文上的派生查询返回空值而非抛异常")
  void emptyContextAnswersQueriesSafely() {
    final ActionContext context = ActionContext.current();

    assertDoesNotThrow(context::isSuperAdmin);
    assertDoesNotThrow(context::getRoles);
    assertSame(false, context.isSuperAdmin(), "未登录不应被判为超管");
    assertSame(0, context.getRoles().size(), "未登录的角色列表应为空");
  }

  @Test
  @DisplayName("索要未登录用户时给出 401")
  void demandingCurrentUserWithoutLoginIsUnauthorized() {
    final ActionContext context = ActionContext.current();

    final ServiceException e = assertThrows(ServiceException.class, context::getCurrentUserNotNull);
    assertEquals(HttpStatus.UNAUTHORIZED, e.getCode(), "未登录应回 401 而不是 500");
    assertEquals("获取用户信息异常", e.getMessage());
  }

  @Test
  @DisplayName("在普通子线程里同样不抛异常")
  void currentWorksOnAPlainWorkerThread() throws InterruptedException {
    final AtomicReference<ActionContext> captured = new AtomicReference<>();
    final AtomicReference<Throwable> failure = new AtomicReference<>();

    final Thread worker = new Thread(() -> {
      try {
        captured.set(ActionContext.current());
      } catch (Throwable t) {
        failure.set(t);
      }
    });
    worker.start();
    worker.join();

    assertNull(failure.get(), "子线程取上下文不应抛异常, 实际: " + failure.get());
    assertNotNull(captured.get());
    assertNull(captured.get().getUserId());
  }
}
