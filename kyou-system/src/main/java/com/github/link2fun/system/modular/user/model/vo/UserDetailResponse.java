package com.github.link2fun.system.modular.user.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysPostDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRoleListDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 用户详情响应：编辑页需要的可分配角色、岗位与已关联 id
 */
@Getter
@Setter
public class UserDetailResponse extends Resp<SysUserDTO> {
  /** 可分配角色 */
  private List<SysRoleListDTO> roles;

  /** 岗位列表 */
  private List<SysPostDTO> posts;

  /** 已分配的岗位 id */
  private List<String> postIds;

  /** 已分配的角色 id */
  private List<String> roleIds;
}
