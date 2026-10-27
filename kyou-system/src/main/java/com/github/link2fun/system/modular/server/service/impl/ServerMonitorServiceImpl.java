package com.github.link2fun.system.modular.server.service.impl;

import com.github.link2fun.framework.web.domain.Server;
import com.github.link2fun.system.modular.server.service.IServerMonitorService;
import org.noear.solon.annotation.Component;

/**
 * 服务器监控实现
 */
@Component
public class ServerMonitorServiceImpl implements IServerMonitorService {

  /**
   * 采集当前服务器指标
   *
   * @return 服务器指标
   * @throws Exception 采集过程中读取系统信息失败
   */
  @Override
  public Server getServerInfo() throws Exception {
    final Server server = new Server();
    server.copyTo();
    return server;
  }
}
