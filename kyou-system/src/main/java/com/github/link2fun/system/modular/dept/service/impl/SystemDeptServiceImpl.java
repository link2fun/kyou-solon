package com.github.link2fun.system.modular.dept.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.basic.api.select.Query;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.framework.auth.DataScopeRowGuard;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.tree.TreeBuilder;
import com.github.link2fun.support.easyquery.DataScope;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.dept.model.req.SysDeptAddReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptModifyReq;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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

  @Inject
  private DataScopeRowGuard dataScopeRowGuard;

  @Db
  private EasyEntityQuery entityQuery;


  /**
   * 查询部门管理数据
   *
   * @param context   上下文
   * @param searchReq 部门查询条件
   * @return 部门信息集合
   */
  @Override
  public List<SysDept> selectDeptList(final ActionContext context, final SysDeptSearchReq searchReq) {

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
   * @param context   上下文
   * @param searchReq 查询条件
   * @return 部门树信息集合
   */
  @Override
  public List<TreeSelect> selectDeptTreeList(final ActionContext context, final SysDeptSearchReq searchReq) {
    final List<SysDept> deptList = selectDeptList(context, searchReq);
    return self.buildDeptTreeSelect(deptList);
  }

  /**
   * 查询部门列表, 并排除指定部门及其所有后代
   *
   * @param context 上下文
   * @param deptId  要排除的部门ID
   * @return 可选的部门列表
   */
  @Override
  public List<SysDept> selectDeptListExcludingSubtree(final ActionContext context, final Long deptId) {
    final List<SysDept> deptList = selectDeptList(context, new SysDeptSearchReq());
    // 自身 + ancestors 链中含该部门的即为子孙
    deptList.removeIf(dept -> deptId.equals(dept.getDeptId()) || containsAncestor(dept.getAncestors(), deptId));
    return deptList;
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
      .where(dept -> {
        dept.roleDeptList().any(roleDept -> roleDept.roleId().eq(roleId));
        if (role.getDeptCheckStrictly()) {
          // 严格勾选: 排除已勾选部门的父节点, 只回显该角色勾选的叶子
          Query<Long> checkedParentIds = entityQuery.queryable(SysDept.class)
            .where(_dept -> _dept.roleDeptList().any(_roleDept -> _roleDept.roleId().eq(roleId)))
            .selectColumn(SysDeptProxy::parentId);

          dept.deptId().notIn(checkedParentIds);
        }
      })
      .selectColumn(SysDeptProxy::deptId)
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
   * 按部门ID查询部门信息, 内部先做行准入校验
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param deptId  部门ID
   * @return 部门信息
   */
  @Override
  public SysDept selectDeptDetail(final ActionContext context, final Long deptId) {
    dataScopeRowGuard.assertDeptVisible(context, deptId);
    return selectDeptById(deptId);
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
   * 新增保存部门信息
   *
   * @param addReq 新增部门请求
   * @return 结果
   */
  @Transaction
  @Override
  public long insertDept(final SysDeptAddReq addReq) {
    final SysDept dept = BeanUtil.toBean(addReq, SysDept.class);
    checkDeptNameUnique("新增", dept);

    final SysDept parentDept = self.selectDeptById(dept.getParentId());
    if (ObjectUtil.isNull(parentDept)) {
      throw new ServiceException("上级部门不存在");
    }
    if (!StrUtil.equals(parentDept.getStatus(), UserConstants.DEPT_NORMAL)) {
      throw new ServiceException("部门停用，不允许新增");
    }

    dept.setAncestors(parentDept.getAncestors() + "," + dept.getParentId());

    return entityQuery.insertable(dept).executeRows();
  }

  /**
   * 修改保存部门信息
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param modifyReq 修改部门请求
   * @return 结果
   */
  @Transaction
  @Override
  public long updateDept(final ActionContext context, final SysDeptModifyReq modifyReq) {
    final SysDept dept = BeanUtil.toBean(modifyReq, SysDept.class);
    final Long deptId = dept.getDeptId();
    dataScopeRowGuard.assertDeptVisible(context, deptId);
    checkDeptNameUnique("修改", dept);
    if (dept.getParentId() == null) {
      throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能为空");
    }
    if (dept.getParentId().equals(deptId)) {
      throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
    }
    if (StrUtil.equals(UserConstants.DEPT_DISABLE, dept.getStatus()) && selectNormalChildrenDeptById(deptId) > 0) {
      throw new ServiceException("该部门包含未停用的子部门！");
    }

    if (dept.getParentId() == 0L) {
      // 根部门（parentId=0）没有上级记录, ancestors 固定为 "0"；但子孙的 ancestors 仍要跟着改写,
      // 否则 find_in_set 会继续沿旧父链命中, 部门树与数据权限都会错乱
      final SysDept oldDept = self.selectDeptById(deptId);
      dept.setAncestors("0");
      if (ObjectUtil.isNotNull(oldDept)) {
        updateDeptChildren(deptId, "0", oldDept.getAncestors());
      }
      return entityQuery.updatable(dept).setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS).executeRows();
    }

    SysDept newParentDept = self.selectDeptById(dept.getParentId());
    SysDept oldDept = self.selectDeptById(dept.getDeptId());
    if (ObjectUtil.isNull(newParentDept)) {
      throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不存在");
    }
    // 上级不能是自己的子孙：否则 ancestors 首尾相接成环, 树构建会栈溢出, find_in_set 递归查询也会失效
    if (containsAncestor(newParentDept.getAncestors(), deptId)) {
      throw new ServiceException("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己的下级部门");
    }
    if (ObjectUtil.isNotNull(oldDept)) {
      String newAncestors = newParentDept.getAncestors() + "," + newParentDept.getDeptId();
      String oldAncestors = oldDept.getAncestors();
      dept.setAncestors(newAncestors);
      updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
    }
    long result = entityQuery.updatable(dept).setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS).executeRows();
    if (UserConstants.DEPT_NORMAL.equals(dept.getStatus()) && StrUtil.isNotBlank(dept.getAncestors())
      && !StrUtil.equals("0", dept.getAncestors())) {
      // 如果该部门是启用状态，则启用该部门的所有上级部门
      updateParentDeptStatusNormal(dept.getAncestors());
    }
    return result;
  }

  /**
   * 判断 deptId 是否出现在上级链中（即 deptId 是 parent 的祖先）。
   *
   * <p>ancestors 形如 "0,100,101"，必须按逗号切分后逐个比较：
   * 字符串包含会把 1 误判成 10 的祖先，从而错误拦截合法的移动。
   *
   * @param ancestors 上级链
   * @param deptId    待检查的部门ID
   * @return 命中返回 true
   */
  static boolean containsAncestor(final String ancestors, final Long deptId) {
    if (StrUtil.isBlank(ancestors) || deptId == null) {
      return false;
    }
    final String target = String.valueOf(deptId);
    return StrUtil.split(ancestors, ',').stream()
      .anyMatch(item -> StrUtil.equals(item.trim(), target));
  }

  /**
   * 修改该部门的父级部门状态
   *
   * @param ancestors 当前部门的上级链, 形如 "0,100,101"
   */
  private void updateParentDeptStatusNormal(final String ancestors) {
    List<Long> deptIds = StrUtil.split(ancestors, ',').stream()
      .filter(StrUtil::isNotBlank)
      .map(Long::valueOf)
      .toList();
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
    if (StrUtil.isBlank(oldAncestors) || StrUtil.equals(oldAncestors, newAncestors)) {
      // 父链没变（如根部门之间互转）或旧值缺失：子孙的 ancestors 无需改写
      return;
    }
    List<SysDept> childrenList = self.selectChildrenDeptById(deptId);
    for (SysDept child : childrenList) {
      child.setAncestors(rebaseAncestors(child.getAncestors(), oldAncestors, newAncestors));
    }
    if (!childrenList.isEmpty()) {
      entityQuery.updatable(childrenList).setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS).executeRows();
    }
  }

  /**
   * 把子部门的 ancestors 从旧父链改写为新父链。
   *
   * <p>{@code replaceFirst} 的第一个参数是正则、第二个参数会解释 {@code $} 与 {@code \}，
   * 直接传裸字符串在 ancestors 含元字符时会静默改写错误，这里显式 quote。
   *
   * @param childAncestors 子部门原 ancestors，形如 {@code "0,100,101"}
   * @param oldAncestors   被移动部门的旧父链
   * @param newAncestors   被移动部门的新父链
   * @return 改写后的 ancestors；输入缺失时原样返回
   */
  static String rebaseAncestors(String childAncestors, String oldAncestors, String newAncestors) {
    if (StrUtil.isBlank(childAncestors) || StrUtil.isBlank(oldAncestors)) {
      return childAncestors;
    }
    return childAncestors.replaceFirst(Pattern.quote(oldAncestors), Matcher.quoteReplacement(newAncestors));
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
        dataScopeRowGuard.assertDeptVisible(context, id);
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
