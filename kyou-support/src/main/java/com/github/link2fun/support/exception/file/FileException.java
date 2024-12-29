package com.github.link2fun.support.exception.file;


import com.github.link2fun.support.exception.base.I18nBaseException;

import java.io.Serial;

/**
 * 文件信息异常类
 *
 * @author ruoyi
 */
public class FileException extends I18nBaseException {
  @Serial
  private static final long serialVersionUID = 1L;

  public FileException(String code, Object[] args) {
    super("file", code, args, null);
  }

}
