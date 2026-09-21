package com.github.link2fun.system.modular.userrole.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 用户-角色关联表的操作集, 宿主是角色; 以用户为宿主见 {@link UserRoleMapping} */
@Component
public class RoleUserMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询角色当前关联的全部用户 ID */
  @Override
  public List<Long> findTargetIds(final Long roleId) {
    return entityQuery.queryable(SysUserRole.class)
      .where(userRole -> userRole.roleId().eq(roleId))
      .selectColumn(SysUserRoleProxy::userId)
      .toList();
  }

  /** 批量建立角色-用户关联 */
  @Override
  public void link(final Long roleId, final Collection<Long> userIds) {
    final List<SysUserRole> userRoles = userIds.stream()
      .map(userId -> {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
      })
      .toList();
    entityQuery.insertable(userRoles).batch().executeRows();
  }

  /** 批量解除角色-用户关联 */
  @Override
  public void unlink(final Long roleId, final Collection<Long> userIds) {
    entityQuery.deletable(SysUserRole.class)
      .where(userRole -> {
        userRole.roleId().eq(roleId);
        userRole.userId().in(userIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除这些角色的全部用户关联, 空集合时不执行任何操作 */
  @Override
  public void unlinkAll(final Collection<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return;
    }
    entityQuery.deletable(SysUserRole.class)
      .where(userRole -> userRole.roleId().in(roleIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
