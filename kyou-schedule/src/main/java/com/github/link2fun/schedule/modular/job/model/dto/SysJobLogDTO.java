package com.github.link2fun.schedule.modular.job.model.dto;

import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务日志
 * {@link com.github.link2fun.schedule.modular.job.model.SysJobLog }
 */
@Data
public class SysJobLogDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 日志ID */
  @Excel(name = "日志序号")
  private Long jobLogId;
  /** 任务名称 */
  @Excel(name = "任务名称")
  private String jobName;
  /** 任务组名 */
  @Excel(name = "任务组名")
  private String jobGroup;
  /** 调用目标字符串 */
  @Excel(name = "调用目标字符串")
  private String invokeTarget;
  /** 日志信息 */
  @Excel(name = "日志信息")
  private String jobMessage;
  /** 执行状态 */
  @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
  private String status;
  /** 异常信息 */
  @Excel(name = "异常信息")
  private String exceptionInfo;
  /** 创建时间 */
  @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime createTime;
  /** 开始时间 */
  private LocalDateTime startTime;
  /** 停止时间 */
  private LocalDateTime stopTime;
}
