package com.github.link2fun.support.exception;

/**
 * 工具类异常
 *
 * @author ruoyi
 */
public class UtilException extends RuntimeException {

  public UtilException(Throwable e) {
    super(e.getMessage(), e);
  }

  public UtilException(String message) {
    super(message);
  }

  public UtilException(String message, Throwable throwable) {
    super(message, throwable);
  }
}
