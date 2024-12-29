package com.github.link2fun.system.modular.usergroup.service;

import java.util.List;

/** 用户和群组关联表 服务接口 */
public interface ISystemUserGroupService {

  /**
   * 给用户分配多个群组, 没有在分配群组内的会进行取消
   *
   * @param userId   用户ID
   * @param groupIds 群组ID List
   * @return 是否成功
   */
  boolean assignGroups(Long userId, List<Long> groupIds);

  /**
   * 向群组中添加多个用户
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  void addUsers(Long groupId, List<Long> userIds);

  /**
   * 从群组中移除多个用户
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  void removeUsers(Long groupId, List<Long> userIds);

  /**
   * 根据用户ID查询群组ID列表
   *
   * @param userId 用户ID
   * @return 群组ID列表
   */
  List<Long> queryGroupIdsByUserId(Long userId);

  /**
   * 根据群组ID查询用户ID列表
   *
   * @param groupId 群组ID
   * @return 用户ID列表
   */
  List<Long> queryUserIdsByGroupId(Long groupId);

}
