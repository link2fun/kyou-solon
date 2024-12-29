package com.github.link2fun.system.modular.roledept.service;

import java.util.List;

public interface ISystemRoleDeptService {

  /**
   * 更新角色和部门的关系
   * 
   * @param roleId  角色id*
   * @param deptIds 部门id集合
   */

  void updateMappings(Long roleId, List<Long> deptIds);

  /**
   * 批量删除角色部门关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  void removeByRoleIds(List<Long> roleIds);
}
