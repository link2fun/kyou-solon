package com.github.link2fun.framework.auth;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.easyquery.DataScope;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.uuid.IdUtils;
import org.noear.solon.annotation.Component;

import java.util.Objects;

/**
 * 数据范围的行准入: 一行记录能否被当前操作者看见与操作
 */
@Component
public class DataScopeRowGuard {

  @Db
  private EasyEntityQuery entityQuery;

  /** 用户行在操作者数据范围内 */
  public void assertUserVisible(final ActionContext context, final Long userId) {
    if (context.isSuperAdmin()) {
      return;
    }
    final Long targetDeptId = entityQuery.queryable(SysUser.class)
      .where(user -> {
        user.delFlag().eq(UserConstants.NORMAL);
        user.userId().eq(userId);
      })
      .selectColumn(SysUserProxy::deptId)
      .singleOrNull();
    if (!DataScope.of(entityQuery, context).containsUser(userId, targetDeptId)) {
      throw new ServiceException("没有权限访问用户数据！");
    }
  }

  /** 用户行可见且可改动: 超管用户受保护 */
  public void assertUserOperable(final ActionContext context, final Long userId) {
    if (Objects.nonNull(userId) && SysUser.isSuperAdmin(userId)) {
      throw new ServiceException("不允许操作超级管理员用户");
    }
    assertUserVisible(context, userId);
  }

  /** 角色行在操作者数据范围内(角色下有范围内用户) */
  public void assertRoleVisible(final ActionContext context, final Long roleId) {
    if (context.isSuperAdmin()) {
      return;
    }
    final DataScope dataScope = DataScope.of(entityQuery, context);
    final boolean visible = entityQuery.queryable(SysRole.class)
      .where(role -> {
        role.delFlag().eq(UserConstants.NORMAL);
        role.roleId().eq(IdUtils.isIdValid(roleId), roleId);
        if (dataScope.filters()) {
          role.userRoleList().any(userRole ->
            dataScope.applyTo(userRole, userRole.user().dept().deptId(), userRole.user().userId()));
        }
      })
      .any();
    if (!visible) {
      throw new ServiceException("没有权限访问角色数据！");
    }
  }

  /** 角色行可见且可改动: 超管角色受保护 */
  public void assertRoleOperable(final ActionContext context, final Long roleId) {
    if (Objects.nonNull(roleId) && SysRole.isSuperAdminRole(roleId)) {
      throw new ServiceException("不允许操作超级管理员角色");
    }
    assertRoleVisible(context, roleId);
  }

  /** 部门行在操作者数据范围内 */
  public void assertDeptVisible(final ActionContext context, final Long deptId) {
    if (!DataScope.of(entityQuery, context).containsDept(deptId)) {
      throw new ServiceException("没有权限访问部门数据！");
    }
  }
}
