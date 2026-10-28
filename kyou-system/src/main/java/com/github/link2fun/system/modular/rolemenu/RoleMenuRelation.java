package com.github.link2fun.system.modular.rolemenu;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleMenuProxy;
import com.github.link2fun.support.easyquery.MappingOps;
import com.github.link2fun.support.easyquery.MappingSync;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collection;
import java.util.List;

/** 角色-菜单关联关系: 以角色为宿主 */
@Component
public class RoleMenuRelation {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  /** 角色当前关联的菜单 ID */
  public List<Long> findMenuIdsByRoleId(final Long roleId) {
    return roleHosted.findTargetIds(roleId);
  }

  /** 把角色的菜单收敛到给定集合, null 或空集合表示清空 */
  public void reassignMenus(final Long roleId, final Collection<Long> menuIds) {
    mappingSync.sync(roleHosted, roleId, menuIds);
  }

  /** 按角色 ID 批量解除关联(角色消亡时静默解绑) */
  @Transaction
  public void removeByRoleIds(final Collection<Long> roleIds) {
    roleHosted.unlinkAll(roleIds);
  }

  /** 菜单被多少角色引用 */
  public long countByMenuId(final Long menuId) {
    return entityQuery.queryable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.menuId().eq(menuId))
      .count();
  }

  /** 角色为宿主的一侧 */
  private final MappingOps roleHosted = new MappingOps() {
    @Override
    public String relationName() {
      return "角色→菜单";
    }

    @Override
    public List<Long> findTargetIds(final Long roleId) {
      return entityQuery.queryable(SysRoleMenu.class)
        .where(roleMenu -> roleMenu.roleId().eq(roleId))
        .selectColumn(SysRoleMenuProxy::menuId)
        .toList();
    }

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

    @Override
    public void unlinkAll(final Collection<Long> roleIds) {
      if (CollectionUtil.isEmpty(roleIds)) {
        return;
      }
      entityQuery.deletable(SysRoleMenu.class)
        .where(roleMenu -> roleMenu.roleId().in(roleIds))
        .allowDeleteStatement(true)
        .executeRows();
    }
  };
}
