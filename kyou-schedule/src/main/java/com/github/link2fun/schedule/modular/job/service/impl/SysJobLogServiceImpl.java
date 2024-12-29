package com.github.link2fun.schedule.modular.job.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.support.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SysJobLogServiceImpl implements ISysJobLogService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 获取quartz调度器日志的计划任务
   *
   * @param searchReq 调度日志信息
   * @return 调度任务日志集合
   */
  @Override
  public List<SysJobLog> selectJobLogList(SysJobLog searchReq) {

    return entityQuery.queryable(SysJobLog.class)
      .where(jobLog -> {
        jobLog.jobName().like(StrUtil.isNotBlank(searchReq.getJobName()), searchReq.getJobName());
        jobLog.jobGroup().eq(StrUtil.isNotBlank(searchReq.getJobGroup()), searchReq.getJobGroup());
        jobLog.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        jobLog.invokeTarget().like(StrUtil.isNotBlank(searchReq.getInvokeTarget()), searchReq.getInvokeTarget());
        jobLog.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        jobLog.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());
      })
      .toList();
  }

  /**
   * 通过调度任务日志ID查询调度信息
   *
   * @param jobLogId 调度任务日志ID
   * @return 调度任务日志对象信息
   */
  @Override
  public SysJobLog selectJobLogById(Long jobLogId) {
//    return getBaseMapper().selectById(jobLogId);
    return entityQuery.queryable(SysJobLog.class)
      .whereById(jobLogId)
      .singleOrNull();
  }

  /**
   * 新增任务日志
   *
   * @param jobLog 调度日志信息
   */
  @Override
  public void addJobLog(SysJobLog jobLog) {
//    getBaseMapper().insert(jobLog);
    entityQuery.insertable(jobLog).executeRows();
  }

  /**
   * 批量删除调度日志信息
   *
   * @param logIds 需要删除的数据ID
   * @return 结果
   */
  @Override
  public long deleteJobLogByIds(List<Long> logIds) {
//    return getBaseMapper().deleteBatchIds(logIds);
    return entityQuery.deletable(SysJobLog.class)
      .whereByIds(logIds)
      .allowDeleteStatement(true)
      .executeRows();
  }

  /**
   * 删除任务日志
   *
   * @param jobId 调度日志ID
   */
  @Override
  public long deleteJobLogById(Long jobId) {
    return entityQuery.deletable(SysJobLog.class)
      .allowDeleteStatement(true)
      .whereById(jobId)
      .executeRows();
  }

  /**
   * 清空任务日志
   */
  @Override
  public void cleanJobLog() {
    entityQuery.sqlExecute("truncate table sys_job_log");
  }

  @Override
  public Page<SysJobLog> pageSearchJobLog(final Page<SysJobLog> page, final SysJobLog searchReq) {

    EasyPageResult<SysJobLog> pageResult = entityQuery.queryable(SysJobLog.class)
      .where(jobLogQuery -> {
        jobLogQuery.jobName().like(StrUtil.isNotBlank(searchReq.getJobName()), searchReq.getJobName());
        jobLogQuery.jobGroup().eq(StrUtil.isNotBlank(searchReq.getJobGroup()), searchReq.getJobGroup());
        jobLogQuery.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        jobLogQuery.invokeTarget().like(StrUtil.isNotBlank(searchReq.getInvokeTarget()), searchReq.getInvokeTarget());
        jobLogQuery.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        jobLogQuery.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());
      })
      .orderBy(jobLog -> jobLog.jobLogId().desc())
      .toPageResult(page.getPageNum(), page.getPageSize());
    return Page.of(pageResult);
  }
}
