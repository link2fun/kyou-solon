package com.github.link2fun.support.exception.user;

import java.io.Serial;

/**
 * 黑名单IP异常类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class BlackListException extends UserException {
  @Serial
  private static final long serialVersionUID = 1L;

  public BlackListException() {
    super("login.blocked", null);
  }
}
