package com.github.link2fun.system.modular.dept.service;

import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysDept;

import java.util.List;

/**
 * 部门管理 服务层
 *
 * @author ruoyi
 */
public interface ISystemDeptService {

  /** 判断当前用户是否有某个部门的操作权限 */
  boolean hasDeptPermission(final ActionContext context, Long deptId);

  /**
   * 查询部门管理数据
   *
   * @param context 上下文
   * @param dept    部门信息
   * @return 部门信息集合
   */
  List<SysDept> selectDeptList(final ActionContext context, SysDept dept);

  /**
   * 查询部门树结构信息
   *
   * @param context 上下文
   * @param dept    部门信息
   * @return 部门树信息集合
   */
  List<TreeSelect> selectDeptTreeList(final ActionContext context, SysDept dept);

  /**
   * 构建前端所需要树结构
   *
   * @param depts 部门列表
   * @return 树结构列表
   */
  List<SysDept> buildDeptTree(List<SysDept> depts);

  /**
   * 构建前端所需要下拉树结构
   *
   * @param depts 部门列表
   * @return 下拉树结构列表
   */
  List<TreeSelect> buildDeptTreeSelect(List<SysDept> depts);

  /**
   * 根据角色ID查询部门树信息
   *
   * @param roleId 角色ID
   * @return 选中部门列表
   */
  List<Long> selectDeptListByRoleId(Long roleId);

  /**
   * 根据部门ID查询信息
   *
   * @param deptId 部门ID
   * @return 部门信息
   */
  SysDept selectDeptById(Long deptId);

  /**
   * 校验部门是否有数据权限
   * <p>仅用于查询类接口(getInfo)的越权防护; 编辑类操作的前置检查已在各执行方法内部完成</p>
   *
   * @param context
   * @param deptId  部门id
   */
  void checkDeptDataScope(final ActionContext context, Long deptId);

  /**
   * 新增保存部门信息(内部已完成部门名称唯一性检查)
   *
   * @param dept 部门信息
   * @return 结果
   */
  long insertDept(SysDept dept);

  /**
   * 修改保存部门信息(内部已完成数据权限/名称唯一性/父子关系/停用状态检查)
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param dept    部门信息
   * @return 结果
   */
  long updateDept(final ActionContext context, SysDept dept);

  /**
   * 删除部门管理信息(内部已完成下级部门/部门用户/数据权限检查)
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param deptId  部门ID
   * @return 结果
   */
  boolean deleteDeptById(final ActionContext context, Long deptId);

  /**
   * 修改所在部门正常状态
   *
   * @param deptIds 部门ID组
   */
  void updateDeptStatusNormal(List<Long> deptIds);

  /**
   * 根据ID查询所有子部门
   *
   * @param deptId 部门ID
   * @return 部门列表
   */
  List<SysDept> selectChildrenDeptById(Long deptId);
}
