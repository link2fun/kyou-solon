package com.github.link2fun.system.modular.userpost.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserPostProxy;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemUserPostServiceImpl implements ISystemUserPostService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 通过岗位ID查询岗位使用数量
   *
   * @param postId 岗位ID
   * @return 结果
   */
  @Override
  public long countUserPostById(final Long postId) {
    return entityQuery.queryable(SysUserPost.class)
        .where(userPost -> userPost.postId().eq(postId))
        .count();
  }

  /**
   * 根据用户ID查询岗位IDList
   *
   * @param userId 用户ID
   * @return 岗位IDList
   */
  @Override
  public List<Long> findPostIdListByUserId(final Long userId) {
    return entityQuery.queryable(SysUserPost.class)
        .where(userPost -> userPost.userId().eq(userId))
        .select(SysUserPostProxy::postId)
        .toList();
  }

  /**
   * 建立用户和岗位关联
   *
   * @param userId  用户ID
   * @param postIds 岗位ID集合
   */
  @Override
  public void updateMappingsByUserId(final Long userId, final List<Long> postIds) {
    Preconditions.checkNotNull(userId, "用户ID不能为空");

    if (CollectionUtil.isEmpty(postIds)) {
      // 如果新的岗位列表为空,则删除所有现有映射
      entityQuery.deletable(SysUserPost.class)
          .where(userPost -> userPost.userId().eq(userId))
          .allowDeleteStatement(true)
          .executeRows();
      return;
    }

    // 查询现有的用户-岗位映射关系
    List<Long> existPostIds = entityQuery.queryable(SysUserPost.class)
        .where(userPost -> userPost.userId().eq(userId))
        .selectColumn(SysUserPostProxy::postId)
        .toList();

    // 需要新增的岗位ID
    List<Long> toInsertPostIds = postIds.stream()
        .filter(postId -> !existPostIds.contains(postId))
        .collect(Collectors.toList());

    // 需要删除的岗位ID
    List<Long> toDeletePostIds = existPostIds.stream()
        .filter(postId -> !postIds.contains(postId))
        .collect(Collectors.toList());

    // 执行新增
    if (!CollectionUtil.isEmpty(toInsertPostIds)) {
      final List<SysUserPost> userPostList = toInsertPostIds.stream()
          .map(postId -> {
            SysUserPost userPost = new SysUserPost();
            userPost.setUserId(userId);
            userPost.setPostId(postId);
            return userPost;
          }).collect(Collectors.toList());
      entityQuery.insertable(userPostList).batch().executeRows();
    }

    // 执行删除
    if (!CollectionUtil.isEmpty(toDeletePostIds)) {
      entityQuery.deletable(SysUserPost.class)
          .where(userPost -> {
            userPost.userId().eq(userId);
            userPost.postId().in(toDeletePostIds);
          })
          .allowDeleteStatement(true)
          .executeRows();
    }
  }

  /**
   * 通过用户ID删除用户和岗位关联
   *
   * @param userId 用户ID
   */
  @Override
  public void deleteUserPostByUserId(final Long userId) {
    entityQuery.deletable(SysUserPost.class)
        .where(userPost -> userPost.userId().eq(userId))
        .allowDeleteStatement(true)
        .executeRows();
  }

  /**
   * 批量删除用户岗位关联信息
   *
   * @param userIds 用户ID集合
   */
  @Override
  public void deleteUserPost(final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }

    entityQuery.deletable(SysUserPost.class)
        .allowDeleteStatement(true)
        .where(userPost -> userPost.userId().in(userIds))
        .executeRows();
  }
}
