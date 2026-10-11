package com.github.link2fun.system.modular.userpost.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserPostProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 岗位-用户关联表的操作集, 宿主是岗位; 以用户为宿主见 {@link UserPostMapping} */
@Component
public class PostUserMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询岗位当前关联的全部用户 ID */
  @Override
  public List<Long> findTargetIds(final Long postId) {
    return entityQuery.queryable(SysUserPost.class)
      .where(userPost -> userPost.postId().eq(postId))
      .selectColumn(SysUserPostProxy::userId)
      .toList();
  }

  /** 批量建立岗位-用户关联 */
  @Override
  public void link(final Long postId, final Collection<Long> userIds) {
    final List<SysUserPost> userPosts = userIds.stream()
      .map(userId -> {
        SysUserPost userPost = new SysUserPost();
        userPost.setPostId(postId);
        userPost.setUserId(userId);
        return userPost;
      })
      .toList();
    entityQuery.insertable(userPosts).batch().executeRows();
  }

  /** 批量解除岗位-用户关联 */
  @Override
  public void unlink(final Long postId, final Collection<Long> userIds) {
    entityQuery.deletable(SysUserPost.class)
      .where(userPost -> {
        userPost.postId().eq(postId);
        userPost.userId().in(userIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除这些岗位的全部用户关联, 空集合时不执行任何操作 */
  @Override
  public void unlinkAll(final Collection<Long> postIds) {
    if (CollectionUtil.isEmpty(postIds)) {
      return;
    }
    entityQuery.deletable(SysUserPost.class)
      .where(userPost -> userPost.postId().in(postIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
