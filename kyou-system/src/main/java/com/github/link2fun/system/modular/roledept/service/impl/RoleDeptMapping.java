package com.github.link2fun.system.modular.roledept.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 角色-部门关联表的操作集 */
@Component
public class RoleDeptMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询角色当前关联的全部部门 ID */
  @Override
  public List<Long> findTargetIds(final Long roleId) {
    return entityQuery.queryable(SysRoleDept.class)
      .where(roleDept -> roleDept.roleId().eq(roleId))
      .selectColumn(SysRoleDeptProxy::deptId)
      .toList();
  }

  /** 批量建立角色-部门关联 */
  @Override
  public void link(final Long roleId, final Collection<Long> deptIds) {
    final List<SysRoleDept> roleDepts = deptIds.stream()
      .map(deptId -> {
        SysRoleDept roleDept = new SysRoleDept();
        roleDept.setRoleId(roleId);
        roleDept.setDeptId(deptId);
        return roleDept;
      })
      .toList();
    entityQuery.insertable(roleDepts).batch().executeRows();
  }

  /** 批量解除角色-部门关联 */
  @Override
  public void unlink(final Long roleId, final Collection<Long> deptIds) {
    entityQuery.deletable(SysRoleDept.class)
      .where(roleDept -> {
        roleDept.roleId().eq(roleId);
        roleDept.deptId().in(deptIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除角色的全部部门关联 */
  @Override
  public void unlinkAll(final Long roleId) {
    entityQuery.deletable(SysRoleDept.class)
      .where(roleDept -> roleDept.roleId().eq(roleId))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
