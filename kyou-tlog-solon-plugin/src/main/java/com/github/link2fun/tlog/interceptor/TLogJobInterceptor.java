package com.github.link2fun.tlog.interceptor;

import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;
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
    if (Context.current() == null) {
      // solon 4.x 移除 ContextHolder，改用 currentWith 作用域式设置，执行完自动恢复
      Context.currentWith(ContextEmpty.create(), () -> doInvoke(job, handler));
    } else {
      doInvoke(job, handler);
    }
  }

  private void doInvoke(final Job job, final JobHandler handler) throws Throwable {
    try {
      tLogRPCHandler.processProviderSide(new TLogLabelBean());
      handler.handle(job.getContext());
    } finally {
      tLogRPCHandler.cleanThreadLocal();
    }
  }
}
