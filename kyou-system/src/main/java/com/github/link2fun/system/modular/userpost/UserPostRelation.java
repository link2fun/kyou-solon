package com.github.link2fun.system.modular.userpost;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserPostProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import com.github.link2fun.support.easyquery.MappingSync;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collection;
import java.util.List;

/** 用户-岗位关联关系: 两个方向与差异同步收在一处 */
@Component
public class UserPostRelation {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  /** 用户当前关联的岗位 ID */
  public List<Long> findPostIdsByUserId(final Long userId) {
    return userHosted.findTargetIds(userId);
  }

  /** 把用户的岗位收敛到给定集合, null 或空集合表示清空 */
  public void reassignPosts(final Long userId, final Collection<Long> postIds) {
    mappingSync.sync(userHosted, userId, postIds);
  }

  /** 按用户 ID 批量解除关联(用户消亡时静默解绑) */
  @Transaction
  public void removeByUserIds(final Collection<Long> userIds) {
    userHosted.unlinkAll(userIds);
  }

  /** 按岗位 ID 批量解除关联(岗位消亡时静默解绑) */
  @Transaction
  public void removeByPostIds(final Collection<Long> postIds) {
    if (CollectionUtil.isEmpty(postIds)) {
      return;
    }
    entityQuery.deletable(SysUserPost.class)
      .where(userPost -> userPost.postId().in(postIds))
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 用户为宿主的一侧 */
  private final MappingOps userHosted = new MappingOps() {
    @Override
    public String relationName() {
      return "用户→岗位";
    }

    @Override
    public List<Long> findTargetIds(final Long userId) {
      return entityQuery.queryable(SysUserPost.class)
        .where(userPost -> userPost.userId().eq(userId))
        .selectColumn(SysUserPostProxy::postId)
        .toList();
    }

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

    @Override
    public void unlinkAll(final Collection<Long> userIds) {
      if (CollectionUtil.isEmpty(userIds)) {
        return;
      }
      entityQuery.deletable(SysUserPost.class)
        .where(userPost -> userPost.userId().in(userIds))
        .allowDeleteStatement(true)
        .executeRows();
    }
  };
}
