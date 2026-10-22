package com.github.link2fun.framework.auth;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/**
 * AuthFacts 的 EasyQuery 实现; 查询随写事务执行, 能看到本事务未提交的变更。
 */
@Component
public class EasyQueryAuthFacts implements AuthFacts {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询角色当前的 roleKey */
  @Override
  public List<String> roleKeysByRoleIds(final Collection<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return List.of();
    }
    return entityQuery.queryable(SysRole.class)
      .where(role -> role.roleId().in(roleIds))
      .selectColumn(SysRoleProxy::roleKey)
      .toList();
  }

  /** 查询持有这些角色的用户 ID */
  @Override
  public List<Long> holderUserIdsByRoleIds(final Collection<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return List.of();
    }
    return entityQuery.queryable(SysUserRole.class)
      .where(userRole -> userRole.roleId().in(roleIds))
      .selectColumn(SysUserRoleProxy::userId)
      .toList();
  }

  /** 查询引用了这些菜单的角色标识 */
  @Override
  public List<String> roleKeysReferencingMenus(final Collection<Long> menuIds) {
    if (CollectionUtil.isEmpty(menuIds)) {
      return List.of();
    }
    return entityQuery.queryable(SysRole.class)
      .where(role -> role.roleMenuList().any(roleMenu -> roleMenu.menuId().in(menuIds)))
      .selectColumn(SysRoleProxy::roleKey)
      .toList();
  }
}
