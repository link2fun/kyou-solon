package com.github.link2fun.support.context.action.service;

import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;

import java.util.List;

/** ActionContext Service 用于加载上下文所需的信息 */
public interface ActionContextService {

  /** 获取某个用户拥有的角色 */
  List<RoleDTO> roleListByUserId(Long userId);


  /** 获取某个角色拥有的菜单权限 */
  List<String> menuPermissionListByRoleKey(String roleKey);


  /** 获取某个用户的信息 */
  SysUserDTO userByUserId(Long userId);


}
