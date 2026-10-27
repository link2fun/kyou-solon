package com.github.link2fun.system.modular.server.service;

import com.github.link2fun.framework.web.domain.Server;

/**
 * 服务器监控: 采集 CPU/内存/磁盘/JVM 指标
 */
public interface IServerMonitorService {

  /**
   * 采集当前服务器指标
   *
   * <p>CPU 使用率需要两次采样, 调用会阻塞约一秒。
   *
   * @return 服务器指标
   * @throws Exception 采集过程中读取系统信息失败
   */
  Server getServerInfo() throws Exception;
}
