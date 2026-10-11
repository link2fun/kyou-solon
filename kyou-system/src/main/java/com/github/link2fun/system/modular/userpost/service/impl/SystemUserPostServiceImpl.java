package com.github.link2fun.system.modular.userpost.service.impl;

import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

@Component
public class SystemUserPostServiceImpl implements ISystemUserPostService {

  @Inject
  private MappingSync mappingSync;

  @Inject
  private UserPostMapping userPostMapping;

  @Inject
  private PostUserMapping postUserMapping;

  /**
   * 根据用户ID查询岗位ID集合
   *
   * @param userId 用户ID
   * @return 岗位ID集合
   */
  @Override
  public List<Long> findPostIdsByUserId(final Long userId) {
    return userPostMapping.findTargetIds(userId);
  }

  /**
   * 重新分配用户的岗位, 用户最终持有的岗位与 postIds 完全一致
   *
   * @param userId  用户ID
   * @param postIds 岗位ID集合, null 或空集合表示清空
   */
  @Override
  public void reassignPosts(final Long userId, final List<Long> postIds) {
    mappingSync.sync(userPostMapping, userId, postIds);
  }

  /** 按用户 ID 批量解除用户-岗位关联（用户消亡时静默解绑） */
  @Override
  @Transaction
  public void removeByUserIds(final List<Long> userIds) {
    userPostMapping.unlinkAll(userIds);
  }

  /** 批量删除岗位的用户关联信息（岗位方向, 岗位消亡时静默解绑） */
  @Override
  @Transaction
  public void removeByPostIds(final List<Long> postIds) {
    postUserMapping.unlinkAll(postIds);
  }
}
