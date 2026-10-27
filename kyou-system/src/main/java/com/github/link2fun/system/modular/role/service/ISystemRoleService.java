package com.github.link2fun.system.modular.role.service;

import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.role.model.req.*;

import java.util.List;

/**
 * 角色业务层
 *
 * @author ruoyi
 */
public interface ISystemRoleService {
  /**
   * 根据条件分页查询角色数据
   *
   * @param pageRequest 分页信息
   * @param searchReq   角色查询条件
   * @param resultClass
   * @return 角色数据集合信息
   */
  <T> Page<T> selectRoleList(ActionContext context, Page<SysRole> pageRequest, SysRoleSearchReq searchReq, Class<T> resultClass);

  /**
   * 根据用户ID查询角色列表
   *
   * @param userId 用户ID
   * @return 角色列表
   */
  List<RoleDTO> selectRolesByUserId(Long userId);

  /**
   * 查询目标用户可持有的角色列表
   *
   * <p>非超管用户不能持有超管角色, 因此目标用户不是超管时结果里会剔除超管角色。
   *
   * @param userId 目标用户ID
   * @return 可持有的角色列表
   */
  List<RoleDTO> selectAssignableRolesByUserId(Long userId);

  /**
   * 查询用户持有的启用角色标识集合(停用/软删角色不参与授权)
   *
   * @param userId 用户ID
   * @return 启用角色的 roleKey 集合
   */
  List<String> listEnabledRoleKeysByUserId(Long userId);

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  List<SysRole> selectRoleAll(ActionContext context);

  /**
   * 查询可授予目标用户的角色
   *
   * <p>非超管用户不能持有超管角色, 因此目标用户不是超管时结果里会剔除超管角色。
   * 该规则属于授权语义, 不放在调用方拼接。
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userId  目标用户ID
   * @return 可分配的角色列表
   */
  List<SysRole> selectAssignableRoles(ActionContext context, Long userId);


  /**
   * 通过角色ID查询角色
   *
   * @param roleId      角色ID
   * @param resultClass 返回结果类型
   * @return 角色对象信息
   */
  <T> T selectRoleById(Long roleId, Class<T> resultClass);

  /**
   * 按角色ID查询角色详情, 内部先做数据权限校验
   *
   * <p>面向「按 id 取单条」的接口, 越权校验与查询在同一次调用内完成;
   * 供其它 service 内部按 id 取实体时用 {@link #selectRoleById}, 那里不做权限判定。
   *
   * @param roleId      角色ID
   * @param resultClass 返回结果类型
   * @return 角色对象信息
   */
  <T> T selectRoleDetail(Long roleId, Class<T> resultClass);

  /**
   * 校验角色是否有数据权限
   * <p>仅用于查询类接口(getInfo)的越权防护; 编辑类操作的前置检查已在各执行方法内部完成</p>
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
   * 新增保存角色信息(内部已完成名称/权限字符唯一性检查)
   *
   * @param role 角色信息
   * @return 结果
   */
  long insertRole(SysRoleAddReq role);

  /**
   * 修改保存角色信息(内部已完成操作权限/数据权限/唯一性检查)
   *
   * @param modifyReq 修改角色请求
   * @return 结果
   */
  long updateRole(SysRoleModifyReq modifyReq);

  /**
   * 修改角色状态(内部已完成操作权限/数据权限检查)
   *
   * @param changeStatusReq 角色状态变更请求
   * @return 结果
   */
  long changeRoleStatus(SysRoleChangeStatusReq changeStatusReq);

  /**
   * 修改数据权限信息(内部已完成操作权限/数据权限检查)
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

  /** 取消单个用户的角色授权 */
  void unassignUser(Long roleId, Long userId);

  /** 批量取消授权用户角色 */
  void unassignUsers(Long roleId, List<Long> userIds);

  /** 批量为角色授权用户(内部已完成数据权限检查) */
  void assignUsers(Long roleId, List<Long> userIds);

  /**
   * 根据角色关键字查询角色信息
   *
   * @param roleKey 角色关键字
   * @return 返回角色信息
   */
  SysRole selectRoleByRoleKey(String roleKey);

  List<SysRole> listByIds(List<Long> roleIdList);

}
