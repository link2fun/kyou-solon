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

/** 用户-群组关联表的操作集, 宿主是群组; 以用户为宿主见 {@link UserGroupMapping} */
@Component
public class GroupUserMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询群组当前关联的全部用户 ID */
  @Override
  public List<Long> findTargetIds(final Long groupId) {
    return entityQuery.queryable(SysUserGroup.class)
      .where(userGroup -> userGroup.groupId().eq(groupId))
      .selectColumn(SysUserGroupProxy::userId)
      .toList();
  }

  /** 批量建立群组-用户关联 */
  @Override
  public void link(final Long groupId, final Collection<Long> userIds) {
    final List<SysUserGroup> userGroups = userIds.stream()
      .map(userId -> {
        SysUserGroup userGroup = new SysUserGroup();
        userGroup.setUserId(userId);
        userGroup.setGroupId(groupId);
        return userGroup;
      })
      .toList();
    entityQuery.insertable(userGroups).batch().executeRows();
  }

  /** 批量解除群组-用户关联 */
  @Override
  public void unlink(final Long groupId, final Collection<Long> userIds) {
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> {
        userGroup.groupId().eq(groupId);
        userGroup.userId().in(userIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除这些群组的全部用户关联, 空集合时不执行任何操作 */
  @Override
  public void unlinkAll(final Collection<Long> groupIds) {
    if (CollectionUtil.isEmpty(groupIds)) {
      return;
    }
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> userGroup.groupId().in(groupIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
