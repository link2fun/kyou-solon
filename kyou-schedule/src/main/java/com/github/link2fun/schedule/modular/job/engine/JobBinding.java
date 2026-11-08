package com.github.link2fun.schedule.modular.job.engine;

import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.util.JobInvokeUtil;
import com.github.link2fun.support.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.scheduling.ScheduledAnno;
import org.noear.solon.scheduling.scheduled.JobHolder;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;

import java.util.ArrayList;
import java.util.Objects;

/**
 * 调度绑定
 */
@Slf4j
@Component
public class JobBinding {

  @Inject
  private IJobManager jobManager;

  public JobBinding() {
  }

  JobBinding(final IJobManager jobManager) {
    this.jobManager = jobManager;
  }

  /** 按任务行注册; 同名先移除再注册(引擎同名复用旧 holder, 新 cron 与 handler 会被丢弃), 暂停任务注册后立即停掉(运行期 jobAdd 无条件启动) */
  public void bind(final SysJob job) {
    final String key = job.getJobName();
    if (jobManager.jobExists(key)) {
      // AbstractJobManager.jobAdd 同键时复用旧 holder, 新 handler 会被静默丢弃
      jobManager.jobRemove(key);
    }
    final boolean enabled = Objects.equals(job.getStatus(), ScheduleConstants.Status.NORMAL.getValue());
    jobManager.jobAdd(key, new ScheduledAnno()
      .cron(job.getCronExpression())
      .enable(enabled), ctx -> {
      log.info("定时任务执行: name={}, group={}", job.getJobName(), job.getJobGroup());
      JobInvokeUtil.invokeMethod(job);
      log.info("定时任务执行完成: name={}, group={}", job.getJobName(), job.getJobGroup());
    });
    if (!enabled) {
      // 引擎未 started 时这里是安全空操作; 运行期则抵消 jobAdd 的自动启动
      jobManager.jobStop(key);
    }
  }

  /** 幂等解绑: 键不在引擎里也不报错, 供重新绑定与删除自愈 */
  public void unbind(final String jobName) {
    jobManager.jobRemove(jobName);
  }

  /** 启动已注册任务, 未注册说明库与引擎漂移 */
  public void start(final String jobName) {
    requireBound(jobName);
    jobManager.jobStart(jobName, null);
  }

  /** 停止已注册任务, 未注册说明库与引擎漂移 */
  public void stop(final String jobName) {
    requireBound(jobName);
    jobManager.jobStop(jobName);
  }

  /** 立即执行一次: 走 JobHolder.handle 的拦截器链, 计时与日志由 JobInterceptorImpl 统一记录; 传 current() 供 TLogJobInterceptor 以引用相等识别手动执行、沿用请求事务 */
  public void triggerNow(final String jobName) throws Throwable {
    final JobHolder holder = jobManager.jobGet(jobName);
    if (Objects.isNull(holder)) {
      throw unbound(jobName);
    }
    holder.handle(Context.current());
  }

  /** 清空引擎里已注册的全部任务: 先拷贝快照再删, 引擎的 jobGetAll 是活视图, 边遍历边删会 CME */
  public void clearAll() {
    for (final String name : new ArrayList<>(jobManager.jobGetAll().keySet())) {
      jobManager.jobRemove(name);
    }
  }

  private void requireBound(final String jobName) {
    if (!jobManager.jobExists(jobName)) {
      throw unbound(jobName);
    }
  }

  private ServiceException unbound(final String jobName) {
    return new ServiceException("定时任务'" + jobName + "'未注册到调度引擎, 库与引擎不一致, 请重启应用或重新编辑该任务");
  }
}
