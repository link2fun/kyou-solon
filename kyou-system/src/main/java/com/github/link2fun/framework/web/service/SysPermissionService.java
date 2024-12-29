package com.github.link2fun.framework.web.service;

import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户权限处理
 *
 * @author ruoyi
 */
@Component
public class SysPermissionService {
  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemMenuService menuService;

  /**
   * 获取角色数据权限
   *
   * @param userId 用户信息
   * @return 角色权限信息
   */
  public Set<String> getRolePermission(Long userId) {
    Set<String> roles = new HashSet<>();
    // 管理员拥有所有权限
    if (SysUser.isAdmin(userId)) {
      roles.add("admin");
    } else {
      roles.addAll(roleService.selectRolePermissionByUserId(userId));
    }
    return roles;
  }

  /**
   * 获取菜单数据权限
   *
   * @param user 用户信息
   * @return 菜单权限信息
   */
  public List<String> getMenuPermission(SysUserDTO user) {
    List<String> perms = Lists.newArrayList();
    // 管理员拥有所有权限
    if (user.isAdmin()) {
      perms.add("*:*:*");
    } else {
      List<RoleDTO> roles = user.getRoles();
      if (!CollectionUtils.isEmpty(roles)) {
        // 多角色设置permissions属性，以便数据权限匹配权限
        for (RoleDTO role : roles) {
          List<String> rolePerms = menuService.selectMenuPermsByRoleId(role.getRoleId());
          role.setPermissions(rolePerms);
          perms.addAll(rolePerms);
        }
      } else {
        perms.addAll(menuService.selectMenuPermsByUserId(user.getUserId()));
      }
    }
    return perms.stream().distinct().collect(Collectors.toList());
  }
}
