package com.github.link2fun.system.modular.user.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 修改用户状态请求; 只有用户ID与状态, 不接受其它列
 * {@link com.github.link2fun.support.core.domain.entity.SysUser}
 */
@Data
public class SysUserChangeStatusReq {

  /** 用户ID */
  @NotNull(message = "用户ID不能为空")
  private Long userId;

  /** 状态（0正常 1停用） */
  @NotBlank(message = "状态不能为空")
  private String status;
}
