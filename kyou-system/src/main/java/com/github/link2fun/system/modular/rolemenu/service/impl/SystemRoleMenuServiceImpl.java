package com.github.link2fun.system.modular.rolemenu.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.domain.entity.SysRoleMenu;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleMenuProxy;
import com.github.link2fun.system.modular.rolemenu.service.ISystemRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Tran;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SystemRoleMenuServiceImpl implements ISystemRoleMenuService {

  @Db
  private EasyEntityQuery entityQuery;

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
   * 更新角色和菜单的关系
   *
   * @param roleId  角色id
   * @param menuIds 菜单id集合
   */
  @Override
  @Tran
  public void updateMappings(final Long roleId, final List<Long> menuIds) {
    if (CollectionUtil.isEmpty(menuIds)) {
      // 如果新的菜单列表为空,则删除所有现有映射
      entityQuery.deletable(SysRoleMenu.class)
        .where(roleMenu -> roleMenu.roleId().eq(roleId))
        .allowDeleteStatement(true)
        .executeRows();
      return;
    }

    // 查询现有的角色-菜单映射关系
    List<Long> existMenuIds = entityQuery.queryable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.roleId().eq(roleId))
      .selectColumn(SysRoleMenuProxy::menuId)
      .toList();

    // 需要新增的菜单ID
    List<Long> toInsertMenuIds = menuIds.stream()
      .filter(menuId -> !existMenuIds.contains(menuId))
      .collect(Collectors.toList());

    // 需要删除的菜单ID
    List<Long> toDeleteMenuIds = existMenuIds.stream()
      .filter(menuId -> !menuIds.contains(menuId))
      .collect(Collectors.toList());

    // 执行新增
    if (!CollectionUtil.isEmpty(toInsertMenuIds)) {
      // 组装新增数据
      final List<SysRoleMenu> roleMenuList = toInsertMenuIds.stream()
        .map(menuId -> {
          SysRoleMenu sysRoleMenu = new SysRoleMenu();
          sysRoleMenu.setRoleId(roleId);
          sysRoleMenu.setMenuId(menuId);
          return sysRoleMenu;
        }).collect(Collectors.toList());
      // 执行新增
      entityQuery.insertable(roleMenuList)
        .batch()
        .executeRows();
    }

    // 执行删除
    if (!CollectionUtil.isEmpty(toDeleteMenuIds)) {
      entityQuery.deletable(SysRoleMenu.class)
        .where(roleMenu -> {
          roleMenu.roleId().eq(roleId);
          roleMenu.menuId().in(toDeleteMenuIds);
        })
        .allowDeleteStatement(true)
        .executeRows();
    }
  }

  /**
   * 批量删除角色菜单关联信息
   *
   * @param roleIds 需要删除的数据ID
   */
  @Override
  public void removeByRoleIds(final List<Long> roleIds) {
    if (CollectionUtil.isEmpty(roleIds)) {
      return;
    }
    entityQuery.deletable(SysRoleMenu.class)
      .where(roleMenu -> roleMenu.roleId().in(roleIds))
      .allowDeleteStatement(true)
      .executeRows();
  }
}
