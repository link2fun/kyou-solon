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
   * 根据ID查询所有子部门（正常状态）
   *
   * @param deptId 部门ID
   * @return 子部门数
   */
  long selectNormalChildrenDeptById(Long deptId);

  /**
   * 是否存在部门子节点
   *
   * @param deptId 部门ID
   * @return 结果
   */
  boolean hasChildByDeptId(Long deptId);

  /**
   * 查询部门是否存在用户
   *
   * @param deptId 部门ID
   * @return 结果 true 存在 false 不存在
   */
  boolean checkDeptExistUser(Long deptId);

  /**
   * 校验部门名称是否唯一
   *
   * @param dept 部门信息
   * @return 结果
   */
  boolean checkDeptNameUnique(SysDept dept);

  /**
   * 校验部门是否有数据权限
   *
   * @param context
   * @param deptId  部门id
   */
  void checkDeptDataScope(final ActionContext context, Long deptId);

  /**
   * 新增保存部门信息
   *
   * @param dept 部门信息
   * @return 结果
   */
  long insertDept(SysDept dept);

  /**
   * 修改保存部门信息
   *
   * @param dept 部门信息
   * @return 结果
   */
  long updateDept(SysDept dept);

  /**
   * 删除部门管理信息
   *
   * @param deptId 部门ID
   * @return 结果
   */
  boolean deleteDeptById(Long deptId);

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
