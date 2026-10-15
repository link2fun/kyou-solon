package com.github.link2fun.system.modular.userrole.service;

import java.util.List;

public interface ISystemUserRoleService {
  /** 通过角色ID查询角色使用数量 */
  long countByRoleId(Long roleId);

  /** 取消单个用户的角色授权 */
  void unassignUser(Long roleId, Long userId);

  /** 批量取消授权用户角色 */
  void unassignUsers(Long roleId, List<Long> userIds);

  /** 批量为角色授权用户 */
  void assignUsers(Long roleId, List<Long> userIds);

  /** 通过用户ID查询角色ID集合 */
  List<Long> findRoleIdsByUserId(Long userId);

  /** 通过角色ID查询持有该角色的用户ID集合 */
  List<Long> findUserIdsByRoleId(Long roleId);

  /**
   * 重新分配用户的角色, 用户最终持有的角色与 roleIds 完全一致
   *
   * @param userId  用户ID
   * @param roleIds 角色ID集合, null 或空集合表示清空
   */
  void reassignRoles(Long userId, List<Long> roleIds);

  /** 按用户 ID 批量解除用户-角色关联（用户消亡时静默解绑） */
  void removeByUserIds(List<Long> userIds);
}
