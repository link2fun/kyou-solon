package com.github.link2fun.schedule.modular.job.interceptor;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.scheduling.scheduled.Job;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobInterceptor;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;

import java.time.LocalDateTime;
import java.util.Date;

@Slf4j
@Component(index = -99)
public class JobInterceptorImpl implements JobInterceptor {

  @Inject
  private ISysJobService jobService;

  @Inject
  private ISysJobLogService jobLogService;



  /**
   * 拦截
   *
   * @param job     任务
   * @param handler 处理器
   */
  @Override
  public void doIntercept(final Job job, final JobHandler handler) throws Throwable {

    final LocalDateTime startTime = LocalDateTime.now();

    final String jobName = job.getName();
    final SysJob _job = jobService.jobGet(jobName);

    final SysJobLog jobLog = new SysJobLog();
//    jobLog.setJobLogId();
    jobLog.setJobName(jobName);
    jobLog.setJobGroup(_job.getJobGroup());
    jobLog.setInvokeTarget(_job.getInvokeTarget());


    try {
      handler.handle(job.getContext());
      jobLog.setJobMessage("任务执行成功");
      jobLog.setStatus("0");
      jobLog.setCreateTime(startTime);
      jobLog.setStartTime(startTime);
      jobLog.setStopTime(LocalDateTime.now());
    } catch (Exception e) {
      jobLog.setJobMessage(e.getMessage());
      jobLog.setStatus("1");
      jobLog.setExceptionInfo(ExceptionUtil.unwrap(e).toString());
      jobLog.setCreateTime(startTime);
      jobLog.setStartTime(startTime);
      jobLog.setStopTime(LocalDateTime.now());
    } finally {
      // 记录日志
      jobLogService.addJobLog(jobLog);
    }


  }
}
