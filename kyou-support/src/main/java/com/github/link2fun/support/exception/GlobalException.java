package com.github.link2fun.support.exception;

import com.github.link2fun.support.exception.base.I18nBaseException;
import lombok.Getter;

import java.io.Serial;

/**
 * 全局异常
 *
 * @author ruoyi
 */
@Getter
public class GlobalException extends I18nBaseException {
  @Serial
  private static final long serialVersionUID = 1L;

  public static final String MODULE_NAME = "global";

  /** 默认错误编码 */
  public static final String DEFAULT_ERROR_CODE = "500";

  /** 错误提示 */
  private String message;

  /** 错误明细，内部调试错误 */
  @Getter
  private String detailMessage;

  /** 空构造方法，避免反序列化问题 */
  public GlobalException() {
    super(MODULE_NAME, DEFAULT_ERROR_CODE, null, "");
  }

  public GlobalException(String message) {
    super(message);
  }

  public GlobalException withDetailMessage(String detailMessage) {
    this.detailMessage = detailMessage;
    return this;
  }


  public GlobalException withMessage(String message) {
    this.message = message;
    return this;
  }
}