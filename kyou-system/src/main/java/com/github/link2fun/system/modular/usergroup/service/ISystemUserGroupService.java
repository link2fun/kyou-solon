package com.github.link2fun.system.modular.usergroup.service;

import java.util.List;

/** 用户和群组关联表 服务接口 */
public interface ISystemUserGroupService {

  /**
   * 重新分配用户的群组, 用户最终持有的群组与 groupIds 完全一致
   *
   * @param userId   用户ID
   * @param groupIds 群组ID集合, null 或空集合表示清空
   */
  void reassignGroups(Long userId, List<Long> groupIds);

  /**
   * 批量为群组分配用户, 已有关系保持不变
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  void assignUsers(Long groupId, List<Long> userIds);

  /**
   * 批量取消群组的用户授权
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  void unassignUsers(Long groupId, List<Long> userIds);

  /**
   * 根据用户ID查询群组ID集合
   *
   * @param userId 用户ID
   * @return 群组ID集合
   */
  List<Long> findGroupIdsByUserId(Long userId);

  /**
   * 根据群组ID查询用户ID集合
   *
   * @param groupId 群组ID
   * @return 用户ID集合
   */
  List<Long> findUserIdsByGroupId(Long groupId);

  /**
   * 按用户 ID 批量解除用户-群组关联（用户消亡时静默解绑）
   *
   * @param userIds 用户ID集合
   */
  void removeByUserIds(List<Long> userIds);

  /**
   * 批量删除群组的用户关联信息（群组消亡时静默解绑）
   *
   * @param groupIds 群组ID集合
   */
  void removeByGroupIds(List<Long> groupIds);

}
