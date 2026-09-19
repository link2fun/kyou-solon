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
   * 重新分配角色的菜单, 角色最终可见的菜单与 menuIds 完全一致
   *
   * @param roleId  角色ID
   * @param menuIds 菜单ID集合, null 或空集合表示清空
   */
  void reassignMenus(Long roleId, List<Long> menuIds);

  /**
   * 批量删除角色菜单关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  void removeByRoleIds(List<Long> roleIds);
}
