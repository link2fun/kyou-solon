package com.github.link2fun.support.utils;

/**
 * 处理并记录日志文件
 *
 * @author ruoyi
 */
public class LogUtils {

  /** 把值套上方括号, null 视为空串 */
  public static String bracket(Object msg) {
    if (msg == null) {
      msg = "";
    }
    return "[" + msg + "]";
  }
}
