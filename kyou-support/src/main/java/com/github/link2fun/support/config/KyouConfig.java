package com.github.link2fun.support.config;

import org.noear.solon.Solon;

public class KyouConfig {

  private static KyouProperties cfg;

  static {
    Solon.context().getBeanAsync(KyouProperties.class, cfg -> KyouConfig.cfg = cfg);
  }

  public static String profile() {
    return cfg.getProfile();
  }

  /** 是否开启地址转换开关 */
  public static Boolean addressEnabled() {
    return cfg.getAddressEnabled();
  }


}
