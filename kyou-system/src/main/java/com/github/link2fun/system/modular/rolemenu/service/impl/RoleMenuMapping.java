package com.github.link2fun.system.modular.rolemenu.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleMenuProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import org.noear.solon.annotation.Component;

import java.util.Collection;
import java.util.List;

/** 角色-菜单关联表的操作集 */
@Component
public class RoleMenuMapping implements MappingOps {

  @Db
  private EasyEntityQuery entityQuery;

  /** 查询角色当前关联的全部菜单 ID */
  @Override
  public List<Long> findTargetIds(final Long roleId) {
    return entityQuery.queryable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.roleId().eq(roleId))
      .selectColumn(SysRoleMenuProxy::menuId)
      .toList();
  }

  /** 批量建立角色-菜单关联 */
  @Override
  public void link(final Long roleId, final Collection<Long> menuIds) {
    final List<SysRoleMenu> roleMenus = menuIds.stream()
      .map(menuId -> {
        SysRoleMenu roleMenu = new SysRoleMenu();
        roleMenu.setRoleId(roleId);
        roleMenu.setMenuId(menuId);
        return roleMenu;
      })
      .toList();
    entityQuery.insertable(roleMenus).batch().executeRows();
  }

  /** 批量解除角色-菜单关联 */
  @Override
  public void unlink(final Long roleId, final Collection<Long> menuIds) {
    entityQuery.deletable(SysRoleMenu.class)
      .where(roleMenu -> {
        roleMenu.roleId().eq(roleId);
        roleMenu.menuId().in(menuIds);
      })
      .allowDeleteStatement(true)
      .executeRows();
  }

  /** 解除角色的全部菜单关联 */
  @Override
  public void unlinkAll(final Long roleId) {
    entityQuery.deletable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.roleId().eq(roleId))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
