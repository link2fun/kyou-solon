package com.github.link2fun.schedule.modular.job.service;

import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.job.TaskException;


import java.util.List;


public interface ISysJobService  {

  /**
   * 获取quartz调度器的计划任务
   *
   * @param page
   * @param searchReq 调度信息
   * @return 调度任务集合
   */
  Page<SysJob> selectJobList(final Page<SysJob> page, SysJob searchReq);

  /**
   * 通过调度任务ID查询调度信息
   *
   * @param jobId 调度任务ID
   * @return 调度任务对象信息
   */
  SysJob selectJobById(Long jobId);

  /**
   * 暂停任务
   *
   * @param job 调度信息
   * @return 结果
   */
  long pauseJob(SysJob job);

  /**
   * 恢复任务
   *
   * @param job 调度信息
   * @return 结果
   */
  long resumeJob(SysJob job);

  /**
   * 删除任务后，所对应的trigger也将被删除
   *
   * @param job 调度信息
   * @return 结果
   */
  long deleteJob(SysJob job);

  /**
   * 批量删除调度信息
   *
   * @param jobIds 需要删除的任务ID
   */
  void deleteJobByIds(List<Long> jobIds);

  /**
   * 任务调度状态修改
   *
   * @param job 调度信息
   * @return 结果
   */
  long changeStatus(SysJob job);

  /**
   * 立即运行任务
   *
   * @param job 调度信息
   * @return 结果
   */
  boolean run(SysJob job) throws Throwable;

  /**
   * 新增任务
   *
   * @param job 调度信息
   * @return 结果
   */
  long insertJob(SysJob job) throws TaskException;

  /**
   * 更新任务
   *
   * @param job 调度信息
   * @return 结果
   */
  long updateJob(SysJob job) throws TaskException;

  /**
   * 校验cron表达式是否有效
   *
   * @param cronExpression 表达式
   * @return 结果
   */
  boolean checkCronExpressionIsValid(String cronExpression);


  /**
   * 根据任务名称 获取任务
   * @param jobName 任务名称
   * @return 任务信息
   */
  SysJob jobGet(String jobName);
}
