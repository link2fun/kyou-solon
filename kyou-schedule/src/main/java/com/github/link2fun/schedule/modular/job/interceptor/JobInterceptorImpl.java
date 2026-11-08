package com.github.link2fun.schedule.modular.job.interceptor;

import cn.hutool.core.exceptions.ExceptionUtil;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.scheduled.Job;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobInterceptor;

import java.time.LocalDateTime;
import java.util.Objects;

@Component(index = -99)
public class JobInterceptorImpl implements JobInterceptor {

  @Inject
  private ISysJobService jobService;

  @Inject
  private ISysJobLogService jobLogService;

  public JobInterceptorImpl() {
  }

  JobInterceptorImpl(final ISysJobService jobService, final ISysJobLogService jobLogService) {
    this.jobService = jobService;
    this.jobLogService = jobLogService;
  }

  /**
   * 任务执行计时与日志审计统一在此收口; 定时触发与手动 triggerNow 走同一条链
   *
   * @param job     任务
   * @param handler 处理器
   */
  @Override
  public void doIntercept(final Job job, final JobHandler handler) throws Throwable {

    // 引擎键即任务名, 按名直取行
    final String jobName = job.getName();
    final SysJob _job = jobService.jobGet(jobName);
    if (Objects.isNull(_job)) {
      // 非 DB 来源的任务(如 @Scheduled 注解)没有 sys_job 行, 放行不记日志
      handler.handle(job.getContext());
      return;
    }

    final LocalDateTime startTime = LocalDateTime.now();

    final SysJobLog jobLog = new SysJobLog();
    jobLog.setJobName(jobName);
    jobLog.setJobGroup(_job.getJobGroup());
    jobLog.setInvokeTarget(_job.getInvokeTarget());

    try {
      handler.handle(job.getContext());
      jobLog.setJobMessage("任务执行成功");
      jobLog.setStatus("0");
      jobLog.setCreateTime(startTime);
    } catch (Exception e) {
      jobLog.setJobMessage(e.getMessage());
      jobLog.setStatus("1");
      jobLog.setExceptionInfo(ExceptionUtil.unwrap(e).toString());
      jobLog.setCreateTime(startTime);
    } finally {
      // 记录日志
      jobLogService.addJobLog(jobLog);
    }

  }
}
