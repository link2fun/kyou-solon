package com.github.link2fun.schedule.modular.job.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 立即执行定时任务请求; 只有任务ID
 * {@link com.github.link2fun.schedule.modular.job.model.SysJob}
 */
@Data
public class SysJobRunReq {

  /** 任务ID */
  @NotNull(message = "任务ID不能为空")
  private Long jobId;
}
