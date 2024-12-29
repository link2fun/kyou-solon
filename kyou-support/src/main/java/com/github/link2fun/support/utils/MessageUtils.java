package com.github.link2fun.support.utils;


import org.noear.solon.i18n.I18nUtil;

/**
 * 获取i18n资源文件
 *
 * @author link2fun
 */
public class MessageUtils {

  /**
   * 根据指定的代码和参数获取国际化消息。
   *
   * @param messageCode 消息代码, 用于从资源文件中获取对应的消息（模板）
   * @param args        参数列表, 用于填充占位符
   * @return 国际化消息
   */
  public static String getMessage(String messageCode, Object... args) {
    return I18nUtil.getMessage(messageCode, args);
  }
}
