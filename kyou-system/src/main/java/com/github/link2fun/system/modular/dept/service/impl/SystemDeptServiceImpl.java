package com.github.link2fun.system.modular.dept.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.base.LongProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.text.Convert;
import com.github.link2fun.support.core.tree.TreeBuilder;
import com.github.link2fun.support.easyquery.DataScope;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collections;
import java.util.List;


@Slf4j
@Component
public class SystemDeptServiceImpl implements ISystemDeptService {

  /**
   * 注入自身
   *
   * @link <a href="https://solon.noear.org/article/442">十、动态代理的本质与边界</a>
   */
  @Inject
  private ISystemDeptService self;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemUserService userService;

  @Inject
  private AggregateDeleter aggregateDeleter;

  @Db
  private EasyEntityQuery entityQuery;


  /**
   * 判断当前用户是否有某个部门的操作权限
   */
  @Override
  public boolean hasDeptPermission(final ActionContext context, final Long deptId) {
    return DataScope.of(entityQuery, context).containsDept(deptId);
  }

  /**
   * 查询部门管理数据
   *
   * @param context   上下文
   * @param searchReq 部门信息
   * @return 部门信息集合
   */
  @Override
  public List<SysDept> selectDeptList(final ActionContext context, final SysDept searchReq) {

    final DataScope dataScope = DataScope.of(entityQuery, context);
    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(_dept -> _dept.delFlag().eq(UserConstants.DEPT_NORMAL))
      .where(_dept -> _dept.deptId().eq(IdUtils.isIdValid(searchReq.getDeptId()), searchReq.getDeptId()))
      .where(_dept -> _dept.parentId().eq(IdUtils.isIdValid(searchReq.getParentId()), searchReq.getParentId()))
      .where(_dept -> _dept.deptName().like(StrUtil.isNotBlank(searchReq.getDeptName()), searchReq.getDeptName()))
      .where(_dept -> _dept.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()))
      .where(_dept -> dataScope.applyTo(_dept, _dept.deptId(), null))
      .orderBy(dept -> {
        dept.parentId().asc();
        dept.orderNum().asc();
      })
      .toList();
  }

  /**
   * 查询部门树结构信息
   *
   * @param context 上下文
   * @param dept    部门信息
   * @return 部门树信息集合
   */
  @Override
  public List<TreeSelect> selectDeptTreeList(final ActionContext context, final SysDept dept) {
    final List<SysDept> deptList = selectDeptList(context, dept);
    return self.buildDeptTreeSelect(deptList);
  }

  /**
   * 构建前端所需要树结构
   *
   * @param depts 部门列表
   * @return 树结构列表
   */
  @Override
  public List<SysDept> buildDeptTree(final List<SysDept> depts) {
    return TreeBuilder.build(depts, SysDept::getDeptId, SysDept::getParentId, SysDept::setChildren);
  }

  /**
   * 构建前端所需要下拉树结构
   *
   * @param depts 部门列表
   * @return 下拉树结构列表
   */
  @Override
  public List<TreeSelect> buildDeptTreeSelect(final List<SysDept> depts) {
    final List<SysDept> deptTreeList = self.buildDeptTree(depts);
    return deptTreeList.stream().map(TreeSelect::new).toList();
  }

  /**
   * 根据角色ID查询部门树信息
   *
   * @param roleId 角色ID
   * @return 选中部门列表
   */
  @Override
  public List<Long> selectDeptListByRoleId(final Long roleId) {
    final SysRole role = roleService.selectRoleById(roleId, SysRole.class);

    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .leftJoin(SysRoleDept.class, (dept, roleDept) -> dept.deptId().eq(roleDept.deptId()))
      .where((dept, roleDept) -> roleDept.roleId().eq(roleId))
      .where((dept, roleDept) -> {
        if (role.getDeptCheckStrictly()) {
          // select dept.parent_id from sys_dept d
          //    //          inner join sys_role_dept rd on dept.dept_id = role_dept.dept_id and role_dept.role_id = #{roleId}
          EntityQueryable<LongProxy, Long> subQuery = entityQuery.queryable(SysDept.class).asAlias("d")
            .innerJoin(SysRoleDept.class, (_dept, _roleDept) -> {
              _dept.deptId().eq(_roleDept.deptId());
              _roleDept.roleId().eq(roleId);
            })
            .select((_dept, _roleDept) -> new LongProxy(_dept.deptId()));

          dept.deptId().notIn(subQuery);
        }
      })
      .selectColumn((dept, roleDept) -> dept.deptId())
      .toList();


  }

  /**
   * 根据部门ID查询信息
   *
   * @param deptId 部门ID
   * @return 部门信息
   */
  @Override
  public SysDept selectDeptById(final Long deptId) {
    return entityQuery.queryable(SysDept.class)
      .whereById(deptId)
      .singleOrNull();
  }

  /**
   * 根据ID查询所有子部门（正常状态）
   *
   * @param deptId 部门ID
   * @return 子部门数
   */
  private long selectNormalChildrenDeptById(final Long deptId) {

    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(_dept -> _dept.status().eq(UserConstants.NORMAL))
      .where(_dept -> _dept.delFlag().eq(UserConstants.NORMAL))
      .where(_dept -> _dept.expression().sql("find_in_set({0}, ancestors)", c -> c.value(deptId)))
      .count();
  }

  /**
   * 是否存在部门子节点
   *
   * @param deptId 部门ID
   * @return 结果
   */
  private boolean hasChildByDeptId(final Long deptId) {

    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(_dept -> _dept.parentId().eq(deptId))
      .where(_dept -> _dept.delFlag().eq(UserConstants.NORMAL))
      .any();
  }

  /**
   * 查询部门是否存在用户
   *
   * @param deptId 部门ID
   * @return 结果 true 存在 false 不存在
   */
  private boolean checkDeptExistUser(final Long deptId) {
    return userService.checkDeptExistUser(deptId);
  }

  /**
   * 校验部门名称唯一性, 不唯一时抛出业务异常
   *
   * @param action 操作描述(如 "新增"/"修改"), 用于拼接错误文案
   * @param dept   部门信息
   */
  private void checkDeptNameUnique(final String action, final SysDept dept) {
    UniqueChecker.checkOccupied(entityQuery.queryable(SysDept.class)
        .where(_dept -> {
          _dept.deptName().eq(dept.getDeptName());
          _dept.parentId().eq(dept.getParentId());
          _dept.delFlag().eq(UserConstants.NORMAL);
          _dept.deptId().ne(IdUtils.isIdValid(dept.getDeptId()), dept.getDeptId());
        }).any(),
      () -> action + "部门'" + dept.getDeptName() + "'失败，部门名称已存在");
  }

  /**
   * 校验部门是否有数据权限
   *
   * @param context 数据权限上下文
   * @param deptId  部门id
   */
  @Override
  public void checkDeptDataScope(final ActionContext context, final Long deptId) {

    if (self.hasDeptPermission(context, deptId)) {
      return;
    }
    throw new ServiceException("没有权限访问部门数据！");

  }

  /**
   * 新增保存部门信息
   *
   * @param dept 部门信息
   * @return 结果
   */
  @Transaction
  @Override
  public long insertDept(final SysDept dept) {
    checkDeptNameUnique("新增", dept);

    final SysDept parentDept = self.selectDeptById(dept.getParentId());
    if (!StrUtil.equals(parentDept.getStatus(), UserConstants.DEPT_NORMAL)) {
      throw new ServiceException("部门停用，不允许新增");
    }

    dept.setAncestors(parentDept.getAncestors() + "," + dept.getParentId());

    return entityQuery.insertable(dept).executeRows();
  }

  /**
   * 修改保存部门信息
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param dept    部门信息
   * @return 结果
   */
  @Transaction
  @Override
  public long updateDept(final ActionContext context, final SysDept dept) {
    final Long deptId = dept.getDeptId();
    checkDeptDataScope(context, deptId);
    checkDeptNameUnique("修改", dept);
    if (dept.getParentId().equals(deptId)) {
      throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
    }
    if (StrUtil.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && selectNormalChildrenDeptById(deptId) > 0) {
      throw new ServiceException("该部门包含未停用的子部门！");
    }

    SysDept newParentDept = self.selectDeptById(dept.getParentId());
    SysDept oldDept = self.selectDeptById(dept.getDeptId());
    if (ObjectUtil.isNotNull(newParentDept) && ObjectUtil.isNotNull(oldDept)) {
      String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
      String oldAncestors = oldDept.getAncestors();
      dept.setAncestors(newAncestors);
      updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
    }
    long result = entityQuery.updatable(dept).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StrUtil.isNotBlank(dept.getAncestors())
      && !StrUtil.equals("0", dept.getAncestors())) {
      // 如果该部门是启用状态，则启用该部门的所有上级部门
      updateParentDeptStatusNormal(dept);
    }
    return result;
  }

  /**
   * 修改该部门的父级部门状态
   *
   * @param dept 当前部门
   */
  private void updateParentDeptStatusNormal(SysDept dept) {
    String ancestors = dept.getAncestors();
    List<Long> deptIds = List.of(Convert.toLongArray(ancestors));
    self.updateDeptStatusNormal(deptIds);
  }

  /**
   * 修改子元素关系
   *
   * @param deptId       被修改的部门ID
   * @param newAncestors 新的父ID集合
   * @param oldAncestors 旧的父ID集合
   */
  public void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
    List<SysDept> childrenList = self.selectChildrenDeptById(deptId);
    for (SysDept child : childrenList) {
      child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
    }
    if (!childrenList.isEmpty()) {
      entityQuery.updatable(childrenList).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    }
  }

  /**
   * 删除部门管理信息
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param deptId  部门ID
   * @return 结果
   */
  @Transaction
  @Override
  public boolean deleteDeptById(final ActionContext context, final Long deptId) {

    return aggregateDeleter.delete(AggregateDeletePlan.of(Collections.singletonList(deptId))
      .guardPerId(id -> {
        if (hasChildByDeptId(id)) {
          throw new ServiceException("存在下级部门,不允许删除");
        }
        if (checkDeptExistUser(id)) {
          throw new ServiceException("部门存在用户,不允许删除");
        }
        checkDeptDataScope(context, id);
      })
      // 软删: 置删除标记
      .delete(ids -> entityQuery.updatable(SysDept.class)
        .setColumns(dept -> dept.delFlag().set(UserConstants.DEPT_DELETED))
        .where(dept -> dept.deptId().in(ids))
        .executeRows())) > 0;
  }

  /**
   * 修改所在部门正常状态
   *
   * @param deptIds 部门ID组
   */
  @Override
  public void updateDeptStatusNormal(final List<Long> deptIds) {

    entityQuery.updatable(SysDept.class)
      .setColumns(dept -> dept.status().set(UserConstants.DEPT_NORMAL))
      .where(dept -> dept.deptId().in(deptIds))
      .executeRows();
  }

  /**
   * 根据ID查询所有子部门
   *
   * @param deptId 部门ID
   * @return 部门列表
   */
  @Override
  public List<SysDept> selectChildrenDeptById(final Long deptId) {

    return entityQuery.queryable(SysDept.class).asAlias(SysDept.TABLE_ALIAS)
      .where(_dept -> _dept.expression().sql("find_in_set({0}, ancestors)", c -> c.value(deptId)))
      .toList();
  }
}
