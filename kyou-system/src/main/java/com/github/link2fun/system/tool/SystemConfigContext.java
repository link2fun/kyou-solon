package com.github.link2fun.system.tool;

import com.github.link2fun.support.context.config.ConfigContext;

public class SystemConfigContext {
  
  
  /** 当前系统是否开启注册功能 */
  public static boolean accountRegisterEnabled() {
    return ConfigContext.getBoolean("sys.account.registerUser", false);
  }


  /** 验证码是否启用 */
  public static boolean accountCaptchaEnabled() {
    return ConfigContext.getBoolean("sys.account.captchaEnabled", true);
  }

  /** IP黑名单 */
  public static String loginBlackIpList() {
    return ConfigContext.getString("sys.login.blackIPList","");
  }

  public static String userInitPassword() {
    return ConfigContext.getString("sys.user.initPassword","666666");
  }
}
