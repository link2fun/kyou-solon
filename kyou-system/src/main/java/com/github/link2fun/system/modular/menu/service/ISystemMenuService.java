package com.github.link2fun.system.modular.menu.service;

import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.system.domain.vo.RouterVo;

import java.util.List;

/**
 * 菜单 业务层
 *
 * @author ruoyi
 */
public interface ISystemMenuService {
  /**
   * 根据用户查询系统菜单列表
   *
   * @param userId 用户ID
   * @return 菜单列表
   */
  List<SysMenu> selectMenuList(Long userId);

  /**
   * 根据用户查询系统菜单列表
   *
   * @param menu   菜单信息
   * @param userId 用户ID
   * @return 菜单列表
   */
  List<SysMenu> selectMenuList(SysMenu menu, Long userId);

  /**
   * 根据用户ID查询权限
   *
   * @param userId 用户ID
   * @return 权限列表
   */
  List<String> selectMenuPermsByUserId(Long userId);

  /**
   * 根据角色ID查询权限
   *
   * @param roleId 角色ID
   * @return 权限列表
   */
  List<String> selectMenuPermsByRoleId(Long roleId);


  /** 根据角色Key查询权限 */
  List<String> selectMenuPermsByRoleKey(String roleKey);

  /**
   * 根据用户ID查询菜单树信息
   *
   * @param userId 用户ID
   * @return 菜单列表
   */
  List<SysMenu> selectMenuTreeByUserId(Long userId);

  /**
   * 根据角色ID查询菜单树信息
   *
   * @param roleId 角色ID
   * @return 选中菜单列表
   */
  List<Long> selectMenuListByRoleId(Long roleId);

  /**
   * 构建前端路由所需要的菜单
   *
   * @param menus 菜单列表
   * @return 路由列表
   */
  List<RouterVo> buildMenus(List<SysMenu> menus);

  /**
   * 构建前端所需要树结构
   *
   * @param menus 菜单列表
   * @return 树结构列表
   */
  List<SysMenu> buildMenuTree(List<SysMenu> menus);

  /**
   * 构建前端所需要下拉树结构
   *
   * @param menus 菜单列表
   * @return 下拉树结构列表
   */
  List<TreeSelect> buildMenuTreeSelect(List<SysMenu> menus);

  /**
   * 根据菜单ID查询信息
   *
   * @param menuId 菜单ID
   * @return 菜单信息
   */
  SysMenu selectMenuById(Long menuId);

  /**
   * 是否存在菜单子节点
   *
   * @param menuId 菜单ID
   * @return 结果 true 存在 false 不存在
   */
  boolean hasChildByMenuId(Long menuId);

  /**
   * 查询菜单是否存在角色
   *
   * @param menuId 菜单ID
   * @return 结果 true 存在 false 不存在
   */
  boolean checkMenuExistRole(Long menuId);

  /**
   * 新增保存菜单信息
   *
   * @param menu 菜单信息
   * @return 结果
   */
  long insertMenu(SysMenu menu);

  /**
   * 修改保存菜单信息
   *
   * @param menu 菜单信息
   * @return 结果
   */
  long updateMenu(SysMenu menu);

  /**
   * 删除菜单管理信息
   *
   * @param menuId 菜单ID
   * @return 结果
   */
  long deleteMenuById(Long menuId);

  /**
   * 校验菜单名称是否唯一
   *
   * @param menu 菜单信息
   * @return 结果
   */
  boolean checkMenuNameUnique(SysMenu menu);


  List<SysMenu> selectMenuTreeAll();


  List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly);
}
