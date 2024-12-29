package com.github.link2fun.framework.context.action;

import com.github.link2fun.support.context.action.service.ActionContextService;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActionContextServiceImpl implements ActionContextService {

  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemMenuService menuService;

  /**
   * 根据用户ID查询角色列表
   *
   * @param userId 用户ID
   * @return 角色DTO列表
   */
  @Override
  public List<RoleDTO> roleListByUserId(Long userId) {

    return roleService.selectRolesByUserId(userId)
        .stream().filter(RoleDTO::getFlag) // 过滤掉当前用户未启用的角色
        .collect(Collectors.toList());
  }

  /**
   * 根据角色标识查询菜单权限列表
   *
   * @param roleKey 角色标识
   * @return 菜单权限字符串列表
   */
  @Override
  public List<String> menuPermissionListByRoleKey(String roleKey) {
    return menuService.selectMenuPermsByRoleKey(roleKey);
  }

  /**
   * 根据用户ID查询用户信息
   *
   * @param userId 用户ID
   * @return 用户DTO对象
   */
  @Override
  public SysUserDTO userByUserId(Long userId) {
    return userService.selectUserById(userId);
  }
}
