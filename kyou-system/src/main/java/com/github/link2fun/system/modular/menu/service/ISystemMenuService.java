package com.github.link2fun.system.modular.menu.service;

import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.dto.SysMenuDTO;
import com.github.link2fun.system.domain.vo.RouterVo;
import com.github.link2fun.system.modular.menu.model.req.SysMenuAddReq;
import com.github.link2fun.system.modular.menu.model.req.SysMenuModifyReq;
import com.github.link2fun.system.modular.menu.model.req.SysMenuSearchReq;

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
  <T> List<T> selectMenuList(Long userId, Class<T> resultClass);

  /**
   * 根据用户查询系统菜单列表
   *
   * @param searchReq 查询条件
   * @param userId    用户ID
   * @return 菜单列表
   */
  <T> List<T> selectMenuList(SysMenuSearchReq searchReq, Long userId, Class<T> resultClass);

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
  List<SysMenuDTO> selectMenuTreeByUserId(Long userId);

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
  List<RouterVo> buildMenus(List<SysMenuDTO> menus);

  /**
   * 构建前端所需要树结构
   *
   * @param menus 菜单列表
   * @return 树结构列表
   */
  List<SysMenuDTO> buildMenuTree(List<SysMenuDTO> menus);

  /**
   * 构建前端所需要下拉树结构
   *
   * @param menus 菜单列表
   * @return 下拉树结构列表
   */
  List<TreeSelect> buildMenuTreeSelect(List<SysMenuDTO> menus);

  /**
   * 根据菜单ID查询信息
   *
   * @param menuId 菜单ID
   * @return 菜单信息
   */
  <T> T selectMenuById(Long menuId, Class<T> resultClass);

  /**
   * 新增保存菜单信息
   *
   * @param addReq 新增菜单请求
   * @return 结果
   */
  long insertMenu(SysMenuAddReq addReq);

  /**
   * 修改保存菜单信息
   *
   * @param modifyReq 修改菜单请求
   * @return 结果
   */
  long updateMenu(SysMenuModifyReq modifyReq);

  /**
   * 删除菜单管理信息
   *
   * @param menuId 菜单ID
   * @return 结果
   */
  long deleteMenuById(Long menuId);


  <T> List<T> selectMenuTreeAll(Class<T> resultClass);


  List<Long> selectMenuListByRoleId(Long roleId, boolean menuCheckStrictly);
}
