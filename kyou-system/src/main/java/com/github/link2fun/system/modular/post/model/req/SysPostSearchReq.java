package com.github.link2fun.system.modular.post.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * SysPostSearchReq 查询条件; 只含列表页真正参与过滤的字段
 * {@link com.github.link2fun.support.core.domain.entity.SysPost}
 */
@Data
public class SysPostSearchReq {

  /** 岗位编码 */
  private String postCode;

  /** 岗位名称 */
  private String postName;

  /** 状态（0正常 1停用） */
  private String status;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
