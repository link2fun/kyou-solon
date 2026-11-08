package com.github.link2fun.tlog.interceptor;

import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.data.tran.TranManager;
import org.noear.solon.scheduling.scheduled.Job;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobInterceptor;

/** TLog 定时任务执行前上下文设置 */
@Condition(onClass = JobInterceptor.class)
@Component(index = -1000)
public class TLogJobInterceptor implements JobInterceptor {


  private final TLogRPCHandler tLogRPCHandler = new TLogRPCHandler();

  /**
   * 拦截: 保证任务在干净的执行环境里跑
   *
   * @param job     任务
   * @param handler 处理器
   */
  @Override
  public void doIntercept(final Job job, final JobHandler handler) throws Throwable {
    if (Context.current() == job.getContext()) {
      // triggerNow 手动执行: 就在请求线程上同步跑, 沿用请求上下文
      doInvoke(job, handler);
      return;
    }
    // 定时触发: 执行线程来自共享池, 可能残留其他请求(如恢复任务的 changeStatus)的事务与上下文;
    // 残留事务会让 easy-query 复用该事务已关闭的连接(Connection is closed)。挂起后执行, 结束恢复原状
    TranManager.with(null, () ->
      Context.currentWith(ContextEmpty.create(), () -> doInvoke(job, handler)));
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
