package com.github.link2fun.system.modular.user.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户授权角色响应：用户本人与可分配角色
 */
@Getter
@Setter
public class UserAuthRoleResponse extends Resp<Void> {
  /** 用户信息 */
  private SysUserDTO user;

  /** 可分配角色 */
  private List<RoleDTO> roles;
}
