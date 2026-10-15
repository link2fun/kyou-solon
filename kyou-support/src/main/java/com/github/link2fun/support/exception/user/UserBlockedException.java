package com.github.link2fun.support.exception.user;

import java.io.Serial;

/**
 * 用户停用异常类
 */
@SuppressWarnings("unused")
public class UserBlockedException extends UserException {
  @Serial
  private static final long serialVersionUID = 1L;

  public UserBlockedException() {
    super("user.blocked", null);
  }
}
