package com.github.link2fun.support.xss;

import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Result;
import org.noear.solon.validation.Validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义xss校验注解实现
 */
public class XssValidator implements Validator<Xss> {

  private static final String HTML_PATTERN = "<(\\S*?)[^>]*>.*?|<.*? />";
  private static final Pattern pattern = Pattern.compile(HTML_PATTERN);


  public static boolean containsHtml(String value) {
    StringBuilder sHtml = new StringBuilder();
    Matcher matcher = pattern.matcher(value);
    while (matcher.find()) {
      sHtml.append(matcher.group());
    }
    return pattern.matcher(sHtml).matches();
  }

  /**
   * 验证上下文
   *
   * @param ctx  上下文
   * @param anno 验证注解
   * @param name 参数名（可能没有）
   * @param tmp  临时字符构建器（用于构建 message；起到复用之效）
   * @return 验证结果
   */
  @Override
  public Result<?> validateOfContext(final Context ctx, final Xss anno, final String name, final StringBuilder tmp) {

    String value = ctx.param(name);
    if (value != null) {
      if (containsHtml(value)) {
        return Result.failure(anno.message());
      }
    }
    return Result.succeed();
  }

  /**
   * 验证值
   *
   * @param anno 验证注解
   * @param val  值
   * @param tmp  临时字符构建器（用于构建 message；起到复用之效）
   * @return 验证结果
   */
  @Override
  public Result<?> validateOfValue(final Xss anno, final Object val, final StringBuilder tmp) {
    if (val != null && !(val instanceof String)) {
      return Result.failure();
    }

    String value = (String) val;
    if (value != null) {
      if (containsHtml(value)) {
        return Result.failure();
      }
    }
    return Result.succeed();
  }
}
