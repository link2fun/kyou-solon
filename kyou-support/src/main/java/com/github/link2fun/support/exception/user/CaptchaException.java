package com.github.link2fun.support.exception.user;

import java.io.Serial;

/**
 * 验证码错误异常类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class CaptchaException extends UserException {
  @Serial
  private static final long serialVersionUID = 1L;

  public CaptchaException() {
    super("user.jcaptcha.error", null);
  }
}
