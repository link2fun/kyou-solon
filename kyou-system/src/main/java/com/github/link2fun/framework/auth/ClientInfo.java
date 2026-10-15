package com.github.link2fun.framework.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** 请求环境快照 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientInfo {

  private String userAgent;

  private String ip;
}
