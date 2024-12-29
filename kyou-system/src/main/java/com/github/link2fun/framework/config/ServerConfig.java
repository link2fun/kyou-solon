package com.github.link2fun.framework.config;

import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;

@Component
public class ServerConfig {


  /**
   * 获取完整的请求路径，包括：域名，端口，上下文访问路径
   *
   * @return 服务地址
   */
  public String getUrl() {
    final Context context = Context.current();
    return getDomain(context);
  }

  public static String getDomain(Context context) {
    String url = context.url();
    String contextPath = Solon.cfg().serverContextPath();
    return url.substring(0, url.length() - context.path().length()) + contextPath;
  }
}
