package com.github.link2fun.system.modular.usergroup.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.easyquery.MappingOps;
import com.github.link2fun.system.modular.usergroup.model.SysUserGroup;
import com.github.link2fun.system.modular.usergroup.model.proxy.SysUserGroupProxy;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 用户-群组关联表的操作集 */
@Component
public class UserGroupMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询用户当前关联的全部群组 ID */
  @Override
  public List<Long> findTargetIds(final Long userId) {
    return entityQuery.queryable(SysUserGroup.class)
      .where(userGroup -> userGroup.userId().eq(userId))
      .selectColumn(SysUserGroupProxy::groupId)
      .toList();
  }

  /** 批量建立用户-群组关联 */
  @Override
  public void link(final Long userId, final Collection<Long> groupIds) {
    final List<SysUserGroup> userGroups = groupIds.stream()
      .map(groupId -> {
        SysUserGroup userGroup = new SysUserGroup();
        userGroup.setUserId(userId);
        userGroup.setGroupId(groupId);
        return userGroup;
      })
      .toList();
    entityQuery.insertable(userGroups).batch().executeRows();
  }

  /** 批量解除用户-群组关联 */
  @Override
  public void unlink(final Long userId, final Collection<Long> groupIds) {
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> {
        userGroup.userId().eq(userId);
        userGroup.groupId().in(groupIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除这些用户的全部群组关联, 空集合时不执行任何操作 */
  @Override
  public void unlinkAll(final Collection<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> userGroup.userId().in(userIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
