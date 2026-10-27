package com.github.link2fun.schedule.modular.job.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;

/**
 * {@link com.github.link2fun.schedule.modular.job.model.SysJob}
 * 新增定时任务请求
 */
@Data
public class SysJobAddReq {

  /** 任务名称 */
  @NotBlank(message = "任务名称不能为空")
  @Length(max = 64, message = "任务名称不能超过64个字符")
  private String jobName;

  /** 任务组名 */
  private String jobGroup;

  /** 调用目标字符串 */
  @NotBlank(message = "调用目标字符串不能为空")
  @Length(min = 0, max = 500, message = "调用目标字符串长度不能超过500个字符")
  private String invokeTarget;

  /** cron执行表达式 */
  @NotBlank(message = "Cron执行表达式不能为空")
  @Length(min = 0, max = 255, message = "Cron执行表达式不能超过255个字符")
  private String cronExpression;

  /** cron计划策略 */
  private String misfirePolicy;

  /** 是否并发执行（0允许 1禁止） */
  private String concurrent;

  /** 任务状态（0正常 1暂停） */
  private String status;

  /** 备注 */
  private String remark;

}
