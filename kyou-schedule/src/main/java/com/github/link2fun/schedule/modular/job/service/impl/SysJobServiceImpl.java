package com.github.link2fun.schedule.modular.job.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.model.req.SysJobAddReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobChangeStatusReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobModifyReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobSearchReq;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import com.github.link2fun.schedule.util.JobInvokeUtil;
import com.github.link2fun.schedule.util.ScheduleUtils;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.noear.java_cron.CronUtils;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.scheduling.ScheduledAnno;
import org.noear.solon.scheduling.scheduled.JobHolder;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class SysJobServiceImpl implements ISysJobService {

  @Inject(required = false)
  IJobManager jobManager;

  @Inject
  private ISysJobService self;

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private ISysJobLogService jobLogService;


  /**
   * 项目启动时，初始化定时器 主要是防止手动修改数据库导致未同步到定时任务处理（注：不能手动修改数据库ID和任务组名，否则会导致脏数据）
   */
  @Init
  public void init() {

    Solon.context().getBeanAsync(IJobManager.class, _jobManager -> {
      // 调用 jobManager.jobGetAll() 获取所有任务, 然后停止
      final Map<String, JobHolder> jobAll = _jobManager.jobGetAll();
      jobAll.keySet()
        .forEach(_jobManager::jobRemove);

      List<SysJob> jobList = entityQuery.queryable(SysJob.class).toList();
      for (final SysJob job : jobList) {

        final ScheduledAnno scheduled = new ScheduledAnno();
        scheduled.cron(job.getCronExpression());
        scheduled.enable(Objects.equals(job.getStatus(), UserConstants.NORMAL));

        _jobManager.jobAdd(job.getJobName(), scheduled, ctx -> {
          log.info("定时任务执行: {}", job.getJobName());
          JobInvokeUtil.invokeMethod(job);
          log.info("定时任务执行完成: {}", job.getJobName());
        });
      }
    });


  }

  /**
   * 获取quartz调度器的计划任务列表
   *
   * @param pageRequest 分页对象
   * @param searchReq   调度查询条件
   * @return 调度任务集合
   */
  @Override
  public Page<SysJob> selectJobList(final Page<SysJob> pageRequest, SysJobSearchReq searchReq) {
    EasyPageResult<SysJob> pageResult = entityQuery.queryable(SysJob.class)
      .where(job -> {
        job.jobName().like(StrUtil.isNotBlank(searchReq.getJobName()), searchReq.getJobName());
        job.jobGroup().eq(StrUtil.isNotBlank(searchReq.getJobGroup()), searchReq.getJobGroup());
        job.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        job.invokeTarget().like(StrUtil.isNotBlank(searchReq.getInvokeTarget()), searchReq.getInvokeTarget());
      })
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize(), pageRequest.getTotal());
    return Page.of(pageRequest, pageResult);
  }

  /**
   * 通过调度任务ID查询调度信息
   *
   * @param jobId 调度任务ID
   * @return 调度任务对象信息
   */
  @Override
  public SysJob selectJobById(Long jobId) {
    return entityQuery.queryable(SysJob.class)
      .where(job -> job.jobId().eq(jobId))
      .singleOrNull();
  }

  /**
   * 暂停任务
   *
   * @param jobId 任务ID
   */
  @Override
  @Transaction
  public long pauseJob(Long jobId) {
    final SysJob job = selectJobById(jobId);
    if (job == null) {
      throw new ServiceException("任务不存在或已过期");
    }
//    int rows = getBaseMapper().updateById(job);
    long rows = entityQuery.updatable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .setColumns(s -> s.status().set(ScheduleConstants.Status.PAUSE.getValue()))
      .executeRows();

    if (rows > 0) {
      jobManager.jobStop(job.getJobName());
    }
    return rows;
  }

  /**
   * 恢复任务
   *
   * @param jobId 任务ID
   */
  @Override
  @Transaction
  public long resumeJob(Long jobId) {
    final SysJob job = selectJobById(jobId);
    if (job == null) {
      throw new ServiceException("任务不存在或已过期");
    }
    long rows = entityQuery.updatable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .setColumns(s -> s.status().set(ScheduleConstants.Status.NORMAL.getValue()))
      .executeRows();
    if (rows > 0) {
      final String beanName = JobInvokeUtil.getBeanName(job.getInvokeTarget());
      final String methodName = JobInvokeUtil.getMethodName(job.getInvokeTarget());
      final List<Object[]> methodParams = JobInvokeUtil.getMethodParams(job.getInvokeTarget());
      final Class<?> beanClass = Solon.context().getBean(JobInvokeUtil.getBeanName(job.getInvokeTarget())).getClass();
      final Method[] declaredMethods = beanClass.getDeclaredMethods();
      final Method method = Arrays.stream(declaredMethods).filter(_method -> _method.getName().equals(methodName)).findFirst().orElseThrow(() -> new RuntimeException("未找到对应方法"));
      for (final Parameter parameter : method.getParameters()) {
      }
      jobManager.jobStart(job.getJobName(), null);
    }
    return rows;
  }

  /**
   * 删除任务, 同时移除调度器中的触发器
   *
   * <p>只停止后续调度, 不中断已经开始的那一次执行: 调度器用 {@code Future.cancel(false)} 取消定时器,
   * 已在跑的 handler 会自然跑完, 期间仍会访问数据库与业务资源。
   * 需要「等执行结束再删」属于运行态准入, 本方法不做该判定。
   *
   * @param jobId 任务ID
   */
  @Override
  @Transaction
  public long deleteJob(Long jobId) {
    final SysJob job = selectJobById(jobId);
    if (job == null) {
      return 0;
    }
    long rows = entityQuery.deletable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .allowDeleteStatement(true)
      .executeRows();
    if (rows > 0) {
      jobManager.jobRemove(job.getJobName());

    }
    return rows;
  }

  /**
   * 批量删除调度信息
   *
   * @param jobIds 需要删除的任务ID
   */
  @Override
  @Transaction
  public void deleteJobByIds(List<Long> jobIds) {
    for (Long jobId : jobIds) {
      deleteJob(jobId);
    }
  }

  /**
   * 任务调度状态修改
   *
   * <p>暂停与恢复各自按 jobId 取任务, 恢复还需要 invokeTarget 等完整字段。
   *
   * @param changeStatusReq 任务ID与目标状态
   */
  @Override
  @Transaction
  public long changeStatus(SysJobChangeStatusReq changeStatusReq) {
    final Long jobId = changeStatusReq.getJobId();
    final String status = changeStatusReq.getStatus();
    if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
      return resumeJob(jobId);
    }
    if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
      return pauseJob(jobId);
    }
    // 状态值非法时任务也必须存在, 与既有语义一致
    if (selectJobById(jobId) == null) {
      throw new ServiceException("任务不存在或已过期");
    }
    return 0;
  }

  /**
   * 立即运行任务
   *
   * @param jobId 任务ID
   */
  @Override
  @Transaction
  public boolean run(Long jobId) throws Throwable {
//    final SysJob sysJob = getBaseMapper().selectById(job.getJobId());
    final SysJob sysJob = entityQuery.queryable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .singleNotNull();

    final String jobName = sysJob.getJobName();
    final JobHolder jobHolder = jobManager.jobGet(jobName);

    final LocalDateTime startTime = LocalDateTime.now();

    final SysJob _job = self.jobGet(jobName);

    final SysJobLog jobLog = new SysJobLog();
//    jobLog.setJobLogId();
    jobLog.setJobName(jobName);
    jobLog.setJobGroup(_job.getJobGroup());
    jobLog.setInvokeTarget(_job.getInvokeTarget());


    try {
      jobHolder.getHandler().handle(Context.current());
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

    return true;
  }

  /**
   * 新增任务
   *
   * @param addReq 新增任务请求
   */
  @Override
  @Transaction
  public long insertJob(SysJobAddReq addReq) {
    final SysJob job = BeanUtil.toBean(addReq, SysJob.class);
    checkJobFields("新增", job);
    job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
//    int rows = getBaseMapper().insert(job);
    long rows = entityQuery.insertable(job).executeRows();
    if (rows > 0) {
      jobManager.jobAdd(job.getJobName(), new ScheduledAnno().cron(job.getCronExpression()), ctx -> {
//        log.info("定时任务执行: {}", job.getJobName());
        JobInvokeUtil.invokeMethod(job);
//        log.info("定时任务执行完成: {}", job.getJobName());
      });
    }
    return rows;
  }

  /**
   * 更新任务的时间表达式
   *
   * @param modifyReq 修改任务请求
   */
  @Override
  @Transaction
  public long updateJob(SysJobModifyReq modifyReq) {
    final SysJob job = BeanUtil.toBean(modifyReq, SysJob.class);
    checkJobFields("修改", job);
    long rows = entityQuery.updatable(job).executeRows();
    if (rows > 0) {
      updateSchedulerJob(job);
    }
    return rows;
  }

  /**
   * 更新任务
   *
   * @param job 任务对象
   */
  public void updateSchedulerJob(SysJob job) {
    // 判断是否存在
    if (jobManager.jobExists(job.getJobName())) {
      // 防止创建时存在数据问题 先移除，然后在执行创建操作
      jobManager.jobRemove(job.getJobName());
    }
    jobManager.jobAdd(job.getJobName(), new ScheduledAnno().cron(job.getCronExpression()), ctx -> {
//      log.info("定时任务执行: {}", job.getJobName());
      JobInvokeUtil.invokeMethod(job);
//      log.info("定时任务执行完成: {}", job.getJobName());
    });
  }

  /**
   * 校验cron表达式是否有效
   *
   * @param cronExpression 表达式
   * @return 结果
   */
  @Override
  public boolean checkCronExpressionIsValid(String cronExpression) {
    return CronUtils.isValid(cronExpression);
  }

  /**
   * 校验 Cron 表达式与调用目标字符串, 不合法时抛业务异常
   *
   * <p>调用目标能被反射执行任意 Bean 方法, 因此除白名单外还要逐条拦掉协议前缀,
   * 避免用 rmi/ldap/http 把定时任务变成出站通道。
   */
  private void checkJobFields(final String action, final SysJob job) {
    final String prefix = action + "任务'" + job.getJobName() + "'失败，";
    final String cronExpression = job.getCronExpression();
    final String invokeTarget = job.getInvokeTarget();
    if (!CronUtils.isValid(cronExpression)) {
      throw new ServiceException(prefix + "Cron表达式不正确");
    }
    if (StrUtil.containsIgnoreCase(invokeTarget, ScheduleConstants.LOOKUP_RMI)) {
      throw new ServiceException(prefix + "目标字符串不允许'rmi'调用");
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, ScheduleConstants.LOOKUP_LDAP, ScheduleConstants.LOOKUP_LDAPS)) {
      throw new ServiceException(prefix + "目标字符串不允许'ldap(s)'调用");
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, Constants.HTTP, Constants.HTTPS)) {
      throw new ServiceException(prefix + "目标字符串不允许'http(s)'调用");
    }
    if (StrUtil.containsAnyIgnoreCase(invokeTarget, ScheduleConstants.JOB_ERROR_STR)) {
      throw new ServiceException(prefix + "目标字符串存在违规");
    }
    if (!ScheduleUtils.whiteList(invokeTarget)) {
      throw new ServiceException(prefix + "目标字符串不在白名单内");
    }
  }

  /**
   * 根据任务名称 获取任务
   *
   * @param jobName 任务名称
   * @return 任务信息
   */
  @Override
  public SysJob jobGet(final String jobName) {
//    return lambdaQuery().eq(SysJob::getJobName, jobName).one();
    return entityQuery.queryable(SysJob.class)
      .where(s -> s.jobName().eq(jobName))
      .singleOrNull();
  }
}
