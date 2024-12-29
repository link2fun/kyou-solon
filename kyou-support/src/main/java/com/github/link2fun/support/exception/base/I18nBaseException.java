package com.github.link2fun.support.exception.base;


import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.utils.MessageUtils;
import lombok.Getter;

import java.io.Serial;

/**
 * 基础异常
 *
 * @author ruoyi
 */
@Getter
@SuppressWarnings("unused")
public class I18nBaseException extends BaseException {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 所属模块 */
  private final String module;

  /** Restful API 的响应码 */
  private final int apiCode = HttpStatus.ERROR;

  /** 错误码 */
  private final String i18nCode;

  /** 错误码对应的参数 */
  private final Object[] i18nArgs;

  /** 错误消息 */
  private final String defaultMessage;

  public I18nBaseException(String module, String i18nCode, Object[] i18nArgs, String defaultMessage) {
    this.module = module;
    this.i18nCode = i18nCode;
    this.i18nArgs = i18nArgs;
    this.defaultMessage = defaultMessage;
  }

  public I18nBaseException(String module, String code, Object[] i18nArgs) {
    this(module, code, i18nArgs, null);
  }

  public I18nBaseException(String module, String defaultMessage) {
    this(module, null, null, defaultMessage);
  }

  public I18nBaseException(String code, Object[] i18nArgs) {
    this(null, code, i18nArgs, null);
  }

  public I18nBaseException(String defaultMessage) {
    this(null, null, null, defaultMessage);
  }

  @Override
  public String getMessage() {
    String message = null;
    if (StrUtil.isNotEmpty(i18nCode)) {
      message = MessageUtils.getMessage(i18nCode, i18nArgs);
    }
    if (message == null) {
      message = defaultMessage;
    }
    return message;
  }

}
