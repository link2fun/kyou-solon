package com.github.link2fun.schedule.modular.job.model;

import com.easy.query.core.annotation.*;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.schedule.modular.job.model.proxy.SysJobLogProxy;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.QueryEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.Date;


/**
 * 定时任务调度日志表 sys_job_log
 *
 * @author ruoyi
 */
@Setter
@Getter
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@EasyAlias("jobLog")
@Table(value = "sys_job_log")
public class SysJobLog extends QueryEntity implements ProxyEntityAvailable<SysJobLog, SysJobLogProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** ID */
  @Excel(name = "日志序号")
  @Column(value = "job_log_id",primaryKey = true)
  private Long jobLogId;

  /** 任务名称 */
  @Excel(name = "任务名称")
  @Column(value = "job_name")
  private String jobName;

  /** 任务组名 */
  @Excel(name = "任务组名")
  @Column(value = "job_group")
  private String jobGroup;

  /** 调用目标字符串 */
  @Excel(name = "调用目标字符串")
  @Column(value = "invoke_target")
  private String invokeTarget;

  /** 日志信息 */
  @Excel(name = "日志信息")
  @Column(value = "job_message")
  private String jobMessage;

  /** 执行状态（0正常 1失败） */
  @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
  @Column(value = "status")
  private String status;

  /** 异常信息 */
  @Excel(name = "异常信息")
  @Column(value = "exception_info")
  private String exceptionInfo;


  /** 创建时间 */
  @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Column(value = "create_time")
  private LocalDateTime createTime;

  /** 开始时间 */
  @ColumnIgnore
  private LocalDateTime startTime;

  /** 停止时间 */
  @ColumnIgnore
  private LocalDateTime stopTime;

}
