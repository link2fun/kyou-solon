package com.github.link2fun.system.modular.userpost.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserPost;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class SystemUserPostServiceImpl implements ISystemUserPostService {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  @Inject
  private UserPostMapping userPostMapping;

  /**
   * 根据用户ID查询岗位IDList
   *
   * @param userId 用户ID
   * @return 岗位IDList
   */
  @Override
  public List<Long> findPostIdListByUserId(final Long userId) {
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
