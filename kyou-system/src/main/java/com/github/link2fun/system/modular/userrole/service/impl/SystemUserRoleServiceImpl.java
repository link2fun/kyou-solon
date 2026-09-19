package com.github.link2fun.system.modular.userrole.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SystemUserRoleServiceImpl implements ISystemUserRoleService {
  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  @Inject
  private UserRoleMapping userRoleMapping;

  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  @Override
  public long countByRoleId(final Long roleId) {
    return entityQuery.queryable(SysUserRole.class)
        .where(userRole -> userRole.roleId().eq(roleId))
        .count();
  }

  /**
   * 删除用户和角色关联信息
   * @return 结果
   */
  @Override
  public boolean removeMapping(final Long userId,final Long roleId) {
    return entityQuery.deletable(SysUserRole.class)
        .allowDeleteStatement(true)
        .where(userRole -> {
          userRole.userId().eq(userId);
          userRole.roleId().eq(roleId);
        })
        .executeRows() > 0;
  }

  /**
   * 批量取消授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要删除的用户数据ID
   * @return 结果
   */
  @Override
  public boolean deleteUserRoleInfos(final Long roleId, final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds) || roleId == null) {
      return false;
    }
    return entityQuery.deletable(SysUserRole.class)
        .where(userRole -> {
          userRole.roleId().eq(roleId);
          userRole.userId().in(userIds);
        })
        .allowDeleteStatement(true)
        .executeRows() > 0;
  }

  /**
   * 批量新增用户角色信息
   *
   * @param roleId  角色ID
   * @param userIds 用户列表
   * @return 结果
   */
  @Override
  public Boolean batchUserRole(final Long roleId, final List<Long> userIds) {

    // 参数检查
    if (roleId == null || CollectionUtil.isEmpty(userIds)) {
      return true;
    }
    // TODO 检查下要授权的用户是否已经有当前角色了
    final List<Long> userIdListExists = entityQuery.queryable(SysUserRole.class)
        .where(userRole -> {
          userRole.userId().in(userIds);
          userRole.roleId().eq(roleId);
        })
        .select(SysUserRoleProxy::userId)
        .toList();
    // 过滤一下要授权的用户
    final List<Long> userIdList_toAlloc = userIds.stream().filter(userId -> !userIdListExists.contains(userId))
        .collect(Collectors.toList());

    if (CollectionUtil.isEmpty(userIdList_toAlloc)) {
      // 没有要授权的了, 不进行处理
      return true;
    }

    final List<SysUserRole> userRoleList = userIdList_toAlloc.stream().map(userId -> {
      SysUserRole userRole = new SysUserRole();
      userRole.setUserId(userId);
      userRole.setRoleId(roleId);
      return userRole;
    }).collect(Collectors.toList());
    // return saveBatch(userRoleList);
    return entityQuery.insertable(userRoleList).batch().executeRows() != 0;
  }

  /**
   * 通过用户ID查询角色ID
   *
   * @param userId 用户ID
   * @return 角色列表
   */
  @Override
  public List<Long> findRoleIdListByUserId(final Long userId) {
    return entityQuery.queryable(SysUserRole.class)
        .where(userRole -> userRole.userId().eq(userId))
        .select(SysUserRoleProxy::roleId)
        .toList();
  }

  /**
   * 重新分配用户的角色, 用户最终持有的角色与 roleIds 完全一致
   *
   * @param userId  用户ID
   * @param roleIds 角色ID集合, null 或空集合表示清空
   */
  @Override
  public void reassignRoles(final Long userId, final List<Long> roleIds) {
    mappingSync.sync(userRoleMapping, userId, roleIds);
  }

  /**
   * 批量删除用户角色关联信息
   *
   * @param userIds 用户ID集合
   */
  @Override
  public void deleteUserRole(final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    entityQuery.deletable(SysUserRole.class)
        .allowDeleteStatement(true)
        .where(userRole -> userRole.userId().in(userIds))
        .executeRows();
  }
}
