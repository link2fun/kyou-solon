package com.github.link2fun.tlog.interceptor;

import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.core.handle.ContextHolder;
import org.noear.solon.scheduling.scheduled.Job;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobInterceptor;

/** TLog 定时任务执行前上下文设置 */
@Condition(onClass = JobInterceptor.class)
@Component(index = -1000)
public class TLogJobInterceptor implements JobInterceptor {


  private final TLogRPCHandler tLogRPCHandler = new TLogRPCHandler();

  /**
   * 拦截
   *
   * @param job     任务
   * @param handler 处理器
   */
  @Override
  public void doIntercept(final Job job, final JobHandler handler) throws Throwable {
    // 为非 Web 场景（如定时任务）补充最小可用的 Solon Context，便于 Sa-Token 获取上下文
    boolean setEmptyContext = false;
    if (Context.current() == null) {
      ContextHolder.currentSet(ContextEmpty.create());
      setEmptyContext = true;
    }

    try {
      tLogRPCHandler.processProviderSide(new TLogLabelBean());
      handler.handle(job.getContext());
    } finally {
      tLogRPCHandler.cleanThreadLocal();
      // 清理临时设置的 Context，避免泄露到后续任务
      if (setEmptyContext) {
        ContextHolder.currentRemove();
      }
    }
  }
}
