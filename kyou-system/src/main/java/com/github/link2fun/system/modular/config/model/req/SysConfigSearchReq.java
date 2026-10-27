package com.github.link2fun.system.modular.config.model.req;

import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;

/**
 * {@link com.github.link2fun.system.modular.config.model.SysConfig}
 * SysConfigSearchReq 查询条件; 只含列表页真正参与过滤的字段
 */
@Data
public class SysConfigSearchReq {

  /** 参数名称 */
  private String configName;

  /** 参数键名 */
  private String configKey;

  /** 参数类型 */
  private String configType;


  /** 请求参数(日期区间等); 命名固定为 params, 前端以 params[beginTime] 形式传入 */
  private BaseEntityParams params = new BaseEntityParams();
}
