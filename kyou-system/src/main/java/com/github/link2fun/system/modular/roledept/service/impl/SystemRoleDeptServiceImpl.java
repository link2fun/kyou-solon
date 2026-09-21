package com.github.link2fun.system.modular.roledept.service.impl;

import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.roledept.service.ISystemRoleDeptService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

@Component
public class SystemRoleDeptServiceImpl implements ISystemRoleDeptService {

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

  /** 批量删除角色部门关联信息 */
  @Override
  @Transaction
  public void removeByRoleIds(final List<Long> roleIds) {
    roleDeptMapping.unlinkAll(roleIds);
  }
}
