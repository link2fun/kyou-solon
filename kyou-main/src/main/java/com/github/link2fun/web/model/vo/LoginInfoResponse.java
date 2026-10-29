package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 登录信息响应：用户资料、角色标识与权限码
 */
@Getter
@Setter
public class LoginInfoResponse extends Resp<Void> {
  /** 用户资料 */
  private SysUserDTO user;

  /** 角色标识 */
  private List<String> roles;

  /** 权限码 */
  private List<String> permissions;
}
