package com.github.link2fun.support.exception.user;


import com.github.link2fun.support.exception.base.I18nBaseException;

import java.io.Serial;

/**
 * 用户信息异常类
 *
 * @author ruoyi
 */
public class UserException extends I18nBaseException {
  @Serial
  private static final long serialVersionUID = 1L;

  public UserException(String code, Object[] args) {
    super("user", code, args, null);
  }
}
