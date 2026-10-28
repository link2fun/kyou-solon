package com.github.link2fun.system.modular.usergroup;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserGroup;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collection;

/** 用户-群组关联关系: 只负责两侧的批量解绑 */
@Component
public class UserGroupRelation {

  @Db
  private EasyEntityQuery entityQuery;

  /** 按用户 ID 批量解除关联(用户消亡时静默解绑) */
  @Transaction
  public void removeByUserIds(final Collection<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> userGroup.userId().in(userIds))
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 按群组 ID 批量解除关联(群组消亡时静默解绑) */
  @Transaction
  public void removeByGroupIds(final Collection<Long> groupIds) {
    if (CollectionUtil.isEmpty(groupIds)) {
      return;
    }
    entityQuery.deletable(SysUserGroup.class)
      .where(userGroup -> userGroup.groupId().in(groupIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
