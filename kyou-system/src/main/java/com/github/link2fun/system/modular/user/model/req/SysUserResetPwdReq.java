package com.github.link2fun.system.modular.user.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

/**
 * 重置密码请求; 只有用户ID与新密码, 不接受其它列
 * {@link com.github.link2fun.support.core.domain.entity.SysUser}
 */
@Data
public class SysUserResetPwdReq {

  /** 用户ID */
  @NotNull(message = "用户ID不能为空")
  private Long userId;

  /** 新密码(明文, 由服务端加密) */
  @NotBlank(message = "新密码不能为空")
  private String password;
}
