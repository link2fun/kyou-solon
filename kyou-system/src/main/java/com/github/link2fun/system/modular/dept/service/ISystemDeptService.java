package com.github.link2fun.system.modular.dept.service;

import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.system.modular.dept.model.req.SysDeptAddReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptModifyReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;

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
   * @param context   上下文
   * @param searchReq 部门查询条件
   * @return 部门信息集合
   */
  List<SysDept> selectDeptList(final ActionContext context, SysDeptSearchReq searchReq);

  /**
   * 查询部门树结构信息
   *
   * @param context   上下文
   * @param searchReq 部门查询条件
   * @return 部门树信息集合
   */
  List<TreeSelect> selectDeptTreeList(final ActionContext context, SysDeptSearchReq searchReq);

  /**
   * 查询部门列表, 并排除指定部门及其所有后代
   *
   * <p>用于「上级部门」下拉: 不能把自己或自己的子孙选成上级, 否则 ancestors 会成环。
   * 后代判定按 ancestors 祖先链切分, 属于部门树语义, 不放在调用方拼装。
   *
   * @param context 上下文
   * @param deptId  要排除的部门ID
   * @return 可选的部门列表
   */
  List<SysDept> selectDeptListExcludingSubtree(final ActionContext context, Long deptId);

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
   * 按部门ID查询部门信息, 内部先做数据权限校验
   *
   * <p>面向「按 id 取单条」的接口, 越权校验与查询在同一次调用内完成;
   * 供其它 service 内部按 id 取实体时用 {@link #selectDeptById}, 那里不做权限判定。
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param deptId  部门ID
   * @return 部门信息
   */
  SysDept selectDeptDetail(ActionContext context, Long deptId);

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
   * @param addReq 新增部门请求
   * @return 结果
   */
  long insertDept(SysDeptAddReq addReq);

  /**
   * 修改保存部门信息(内部已完成数据权限/名称唯一性/父子关系/停用状态检查)
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param modifyReq 修改部门请求
   * @return 结果
   */
  long updateDept(final ActionContext context, SysDeptModifyReq modifyReq);

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
