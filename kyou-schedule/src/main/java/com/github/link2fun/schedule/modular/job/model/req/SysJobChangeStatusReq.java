package com.github.link2fun.schedule.modular.job.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 修改定时任务状态请求; 只有任务ID与状态
 * {@link com.github.link2fun.schedule.modular.job.model.SysJob}
 */
@Data
public class SysJobChangeStatusReq {

  /** 任务ID */
  @NotNull(message = "任务ID不能为空")
  private Long jobId;

  /** 任务状态（0正常 1暂停） */
  @NotBlank(message = "任务状态不能为空")
  private String status;
}
