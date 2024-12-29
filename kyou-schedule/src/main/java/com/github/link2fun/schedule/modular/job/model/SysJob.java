package com.github.link2fun.schedule.modular.job.model;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.link2fun.schedule.modular.job.model.proxy.SysJobProxy;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.constant.ScheduleConstants;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.utils.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.java_cron.CronUtils;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.text.ParseException;
import java.util.Date;


/**
 * 定时任务调度表 sys_job
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table(value = "sys_job")
@EntityProxy
public class SysJob extends BaseEntity implements ProxyEntityAvailable<SysJob, SysJobProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 任务ID */
  @Excel(name = "任务序号", cellType = Excel.ColumnType.NUMERIC)
  @Column(value = "job_id", primaryKey = true, generatedKey = true)
  private Long jobId;

  /** 任务名称 */
  @Excel(name = "任务名称")
  @NotBlank(message = "任务名称不能为空")
  @Length(max = 64, message = "任务名称不能超过64个字符")
  @Column(value = "job_name")
  private String jobName;

  /** 任务组名 */
  @Excel(name = "任务组名")
  @Column(value = "job_group")
  private String jobGroup;

  /** 调用目标字符串 */
  @Excel(name = "调用目标字符串")
  @NotBlank(message = "调用目标字符串不能为空")
  @Length(min = 0, max = 500, message = "调用目标字符串长度不能超过500个字符")
  @Column(value = "invoke_target")
  private String invokeTarget;

  /** cron执行表达式 */
  @Excel(name = "执行表达式")
  @NotBlank(message = "Cron执行表达式不能为空")
  @Length(min = 0, max = 255, message = "Cron执行表达式不能超过255个字符")
  @Column(value = "cron_expression")
  private String cronExpression;

  /** cron计划策略 */
  @Excel(name = "计划策略 ", readConverterExp = "0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行")
  @Column(value = "misfire_policy")
  private String misfirePolicy = ScheduleConstants.MISFIRE_DEFAULT;

  /** 是否并发执行（0允许 1禁止） */
  @Excel(name = "并发执行", readConverterExp = "0=允许,1=禁止")
  @Column(value = "concurrent")
  private String concurrent;

  /** 任务状态（0正常 1暂停） */
  @Excel(name = "任务状态", readConverterExp = "0=正常,1=暂停")
  @Column(value = "status")
  private String status;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  public Date getNextValidTime() {
    if (StringUtils.isNotEmpty(cronExpression)) {
      try {
        CronUtils.getNextTime(cronExpression, new Date());
      } catch (ParseException e) {
        return null;
      }
    }
    return null;
  }

}
