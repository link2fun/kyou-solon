package com.github.link2fun.schedule.modular.job.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.schedule.modular.job.engine.JobBinding;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.dto.SysJobDTO;
import com.github.link2fun.schedule.modular.job.model.req.SysJobAddReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobChangeStatusReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobModifyReq;
import com.github.link2fun.schedule.modular.job.model.req.SysJobSearchReq;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import com.github.link2fun.schedule.util.JobInvokeUtil;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.noear.java_cron.CronUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SysJobServiceImpl implements ISysJobService {

  @Inject
  private JobBinding jobBinding;

  @Db
  private EasyEntityQuery entityQuery;


  /** 项目启动时初始化定时器, 同步手动改库造成的漂移; 单行脏数据(如非法 cron)只跳过该行并记日志, 不阻断启动 */
  @Init
  public void init() {
    jobBinding.clearAll();
    for (final SysJob job : entityQuery.queryable(SysJob.class).toList()) {
      try {
        jobBinding.bind(job);
      } catch (Exception e) {
        jobBinding.unbind(job.getJobName());
        log.warn("[定时任务] 启动期绑定失败, 已跳过: jobName={}, cron={}", job.getJobName(), job.getCronExpression(), e);
      }
    }
  }

  /**
   * 获取quartz调度器的计划任务列表
   *
   * @param pageRequest 分页对象
   * @param searchReq   调度查询条件
   * @return 调度任务集合
   */
  @Override
  public <T> Page<T> selectJobList(final Page<T> pageRequest, SysJobSearchReq searchReq, final Class<T> resultClass) {
    EasyPageResult<T> pageResult = entityQuery.queryable(SysJob.class)
      .where(job -> {
        job.jobName().like(StrUtil.isNotBlank(searchReq.getJobName()), searchReq.getJobName());
        job.jobGroup().eq(StrUtil.isNotBlank(searchReq.getJobGroup()), searchReq.getJobGroup());
        job.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        job.invokeTarget().like(StrUtil.isNotBlank(searchReq.getInvokeTarget()), searchReq.getInvokeTarget());
      })
      .selectAutoInclude(resultClass)
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
  public <T> T selectJobById(Long jobId, final Class<T> resultClass) {
    return entityQuery.queryable(SysJob.class)
      .where(job -> job.jobId().eq(jobId))
      .selectAutoInclude(resultClass)
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
    final SysJobDTO job = selectJobById(jobId, SysJobDTO.class);
    if (job == null) {
      throw new ServiceException("任务不存在或已过期");
    }
    long rows = entityQuery.updatable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .setColumns(s -> s.status().set(ScheduleConstants.Status.PAUSE.getValue()))
      .executeRows();

    if (rows > 0) {
      jobBinding.stop(job.getJobName());
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
    final SysJobDTO job = selectJobById(jobId, SysJobDTO.class);
    if (job == null) {
      throw new ServiceException("任务不存在或已过期");
    }
    long rows = entityQuery.updatable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .setColumns(s -> s.status().set(ScheduleConstants.Status.NORMAL.getValue()))
      .executeRows();
    if (rows > 0) {
      jobBinding.start(job.getJobName());
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
    final SysJobDTO job = selectJobById(jobId, SysJobDTO.class);
    if (job == null) {
      return 0;
    }
    long rows = entityQuery.deletable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .allowDeleteStatement(true)
      .executeRows();
    if (rows > 0) {
      jobBinding.unbind(job.getJobName());

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
    if (selectJobById(jobId, SysJobDTO.class) == null) {
      throw new ServiceException("任务不存在或已过期");
    }
    return 0;
  }

  /**
   * 立即运行任务: 任务不存在时抛业务异常, 计时与日志由拦截器统一记录
   *
   * @param jobId 任务ID
   */
  @Override
  @Transaction
  public void run(Long jobId) throws Throwable {
    final SysJob sysJob = entityQuery.queryable(SysJob.class)
      .where(s -> s.jobId().eq(jobId))
      .singleOrNull();
    if (sysJob == null) {
      throw new ServiceException("任务不存在或已过期！");
    }

    jobBinding.triggerNow(sysJob.getJobName());
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
    if (StrUtil.isBlank(job.getJobGroup())) {
      // 库里落 NULL 会让列表的组筛选漏掉该行, 先归一到默认组
      job.setJobGroup(ScheduleConstants.DEFAULT_GROUP);
    }
    checkJobFields("新增", job);
    checkJobNameUnique("新增", job);
    job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
    long rows = entityQuery.insertable(job).executeRows();
    if (rows > 0) {
      jobBinding.bind(job);
    }
    return rows;
  }

  /**
   * 更新任务定义: 按旧名+旧组解绑再重绑(旧键 holder 不会自行消失), 只写提交的列, 空组名视为未提交保持库中原组
   *
   * @param modifyReq 修改任务请求
   */
  @Override
  @Transaction
  public long updateJob(SysJobModifyReq modifyReq) {
    final SysJob job = BeanUtil.toBean(modifyReq, SysJob.class);
    if (StrUtil.isBlank(job.getJobGroup())) {
      job.setJobGroup(null);
    }
    checkJobFields("修改", job);
    checkJobNameUnique("修改", job);
    // 改名前的行: 旧键的 holder 不会自行消失, 需要按旧名解绑
    final SysJob current = entityQuery.queryable(SysJob.class)
      .where(s -> s.jobId().eq(job.getJobId()))
      .singleOrNull();
    if (current == null) {
      return 0;
    }
    // 只写请求提交的列: 未带 status 时库里状态保持不变, 不会被写成 NULL
    long rows = entityQuery.updatable(job)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
    if (rows > 0) {
      // bind 自带同名自愈, 这里只需把旧名字的注册清掉
      jobBinding.unbind(current.getJobName());
      jobBinding.bind(entityQuery.queryable(SysJob.class)
        .where(s -> s.jobId().eq(job.getJobId()))
        .singleNotNull());
    }
    return rows;
  }

  /** 校验 Cron 表达式与调用目标(判据统一走 {@link JobInvokeUtil#violation}), 杜绝「存得下、跑不了」 */
  private void checkJobFields(final String action, final SysJob job) {
    final String prefix = action + "任务'" + job.getJobName() + "'失败，";
    final String cronExpression = job.getCronExpression();
    final String invokeTarget = job.getInvokeTarget();
    if (!CronUtils.isValid(cronExpression)) {
      throw new ServiceException(prefix + "Cron表达式不正确");
    }
    final String violation = JobInvokeUtil.violation(invokeTarget);
    if (violation != null) {
      throw new ServiceException(prefix + violation);
    }
  }

  /**
   * 根据任务名称获取任务(任务名全局唯一)
   *
   * @param jobName 任务名称
   * @return 任务信息
   */
  @Override
  public SysJob jobGet(final String jobName) {
    return entityQuery.queryable(SysJob.class)
      .where(s -> s.jobName().eq(jobName))
      .singleOrNull();
  }

  /** 校验任务名全局唯一: 引擎键即任务名, 重名行会让点查抛多行异常、绑定互相顶掉 */
  private void checkJobNameUnique(final String action, final SysJob job) {
    UniqueChecker.checkOccupied(entityQuery.queryable(SysJob.class)
        .where(_job -> {
          _job.jobName().eq(job.getJobName());
          _job.jobId().ne(Objects.nonNull(job.getJobId()), job.getJobId());
        }).any(),
      () -> action + "任务'" + job.getJobName() + "'失败，任务名称已存在");
  }
}
