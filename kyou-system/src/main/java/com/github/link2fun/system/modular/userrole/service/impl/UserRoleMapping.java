package com.github.link2fun.system.modular.userrole.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 用户-角色关联表的操作集 */
@Component
public class UserRoleMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询用户当前关联的全部角色 ID */
  @Override
  public List<Long> findTargetIds(final Long userId) {
    return entityQuery.queryable(SysUserRole.class)
      .where(userRole -> userRole.userId().eq(userId))
      .selectColumn(SysUserRoleProxy::roleId)
      .toList();
  }

  /** 批量建立用户-角色关联 */
  @Override
  public void link(final Long userId, final Collection<Long> roleIds) {
    final List<SysUserRole> userRoles = roleIds.stream()
      .map(roleId -> {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        return userRole;
      })
      .toList();
    entityQuery.insertable(userRoles).batch().executeRows();
  }

  /** 批量解除用户-角色关联 */
  @Override
  public void unlink(final Long userId, final Collection<Long> roleIds) {
    entityQuery.deletable(SysUserRole.class)
      .where(userRole -> {
        userRole.userId().eq(userId);
        userRole.roleId().in(roleIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除用户的全部角色关联 */
  @Override
  public void unlinkAll(final Long userId) {
    entityQuery.deletable(SysUserRole.class)
      .where(userRole -> userRole.userId().eq(userId))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
