package com.github.link2fun.system.modular.userrole;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import com.github.link2fun.support.easyquery.MappingSync;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collection;
import java.util.List;

/** 用户-角色关联关系: 两个方向与差异同步收在一处 */
@Component
public class UserRoleRelation {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  /** 用户当前关联的角色 ID */
  public List<Long> findRoleIdsByUserId(final Long userId) {
    return userHosted.findTargetIds(userId);
  }

  /** 角色当前关联的用户 ID */
  public List<Long> findUserIdsByRoleId(final Long roleId) {
    return roleHosted.findTargetIds(roleId);
  }

  /** 把用户的角色收敛到给定集合, null 或空集合表示清空 */
  public void reassignRoles(final Long userId, final Collection<Long> roleIds) {
    mappingSync.sync(userHosted, userId, roleIds);
  }

  /** 给角色追加用户, 已有关系保持不变 */
  public void assignUsers(final Long roleId, final Collection<Long> userIds) {
    mappingSync.add(roleHosted, roleId, userIds);
  }

  /** 批量解除角色下的用户关联 */
  public void unassignUsers(final Long roleId, final Collection<Long> userIds) {
    roleHosted.unlink(roleId, userIds);
  }

  /** 角色被多少用户持有 */
  public long countByRoleId(final Long roleId) {
    return entityQuery.queryable(SysUserRole.class)
      .where(userRole -> userRole.roleId().eq(roleId))
      .count();
  }

  /** 按用户 ID 批量解除关联(用户消亡时静默解绑) */
  @Transaction
  public void removeByUserIds(final Collection<Long> userIds) {
    userHosted.unlinkAll(userIds);
  }

  /** 用户为宿主的一侧 */
  private final MappingOps userHosted = new MappingOps() {
    @Override
    public String relationName() {
      return "用户→角色";
    }

    @Override
    public List<Long> findTargetIds(final Long userId) {
      return entityQuery.queryable(SysUserRole.class)
        .where(userRole -> userRole.userId().eq(userId))
        .selectColumn(SysUserRoleProxy::roleId)
        .toList();
    }

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

    @Override
    public void unlinkAll(final Collection<Long> userIds) {
      if (CollectionUtil.isEmpty(userIds)) {
        return;
      }
      entityQuery.deletable(SysUserRole.class)
        .where(userRole -> userRole.userId().in(userIds))
        .allowDeleteStatement(true)
        .executeRows();
    }
  };

  /** 角色为宿主的一侧 */
  private final MappingOps roleHosted = new MappingOps() {
    @Override
    public String relationName() {
      return "角色→用户";
    }

    @Override
    public List<Long> findTargetIds(final Long roleId) {
      return entityQuery.queryable(SysUserRole.class)
        .where(userRole -> userRole.roleId().eq(roleId))
        .selectColumn(SysUserRoleProxy::userId)
        .toList();
    }

    @Override
    public void link(final Long roleId, final Collection<Long> userIds) {
      final List<SysUserRole> userRoles = userIds.stream()
        .map(userId -> {
          SysUserRole userRole = new SysUserRole();
          userRole.setRoleId(roleId);
          userRole.setUserId(userId);
          return userRole;
        })
        .toList();
      entityQuery.insertable(userRoles).batch().executeRows();
    }

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
  };
}
