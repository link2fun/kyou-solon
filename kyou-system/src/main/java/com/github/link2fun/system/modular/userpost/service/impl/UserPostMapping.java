package com.github.link2fun.system.modular.userpost.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserPostProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 用户-岗位关联表的操作集 */
@Component
public class UserPostMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询用户当前关联的全部岗位 ID */
  @Override
  public List<Long> findTargetIds(final Long userId) {
    return entityQuery.queryable(SysUserPost.class)
      .where(userPost -> userPost.userId().eq(userId))
      .selectColumn(SysUserPostProxy::postId)
      .toList();
  }

  /** 批量建立用户-岗位关联 */
  @Override
  public void link(final Long userId, final Collection<Long> postIds) {
    final List<SysUserPost> userPosts = postIds.stream()
      .map(postId -> {
        SysUserPost userPost = new SysUserPost();
        userPost.setUserId(userId);
        userPost.setPostId(postId);
        return userPost;
      })
      .toList();
    entityQuery.insertable(userPosts).batch().executeRows();
  }

  /** 批量解除用户-岗位关联 */
  @Override
  public void unlink(final Long userId, final Collection<Long> postIds) {
    entityQuery.deletable(SysUserPost.class)
      .where(userPost -> {
        userPost.userId().eq(userId);
        userPost.postId().in(postIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除用户的全部岗位关联 */
  @Override
  public void unlinkAll(final Long userId) {
    entityQuery.deletable(SysUserPost.class)
      .where(userPost -> userPost.userId().eq(userId))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
