package com.github.link2fun.system.modular.userrole.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import com.google.common.base.Preconditions;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemUserRoleServiceImpl implements ISystemUserRoleService {
  @Db
  private EasyEntityQuery entityQuery;

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
   * 建立用户和角色关联
   *
   * @param userId  用户ID
   * @param roleIds 角色ID集合
   */
  @Tran
  @Override
  public void updateMappingsByUserId(final Long userId, final List<Long> roleIds) {
    Preconditions.checkNotNull(userId, "用户ID不能为空");

    if (CollectionUtil.isEmpty(roleIds)) {
      log.debug("[更新用户角色关系] 没有角色要关联, 直接删除已存在的关联关系");
      // 没有角色要关联, 直接删除已存在的关联关系
      entityQuery.deletable(SysUserRole.class)
          .where(userRole -> userRole.userId().eq(userId))
          .allowDeleteStatement(true)
          .executeRows();
      return;
    }

    // 查询现有的用户-角色映射关系
    List<Long> existRoleIds = entityQuery.queryable(SysUserRole.class)
        .where(userRole -> userRole.userId().eq(userId))
        .selectColumn(SysUserRoleProxy::roleId)
        .toList();

    // 需要新增的角色ID
    List<Long> toInsertRoleIds = roleIds.stream()
        .filter(roleId -> !existRoleIds.contains(roleId))
        .collect(Collectors.toList());

    // 需要删除的角色ID
    List<Long> toDeleteRoleIds = existRoleIds.stream()
        .filter(roleId -> !roleIds.contains(roleId))
        .collect(Collectors.toList());

    // 执行新增
    if (!CollectionUtil.isEmpty(toInsertRoleIds)) {
      log.debug("[更新用户角色关系] 有角色要新增, 新增: {}", toInsertRoleIds);
      final List<SysUserRole> userRoleList = toInsertRoleIds.stream()
          .map(roleId -> {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
          }).collect(Collectors.toList());
      entityQuery.insertable(userRoleList).batch().executeRows();
    }

    // 执行删除
    if (!CollectionUtil.isEmpty(toDeleteRoleIds)) {
      log.debug("[更新用户角色关系] 有角色要删除, 删除: {}", toDeleteRoleIds);
      entityQuery.deletable(SysUserRole.class)
          .where(userRole -> {
            userRole.userId().eq(userId);
            userRole.roleId().in(toDeleteRoleIds);
          })
          .allowDeleteStatement(true)
          .executeRows();
    }
  }

  /**
   * 通过用户ID删除用户和角色关联
   *
   * @param userId 用户ID
   */
  @Override
  public void deleteUserRoleByUserId(final Long userId) {
    entityQuery.deletable(SysUserRole.class)
        .allowDeleteStatement(true)
        .where(userRole -> userRole.userId().eq(userId))
        .executeRows();
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
