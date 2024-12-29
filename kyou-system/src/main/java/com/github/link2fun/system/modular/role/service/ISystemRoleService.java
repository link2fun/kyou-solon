package com.github.link2fun.system.modular.role.service;

import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.role.model.req.SysRoleAddReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeDataScopeReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeStatusReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleModifyReq;

import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author ruoyi
 */
public interface ISystemRoleService {
  /**
   * 根据条件分页查询角色数据
   *
   * @param page        分页信息
   * @param searchReq   角色信息
   * @param resultClass
   * @return 角色数据集合信息
   */
  <T> Page<T> selectRoleList(ActionContext context, Page<SysRole> page, SysRole searchReq, Class<T> resultClass);

  /**
   * 根据用户ID查询角色列表
   *
   * @param userId 用户ID
   * @return 角色列表
   */
  List<RoleDTO> selectRolesByUserId(Long userId);

  /**
   * 根据用户ID查询角色权限
   *
   * @param userId 用户ID
   * @return 权限列表
   */
  Set<String> selectRolePermissionByUserId(Long userId);

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  List<SysRole> selectRoleAll(ActionContext context);


  /**
   * 通过角色ID查询角色
   *
   * @param roleId      角色ID
   * @param resultClass 返回结果类型
   * @return 角色对象信息
   */
  <T> T selectRoleById(Long roleId, Class<T> resultClass);

  /**
   * 校验角色名称是否唯一
   *
   * @param roleName 角色信息
   * @param roleId 角色ID
   * @return 结果
   */
  boolean isRoleNameUnique(String roleName, Long roleId);

  /**
   * 校验角色权限是否唯一
   *
   * @param roleKey
   * @param roleId  TODO
   * @return 结果
   */
  boolean isRoleKeyUnique(String roleKey, Long roleId);

  /**
   * 校验角色是否允许操作
   *
   * @param roleId 角色信息
   */
  void checkRoleAllowed(Long roleId);

  /**
   * 校验角色是否有数据权限
   *
   * @param roleId 角色id
   */
  void checkRoleDataScope(Long roleId);

  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  long countUserRoleByRoleId(Long roleId);

  /**
   * 新增保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  long insertRole(SysRoleAddReq role);

  /**
   * 修改保存角色信息
   *
   * @param modifyReq 角色信息
   * @return 结果
   */
  long updateRole(SysRoleModifyReq modifyReq);

  /**
   * 修改角色状态
   *
   * @param changeStatusReq 角色信息
   * @return 结果
   */
  long changeRoleStatus(SysRoleChangeStatusReq changeStatusReq);

  /**
   * 修改数据权限信息
   *
   * @param changeDataScopeReq 修改数据权限请求
   * @return 结果
   */
  long authDataScope(SysRoleChangeDataScopeReq changeDataScopeReq);

  /**
   * 批量删除角色信息
   *
   * @param roleIds 需要删除的角色ID
   * @return 结果
   */
  long deleteRoleByIds(List<Long> roleIds);

  /** 取消授权用户角色 */
  boolean removeUserRoleMapping(Long userId, Long roleId);

  /**
   * 批量取消授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要取消授权的用户数据ID
   * @return 结果
   */
  boolean deleteAuthUsers(Long roleId, List<Long> userIds);

  /**
   * 批量选择授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要删除的用户数据ID
   * @return 结果
   */
  Boolean insertAuthUsers(Long roleId, List<Long> userIds);

  /**
   * 根据角色关键字查询角色信息
   *
   * @param roleKey 角色关键字
   * @return 返回角色信息
   */
  SysRole selectRoleByRoleKey(String roleKey);

  List<SysRole> listByIds(List<Long> roleIdList);

}
