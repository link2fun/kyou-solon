package com.github.link2fun.framework.auth;

import com.github.link2fun.support.utils.ip.IpUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;

@Component
public class SolonClientInfoProvider implements ClientInfoProvider {

  /** 从 Solon 请求上下文取 userAgent 与 IP */
  @Override
  public ClientInfo capture() {
    final Context context = Context.current();
    if (context == null) {
      return new ClientInfo("", "");
    }
    return new ClientInfo(context.userAgent(), IpUtils.getIpAddr());
  }
}
