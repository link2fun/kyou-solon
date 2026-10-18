package com.github.link2fun.support.context.action.service;

import com.github.link2fun.support.core.domain.dto.SysUserDTO;

import java.util.List;

/** ActionContext Service 用于加载上下文所需的信息 */
public interface ActionContextService {

  /** 获取某个用户持有的角色标识列表(仅启用角色) */
  List<String> roleKeyListByUserId(Long userId);


  /** 获取某个角色拥有的菜单权限 */
  List<String> menuPermissionListByRoleKey(String roleKey);


  /** 获取某个用户的信息 */
  SysUserDTO userByUserId(Long userId);


}
