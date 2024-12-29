package com.github.link2fun.support.utils;

import cn.hutool.core.convert.Convert;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.SessionState;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 客户端工具类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class ServletUtils {
  /**
   * 获取String参数
   */
  public static String getParameter(String name) {
    return Context.current().param(name);
  }

  /**
   * 获取String参数
   */
  public static String getParameter(String name, String defaultValue) {
    return Convert.toStr(Context.current().param(name), defaultValue);
  }

  /**
   * 获取Integer参数
   */
  public static Integer getParameterToInt(String name) {
    return Convert.toInt(Context.current().param(name));
  }

  /**
   * 获取Integer参数
   */
  public static Integer getParameterToInt(String name, Integer defaultValue) {
    return Convert.toInt(Context.current().param(name), defaultValue);
  }

  /**
   * 获取Boolean参数
   */
  public static Boolean getParameterToBool(String name) {
    return Convert.toBool(Context.current().param(name));
  }

  /**
   * 获取Boolean参数
   */
  public static Boolean getParameterToBool(String name, Boolean defaultValue) {
    return Convert.toBool(Context.current().param(name), defaultValue);
  }

  /**
   * 获得所有请求参数
   *
   * @param request 请求对象{@link Context}
   * @return Map
   */
  public static Map<String, List<String>> getParams(Context request) {
    final Map<String, List<String>> map = request.paramMap().toValuesMap();
    return Collections.unmodifiableMap(map);
  }


  /**
   * 获取request
   */
  public static Object getRequest() {

    return Context.current().request();
  }

  /**
   * 获取response
   */
  public static Object getResponse() {
    return Context.current().response();
  }

  /**
   * 获取session
   */
  public static SessionState getSession() {
    return Context.current().sessionState();
  }


  /**
   * 将字符串渲染到客户端
   *
   * @param context 渲染对象
   * @param string  待渲染的字符串
   */
  public static void renderString(Context context, String string) {
    context.status(200);
    context.contentType("application/json");
    context.charset("utf-8");
    context.bodyNew(string);
  }

  /**
   * 是否是Ajax异步请求
   */
  public static boolean isAjaxRequest() {
    Context context = Context.current();

    String accept = context.header("accept");
    if (accept != null && accept.contains("application/json")) {
      return true;
    }

    String xRequestedWith = context.header("X-Requested-With");
    if (xRequestedWith != null && xRequestedWith.contains("XMLHttpRequest")) {
      return true;
    }

    String uri = context.url();
    if (StringUtils.inStringIgnoreCase(uri, ".json", ".xml")) {
      return true;
    }

    String ajax = context.param("__ajax");
    return StringUtils.inStringIgnoreCase(ajax, "json", "xml");
  }

  /**
   * 内容编码
   *
   * @param str 内容
   * @return 编码后的内容
   */
  public static String urlEncode(String str) {
    return URLEncoder.encode(str, StandardCharsets.UTF_8);
  }

  /**
   * 内容解码
   *
   * @param str 内容
   * @return 解码后的内容
   */
  public static String urlDecode(String str) {
    return URLDecoder.decode(str, StandardCharsets.UTF_8);
  }
}
