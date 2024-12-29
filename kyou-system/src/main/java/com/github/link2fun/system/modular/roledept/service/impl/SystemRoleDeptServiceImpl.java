package com.github.link2fun.system.modular.roledept.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;
import com.github.link2fun.system.modular.roledept.service.ISystemRoleDeptService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemRoleDeptServiceImpl implements ISystemRoleDeptService {

  @Db
  private EasyEntityQuery entityQuery;


  /**
   * 更新角色和部门的关系
   *
   * @param roleId  角色id
   * @param deptIds 部门id集合
   */
  @Override
  @Tran
  public void updateMappings(final Long roleId, final List<Long> deptIds) {
    if (CollectionUtil.isEmpty(deptIds)) {
      // 如果新的部门列表为空,则删除所有现有映射
      entityQuery.deletable(SysRoleDept.class)
          .where(roleDept -> roleDept.roleId().eq(roleId))
          .allowDeleteStatement(true)
          .executeRows();
      return;
    }

    // 查询现有的角色-部门映射关系
    List<Long> existDeptIds = entityQuery.queryable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().eq(roleId))
        .selectColumn(SysRoleDeptProxy::deptId)
        .toList();

    // 需要新增的部门ID
    List<Long> toInsertDeptIds = deptIds.stream()
        .filter(deptId -> !existDeptIds.contains(deptId))
        .collect(Collectors.toList());

    // 需要删除的部门ID
    List<Long> toDeleteDeptIds = existDeptIds.stream()
        .filter(deptId -> !deptIds.contains(deptId))
        .collect(Collectors.toList());

    // 执行新增
    if (!CollectionUtil.isEmpty(toInsertDeptIds)) {
      // 组装新增数据
      final List<SysRoleDept> roleDeptList = toInsertDeptIds.stream()
          .map(deptId -> {
            SysRoleDept sysRoleDept = new SysRoleDept();
            sysRoleDept.setRoleId(roleId);
            sysRoleDept.setDeptId(deptId);
            return sysRoleDept;
          }).collect(Collectors.toList());
      // 执行新增
      entityQuery.insertable(roleDeptList)
          .batch()
          .executeRows();
    }

    // 执行删除
    if (!CollectionUtil.isEmpty(toDeleteDeptIds)) {
      entityQuery.deletable(SysRoleDept.class)
          .where(roleDept -> {
            roleDept.roleId().eq(roleId);
            roleDept.deptId().in(toDeleteDeptIds);
          })
          .allowDeleteStatement(true)
          .executeRows();
    }
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
