package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * 个人信息响应：用户资料与其角色、岗位分组
 */
@Getter
@Setter
public class UserProfileResponse extends Resp<SysUserDTO> {
  /** 角色分组 */
  private String roleGroup;

  /** 岗位分组 */
  private String postGroup;
}
