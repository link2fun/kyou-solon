package com.github.link2fun.system.modular.roledept;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import com.github.link2fun.support.easyquery.MappingSync;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collection;
import java.util.List;

/** 角色-部门关联关系: 以角色为宿主 */
@Component
public class RoleDeptRelation {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  /** 角色当前关联的部门 ID */
  public List<Long> findDeptIdsByRoleId(final Long roleId) {
    return roleHosted.findTargetIds(roleId);
  }

  /** 把角色的部门收敛到给定集合, null 或空集合表示清空 */
  public void reassignDepts(final Long roleId, final Collection<Long> deptIds) {
    mappingSync.sync(roleHosted, roleId, deptIds);
  }

  /** 按角色 ID 批量解除关联(角色消亡时静默解绑) */
  @Transaction
  public void removeByRoleIds(final Collection<Long> roleIds) {
    roleHosted.unlinkAll(roleIds);
  }

  /** 角色为宿主的一侧 */
  private final MappingOps roleHosted = new MappingOps() {
    @Override
    public String relationName() {
      return "角色→部门";
    }

    @Override
    public List<Long> findTargetIds(final Long roleId) {
      return entityQuery.queryable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().eq(roleId))
        .selectColumn(SysRoleDeptProxy::deptId)
        .toList();
    }

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

    @Override
    public void unlinkAll(final Collection<Long> roleIds) {
      if (CollectionUtil.isEmpty(roleIds)) {
        return;
      }
      entityQuery.deletable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().in(roleIds))
        .allowDeleteStatement(true)
        .executeRows();
    }
  };
}
