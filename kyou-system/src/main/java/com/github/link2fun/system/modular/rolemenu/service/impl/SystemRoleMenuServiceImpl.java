package com.github.link2fun.system.modular.rolemenu.service.impl;

import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.rolemenu.service.ISystemRoleMenuService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

@Component
public class SystemRoleMenuServiceImpl implements ISystemRoleMenuService {

  @Db
  private EasyEntityQuery entityQuery;

  @Inject
  private MappingSync mappingSync;

  @Inject
  private RoleMenuMapping roleMenuMapping;

  /**
   * 查询菜单使用数量
   *
   * @param menuId 菜单ID
   * @return 结果
   */
  @Override
  public long countByMenuId(final Long menuId) {
    return entityQuery.queryable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.menuId().eq(menuId))
      .count();
  }

  /**
   * 重新分配角色的菜单, 角色最终可见的菜单与 menuIds 完全一致
   *
   * @param roleId  角色ID
   * @param menuIds 菜单ID集合, null 或空集合表示清空
   */
  @Override
  public void reassignMenus(final Long roleId, final List<Long> menuIds) {
    mappingSync.sync(roleMenuMapping, roleId, menuIds);
  }

  /** 批量删除角色菜单关联信息 */
  @Override
  @Transaction
  public void removeByRoleIds(final List<Long> roleIds) {
    roleMenuMapping.unlinkAll(roleIds);
  }
}
