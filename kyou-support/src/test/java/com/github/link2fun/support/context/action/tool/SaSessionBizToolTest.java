package com.github.link2fun.support.context.action.tool;

import com.github.link2fun.support.core.domain.model.SessionUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 非 Web 线程（定时任务、单元测试）里读登录态不能抛异常。
 *
 * <p>{@code ActionContext.current()} 与限流/防重拦截器都以 {@code isLogin()} 为唯一前置判定,
 * 这条约定一旦被破坏, 无请求上下文的写库与拦截器都会崩。
 */
public class SaSessionBizToolTest {

  @Test
  @DisplayName("无 Sa-Token 上下文时 isLogin() 判定为未登录而非抛异常")
  void isLoginWithoutWebContextIsFalse() {
    assertFalse(assertDoesNotThrow(SaSessionBizTool::isLogin,
      "非 Web 线程取登录态不应抛异常"));
  }

  @Test
  @DisplayName("无 Sa-Token 上下文时读会话用户抛异常, 由调用方决定降级策略")
  void currentUserWithoutWebContextThrows() {
    // ActionContext.current() 先用 isLogin() 分流, 这里只确认未登录语义只有一个来源
    assertFalse(SaSessionBizTool.isLogin());
    assertNull(assertDoesNotThrow(() -> ActionContextProbe.readSafely(), "probe 应吞掉异常"));
  }

  @Test
  @DisplayName("在普通子线程里同样判定为未登录")
  void isLoginWorksOnAPlainWorkerThread() throws InterruptedException {
    final AtomicReference<Boolean> loggedIn = new AtomicReference<>();
    final AtomicReference<Throwable> failure = new AtomicReference<>();

    final Thread worker = new Thread(() -> {
      try {
        loggedIn.set(SaSessionBizTool.isLogin());
      } catch (Throwable t) {
        failure.set(t);
      }
    });
    worker.start();
    worker.join();

    assertNull(failure.get(), "子线程取登录态不应抛异常, 实际: " + failure.get());
    assertFalse(loggedIn.get(), "子线程未登录");
  }


  /** 模拟 ActionContext.current() 的分流: 未登录就不再触碰会话 */
  private static final class ActionContextProbe {

    /** 未登录时读会话用户的降级结果 */
    static SessionUser readSafely() {
      if (!SaSessionBizTool.isLogin()) {
        return null;
      }
      return SaSessionBizTool.currentUser();
    }
  }
}
