package com.github.link2fun.framework.auth;

/** 请求环境捕获 */
public interface ClientInfoProvider {

  /** 捕获当前请求的客户端环境快照 */
  ClientInfo capture();
}
