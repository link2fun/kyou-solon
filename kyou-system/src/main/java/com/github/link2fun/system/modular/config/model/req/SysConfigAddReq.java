package com.github.link2fun.system.modular.config.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

/**
 * {@link com.github.link2fun.system.modular.config.model.SysConfig}
 * 新增参数配置请求
 */
@Data
public class SysConfigAddReq {

  /** 参数名称 */
  @NotBlank(message = "参数名称不能为空")
  private String configName;

  /** 参数键名 */
  @NotBlank(message = "参数键名不能为空")
  private String configKey;

  /** 参数键值 */
  @NotBlank(message = "参数键值不能为空")
  private String configValue;

  /** 备注 */
  private String remark;

}
