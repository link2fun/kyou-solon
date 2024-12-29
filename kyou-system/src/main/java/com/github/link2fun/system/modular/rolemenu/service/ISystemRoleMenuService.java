package com.github.link2fun.system.modular.rolemenu.service;

import java.util.List;

public interface ISystemRoleMenuService {
  /**
   * 查询菜单使用数量
   *
   * @param menuId 菜单ID
   * @return 结果
   */
  long countByMenuId(Long menuId);

  /**
   * 更新角色和菜单的关系
   * 
   * @param roleId  角色id
   * @param menuIds 菜单id集合
   */
  void updateMappings(Long roleId, List<Long> menuIds);

  /**
   * 批量删除角色菜单关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  void removeByRoleIds(List<Long> roleIds);
}
