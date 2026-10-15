package com.github.link2fun.system.tool;

import cn.hutool.core.convert.Convert;
import com.github.link2fun.support.context.config.service.IConfigHolder;
import lombok.Setter;
import org.noear.solon.Solon;

/**
 * 系统参数的读取入口: 配置键与缺省值在此集中定义。
 */
public class SystemConfigContext {

  /** 账号自助-验证码开关 */
  private static final String KEY_ACCOUNT_CAPTCHA_ENABLED = "sys.account.captchaEnabled";
  /** 账号自助-是否开启用户注册功能 */
  private static final String KEY_ACCOUNT_REGISTER_USER = "sys.account.registerUser";
  /** 用户登录-黑名单列表: 多个匹配项以 ; 分隔, 支持 * 通配与网段 */
  private static final String KEY_LOGIN_BLACK_IP_LIST = "sys.login.blackIPList";
  /** 用户管理-账号初始密码 */
  private static final String KEY_USER_INIT_PASSWORD = "sys.user.initPassword";

  /** 验证码开关缺省值: 开启 */
  private static final boolean DEFAULT_ACCOUNT_CAPTCHA_ENABLED = true;
  /** 注册开关缺省值: 关闭 */
  private static final boolean DEFAULT_ACCOUNT_REGISTER_ENABLED = false;
  /** 黑名单缺省值: 空, 不拦截任何 IP */
  private static final String DEFAULT_LOGIN_BLACK_IP_LIST = "";
  /** 初始密码缺省值 */
  private static final String DEFAULT_USER_INIT_PASSWORD = "666666";


  @Setter
  private static IConfigHolder configHolder;

  static {
    if (Solon.context() != null) {
      Solon.context().getBeanAsync(IConfigHolder.class, holder -> configHolder = holder);
    }
  }

  /** 验证码是否启用 */
  public static boolean accountCaptchaEnabled() {
    return Convert.toBool(holder().get(KEY_ACCOUNT_CAPTCHA_ENABLED), DEFAULT_ACCOUNT_CAPTCHA_ENABLED);
  }

  /** 当前系统是否开启注册功能 */
  public static boolean accountRegisterEnabled() {
    return Convert.toBool(holder().get(KEY_ACCOUNT_REGISTER_USER), DEFAULT_ACCOUNT_REGISTER_ENABLED);
  }

  /** IP黑名单列表, 多个匹配项以 ; 分隔 */
  public static String loginBlackIpList() {
    return Convert.toStr(holder().get(KEY_LOGIN_BLACK_IP_LIST), DEFAULT_LOGIN_BLACK_IP_LIST);
  }

  /** 账号初始密码 */
  public static String userInitPassword() {
    return Convert.toStr(holder().get(KEY_USER_INIT_PASSWORD), DEFAULT_USER_INIT_PASSWORD);
  }

  /** 取已接线的配置持有器 */
  private static IConfigHolder holder() {
    if (configHolder == null) {
      throw new IllegalStateException("SystemConfigContext 未接线: 容器未启动, 且未调用 setConfigHolder");
    }
    return configHolder;
  }
}
