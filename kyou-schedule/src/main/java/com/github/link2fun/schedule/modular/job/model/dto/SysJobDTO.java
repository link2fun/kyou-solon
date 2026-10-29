package com.github.link2fun.schedule.modular.job.model.dto;

import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 定时任务响应
 * {@link com.github.link2fun.schedule.modular.job.model.SysJob }
 */
@Data
public class SysJobDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 任务ID */
  @Excel(name = "任务序号", cellType = Excel.ColumnType.NUMERIC)
  private Long jobId;
  /** 任务名称 */
  @Excel(name = "任务名称")
  private String jobName;
  /** 任务组名 */
  @Excel(name = "任务组名")
  private String jobGroup;
  /** 调用目标字符串 */
  @Excel(name = "调用目标字符串")
  private String invokeTarget;
  /** cron执行表达式 */
  @Excel(name = "执行表达式")
  private String cronExpression;
  /** 计划执行错误策略 */
  @Excel(name = "计划策略 ", readConverterExp = "0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行")
  private String misfirePolicy = ScheduleConstants.MISFIRE_DEFAULT;
  /** 是否并发执行 */
  @Excel(name = "并发执行", readConverterExp = "0=允许,1=禁止")
  private String concurrent;
  /** 状态 */
  @Excel(name = "任务状态", readConverterExp = "0=正常,1=暂停")
  private String status;
  /** 备注 */
  private String remark;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
