package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录响应：会话令牌
 */
@Getter
@Setter
public class LoginResponse extends Resp<Void> {
  /** 会话令牌 */
  private String token;
}
