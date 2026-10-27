package com.github.link2fun.generator.domain.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * GenTableSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class GenTableSearchReq {

  /** 表名称 */
  private String tableName;

  /** 表描述 */
  private String tableComment;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
