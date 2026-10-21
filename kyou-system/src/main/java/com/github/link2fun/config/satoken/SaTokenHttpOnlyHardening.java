package com.github.link2fun.config.satoken;

import cn.dev33.satoken.config.SaTokenConfig;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Init;

/**
 * sa-token Cookie 加固: 将登录 token cookie 设为 HttpOnly, 阻断脚本读取
 */
@Slf4j
@Component
public class SaTokenHttpOnlyHardening {

  /**
   * 项目启动时, 对 sa-token 全局配置开启 cookie HttpOnly
   */
  @Init
  public void init() {
    Solon.context().getBeanAsync(SaTokenConfig.class, cfg -> {
      if (cfg.getCookie() != null) {
        cfg.getCookie().setHttpOnly(true);
        // 拿到的是容器中的 SaTokenConfig bean, sa-token 插件注入的是同一实例, 改字段即全局生效
        log.info("sa-token cookie HttpOnly 已开启");
      }
    });
  }
}
