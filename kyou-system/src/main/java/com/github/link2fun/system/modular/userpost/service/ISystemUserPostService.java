package com.github.link2fun.system.modular.userpost.service;

import java.util.List;

public interface ISystemUserPostService {

  /**
   * 根据用户ID查询岗位IDList
   *
   * @param userId 用户ID
   * @return 岗位IDList
   */
  List<Long> findPostIdListByUserId(Long userId);

  /**
   * 重新分配用户的岗位, 用户最终持有的岗位与 postIds 完全一致
   *
   * @param userId  用户ID
   * @param postIds 岗位ID集合, null 或空集合表示清空
   */
  void reassignPosts(Long userId, List<Long> postIds);

  /**
   * 批量删除用户岗位关联信息
   *
   * @param userIds 用户ID集合
   */
  void deleteUserPost(List<Long> userIds);
}
