package com.github.link2fun.support.core.domain.model;

import lombok.Data;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serializable;

/**
 * 用户登录对象
 *
 * @author ruoyi
 */
@Data
public class LoginBody implements Serializable {
  /** 用户名 */
  @NotBlank(message = "用户账号不能为空")
  private String username;

  /** 用户密码 */
  @NotBlank(message = "用户密码不能为空")
  private String password;

  /** 验证码 */
  private String code;

  /** 唯一标识 */
  private String uuid;

}
