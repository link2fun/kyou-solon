package com.github.link2fun.support.exception;

import java.io.Serial;

/**
 * 演示模式异常
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class DemoModeException extends RuntimeException {
  @Serial
  private static final long serialVersionUID = 1L;

  public DemoModeException() {
  }
}
