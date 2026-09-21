package com.github.link2fun.system.modular.userrole.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

@Component
public class SystemUserRoleServiceImpl implements ISystemUserRoleService {
  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  @Inject
  private UserRoleMapping userRoleMapping;

  @Inject
  private RoleUserMapping roleUserMapping;

  /** 通过角色ID查询角色使用数量 */
  @Override
  public long countByRoleId(final Long roleId) {
    return entityQuery.queryable(SysUserRole.class)
      .where(userRole -> userRole.roleId().eq(roleId))
      .count();
  }

  /** 取消单个用户的角色授权 */
  @Override
  public void unassignUser(final Long roleId, final Long userId) {
    userRoleMapping.unlink(userId, List.of(roleId));
  }

  /** 批量取消授权用户角色 */
  @Override
  @Transaction
  public void unassignUsers(final Long roleId, final List<Long> userIds) {
    roleUserMapping.unlink(roleId, userIds);
  }

  /** 批量为角色授权用户 */
  @Override
  public void assignUsers(final Long roleId, final List<Long> userIds) {
    mappingSync.add(roleUserMapping, roleId, userIds);
  }

  /** 通过用户ID查询角色ID */
  @Override
  public List<Long> findRoleIdListByUserId(final Long userId) {
    return userRoleMapping.findTargetIds(userId);
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

  /** 批量删除用户角色关联信息 */
  @Override
  @Transaction
  public void deleteUserRole(final List<Long> userIds) {
    userRoleMapping.unlinkAll(userIds);
  }
}
