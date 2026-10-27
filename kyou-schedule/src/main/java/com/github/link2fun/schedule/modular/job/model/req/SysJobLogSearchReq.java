package com.github.link2fun.schedule.modular.job.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.schedule.modular.job.model.SysJobLog}
 * SysJobLogSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysJobLogSearchReq {

  /** 任务名称 */
  private String jobName;

  /** 任务组名 */
  private String jobGroup;

  /** 调用目标字符串 */
  private String invokeTarget;

  /** 执行状态（0正常 1失败） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
