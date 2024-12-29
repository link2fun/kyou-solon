package com.github.link2fun.system.modular.userpost.service;

import java.util.List;

public interface ISystemUserPostService {

  /**
   * 通过岗位ID查询岗位使用数量
   *
   * @param postId 岗位ID
   * @return 结果
   */
  long countUserPostById(Long postId);

  /**
   * 根据用户ID查询岗位IDList
   *
   * @param userId 用户ID
   * @return 岗位IDList
   */
  List<Long> findPostIdListByUserId(Long userId);

  /**
   * 建立用户和岗位关联
   *
   * @param userId  用户ID
   * @param postIds 岗位ID集合
   */
  void updateMappingsByUserId(Long userId, List<Long> postIds);

  /**
   * 通过用户ID删除用户和岗位关联
   *
   * @param userId 用户ID
   */
  void deleteUserPostByUserId(Long userId);

  /**
   * 批量删除用户岗位关联信息
   *
   * @param userIds 用户ID集合
   */
  void deleteUserPost(List<Long> userIds);
}
