package com.github.link2fun.tlog.interceptor;

import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Condition;
import org.noear.solon.scheduling.scheduled.Job;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobInterceptor;

/** TLog 定时任务执行前上下文设置 */
@Condition(onClass = JobInterceptor.class)
@Component
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
    try {
      tLogRPCHandler.processProviderSide(new TLogLabelBean());
      handler.handle(job.getContext());
    }finally {
      tLogRPCHandler.cleanThreadLocal();
    }
  }
}
