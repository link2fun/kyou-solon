package com.github.link2fun.system.modular.roledept.service;

import java.util.List;

public interface ISystemRoleDeptService {

  /**
   * 重新分配角色的部门, 角色最终可见的部门与 deptIds 完全一致
   *
   * @param roleId  角色ID
   * @param deptIds 部门ID集合, null 或空集合表示清空
   */
  void reassignDepts(Long roleId, List<Long> deptIds);

  /**
   * 批量删除角色部门关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  void removeByRoleIds(List<Long> roleIds);
}
