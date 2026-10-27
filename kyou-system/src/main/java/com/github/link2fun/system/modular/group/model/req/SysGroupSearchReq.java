package com.github.link2fun.system.modular.group.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.system.modular.group.model.SysGroup}
 * SysGroupSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysGroupSearchReq {

  /** 用户组ID */
  private Long groupId;

  /** 用户组名称 */
  private String groupName;

  /** 状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
