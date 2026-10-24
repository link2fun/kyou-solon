package com.github.link2fun.support.constant;


/**
 * 通用常量信息
 *
 * @author ruoyi
 */
public class Constants {
  /** UTF-8 字符集 */
  public static final String UTF8 = "UTF-8";

  /** GBK 字符集 */
  public static final String GBK = "GBK";

  /** www主域 */
  public static final String WWW = "www.";

  /** http请求 */
  @SuppressWarnings("HttpUrlsUsage")
  public static final String HTTP = "http://";

  /** https请求 */
  public static final String HTTPS = "https://";

  /** 通用成功标识 */
  public static final String SUCCESS = "0";

  /** 通用失败标识 */
  public static final String FAIL = "1";

  /** 登录成功 */
  public static final String LOGIN_SUCCESS = "Success";

  /** 注销 */
  public static final String LOGOUT = "Logout";

  /** 注册 */
  public static final String REGISTER = "Register";

  /** 登录失败 */
  public static final String LOGIN_FAIL = "Error";

  /** 令牌 */
  public static final String TOKEN = "token";

  /** 会话中的当前用户 */
  public static final String SESSION_USER = "sessionUser";

  /** 资源映射路径 前缀 */
  public static final String RESOURCE_PREFIX = "/profile";
}
