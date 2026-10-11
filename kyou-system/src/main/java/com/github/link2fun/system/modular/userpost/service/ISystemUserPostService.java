package com.github.link2fun.system.modular.userpost.service;

import java.util.List;

public interface ISystemUserPostService {

  /**
   * 根据用户ID查询岗位ID集合
   *
   * @param userId 用户ID
   * @return 岗位ID集合
   */
  List<Long> findPostIdsByUserId(Long userId);

  /**
   * 重新分配用户的岗位, 用户最终持有的岗位与 postIds 完全一致
   *
   * @param userId  用户ID
   * @param postIds 岗位ID集合, null 或空集合表示清空
   */
  void reassignPosts(Long userId, List<Long> postIds);

  /**
   * 按用户 ID 批量解除用户-岗位关联（用户消亡时静默解绑）
   *
   * @param userIds 用户ID集合
   */
  void removeByUserIds(List<Long> userIds);

  /**
   * 批量删除岗位的用户关联信息（岗位方向, 岗位消亡时静默解绑）
   *
   * @param postIds 岗位ID集合
   */
  void removeByPostIds(List<Long> postIds);
}
