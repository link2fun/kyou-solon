package com.github.link2fun.system.modular.roledept.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.roledept.service.ISystemRoleDeptService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Component
public class SystemRoleDeptServiceImpl implements ISystemRoleDeptService {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  @Inject
  private RoleDeptMapping roleDeptMapping;

  /**
   * 重新分配角色的部门, 角色最终可见的部门与 deptIds 完全一致
   *
   * @param roleId  角色ID
   * @param deptIds 部门ID集合, null 或空集合表示清空
   */
  @Override
  public void reassignDepts(final Long roleId, final List<Long> deptIds) {
    mappingSync.sync(roleDeptMapping, roleId, deptIds);
  }

  /**
   * 批量删除角色部门关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  @Override
  public void removeByRoleIds(final List<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return;
    }
    entityQuery.deletable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().in(roleIds))
        .allowDeleteStatement(true)
        .executeRows();
  }
}
