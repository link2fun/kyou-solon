package com.github.link2fun.system.modular.userrole.service;

import java.util.List;

public interface ISystemUserRoleService {
  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  long countByRoleId(Long roleId);

  /**
   * 删除用户和角色关联信息
   *
   * @param userId
   * @param roleId
   * @return 结果
   */
  boolean removeMapping(Long userId, Long roleId);

  /**
   * 批量取消授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要删除的用户数据ID
   * @return 结果
   */
  boolean deleteUserRoleInfos(Long roleId, List<Long> userIds);

  /**
   * 批量新增用户角色信息
   *
   * @param roleId  角色ID
   * @param userIds 用户列表
   * @return 结果
   */
  Boolean batchUserRole(Long roleId, List<Long> userIds);

  /**
   * 通过用户ID查询角色ID
   *
   * @param userId 用户ID
   * @return 角色列表
   */
  List<Long> findRoleIdListByUserId(Long userId);

  /**
   * 建立用户和角色关联
   *
   * @param userId  用户ID
   * @param roleIds 角色ID集合
   */
  void updateMappingsByUserId(Long userId, List<Long> roleIds);

  /**
   * 通过用户ID删除用户和角色关联
   *
   * @param userId 用户ID
   */
  void deleteUserRoleByUserId(Long userId);

  /**
   * 批量删除用户角色关联信息
   *
   * @param userIds 用户ID集合
   */
  void deleteUserRole(List<Long> userIds);
}
