package com.github.link2fun.support.easyquery;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.proxy.AbstractProxyEntity;
import com.easy.query.core.proxy.SQLColumn;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRoleDept;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** 数据范围判定结果: 部门轴 {@link DeptScope} 与「仅本人」轴的组合, 应用到查询时两轴条件 OR 成一组。 */
public final class DataScope implements Serializable {

  /** 角色上的数据范围类型, 与 sys_role.data_scope 取值一一对应 */
  private enum ScopeType {
    /** 全部数据 */
    ALL("1"),
    /** 自定义数据 */
    CUSTOM("2"),
    /** 本部门数据 */
    DEPT("3"),
    /** 本部门及以下数据 */
    DEPT_AND_CHILD("4"),
    /** 仅本人数据 */
    SELF("5");

    private final String code;

    /** 绑定数据范围取值 */
    ScopeType(final String code) {
      this.code = code;
    }

    /** 按 sys_role.data_scope 的取值找类型, 未知取值返回 null */
    private static ScopeType ofCode(final String code) {
      for (final ScopeType type : values()) {
        if (type.code.equals(code)) {
          return type;
        }
      }
      return null;
    }
  }

  /** 过滤到空(不放行)时用的部门 ID, 与任何真实部门都不相等 */
  private static final long DENY_DEPT_ID = 0L;

  /** 部门轴作用域 */
  private final DeptScope dept;

  /** 「仅本人」授权的用户 ID; 无该类授权时为 null */
  private final Long selfUserId;

  /** 由两轴构造, 部门轴不可为 null */
  public DataScope(final DeptScope dept, final Long selfUserId) {
    this.dept = Objects.requireNonNull(dept, "dept scope must not be null");
    this.selfUserId = selfUserId;
  }

  /** 两轴均不过滤的作用域(未启用数据范围过滤时的缺省值)。 */
  public static DataScope ignoreFilter() {
    return new DataScope(DeptScope.ignoreFilter(), null);
  }

  /**
   * 按当前操作上下文解析数据范围。
   *
   * @param entityQuery 解析自定义 / 部门及以下范围用的查询器
   * @param context     操作上下文, 未登录时解析成过滤到空
   */
  public static DataScope of(final EasyEntityQuery entityQuery, final ActionContext context) {
    return of(entityQuery, context.getRoles(), context.getUserId(), context.getDeptId());
  }

  /**
   * 解析数据范围, 不依赖线程上下文。
   *
   * @param entityQuery 解析自定义 / 部门及以下范围用的查询器
   * @param roles       当前用户的角色集合, 空集合解析成过滤到空
   * @param userId      当前用户 ID, 1 是超管
   * @param deptId      当前部门 ID, 部门类范围的解析基准
   */
  public static DataScope of(final EasyEntityQuery entityQuery, final List<RoleDTO> roles,
                             final Long userId, final Long deptId) {
    // 1. 超管不过滤
    if (SysUser.isSuperAdmin(userId)) {
      return ignoreFilter();
    }

    // 2. 遍历角色归集各范围类型; 任一「全部数据」即整体不过滤
    final Set<Long> customRoleIds = new LinkedHashSet<>();
    boolean deptScope = false;
    boolean subtreeScope = false;
    boolean selfScope = false;
    if (CollectionUtil.isNotEmpty(roles)) {
      for (final RoleDTO role : roles) {
        final ScopeType scopeType = ScopeType.ofCode(role.getDataScope());
        if (Objects.isNull(scopeType)) {
          // 未知取值给不出授权, 该角色视同不授权
          continue;
        }
        switch (scopeType) {
          case ALL:
            return ignoreFilter();
          case CUSTOM:
            if (Objects.nonNull(role.getRoleId())) {
              customRoleIds.add(role.getRoleId());
            }
            break;
          case DEPT:
            deptScope = true;
            break;
          case DEPT_AND_CHILD:
            subtreeScope = true;
            break;
          case SELF:
            selfScope = Objects.nonNull(userId);
            break;
          default:
            break;
        }
      }
    }

    // 3. 部门类范围并成一份白名单: 本部门 + 本部门及以下 + 自定义勾选的部门
    final Set<Long> visibleDeptIds = new LinkedHashSet<>();
    if (Objects.nonNull(deptId)) {
      if (deptScope) {
        visibleDeptIds.add(deptId);
      }
      if (subtreeScope) {
        visibleDeptIds.addAll(subtreeDeptIds(entityQuery, deptId));
      }
    }
    if (!customRoleIds.isEmpty()) {
      visibleDeptIds.addAll(entityQuery.queryable(SysRoleDept.class)
        .where(roleDept -> roleDept.roleId().in(customRoleIds))
        .selectColumn(SysRoleDeptProxy::deptId)
        .toList());
    }

    // 4. 组装两轴, 有「仅本人」角色时记下用户 ID
    return new DataScope(DeptScope.filterByDeptIds(visibleDeptIds), selfScope ? userId : null);
  }

  /** 解析「本部门及以下」: 本部门自身加上祖先链上挂着它的部门 */
  private static List<Long> subtreeDeptIds(final EasyEntityQuery entityQuery, final Long deptId) {
    return entityQuery.queryable(SysDept.class)
      .where(dept -> dept.or(() -> {
        dept.deptId().eq(deptId);
        dept.expression().rawSQLCommand("find_in_set({0}, ancestors)", deptId);
      }))
      .selectColumn(SysDeptProxy::deptId)
      .toList();
  }

  /**
   * 把数据范围应用到查询上。
   *
   * @param owner      条件分组挂在查询的哪个代理上
   * @param deptColumn 查询里的部门列(如 {@code dept.deptId()})
   * @param userColumn 查询里的用户列(如 {@code user.userId()}), 查询里没有用户表时传 null
   */
  public void applyTo(final AbstractProxyEntity<?, ?> owner,
                      final SQLColumn<?, Long> deptColumn, final SQLColumn<?, Long> userColumn) {
    if (!filters()) {
      return;
    }
    final boolean deptApplies = dept.shouldFilter() && !dept.visibleDeptIds().isEmpty();
    final boolean selfApplies = Objects.nonNull(selfUserId) && Objects.nonNull(userColumn);
    if (deptApplies && selfApplies) {
      // 白名单与本人授权同时在场, OR 成一组
      owner.or(() -> {
        deptColumn.in(dept.visibleDeptIds());
        userColumn.eq(selfUserId);
      });
      return;
    }
    if (deptApplies) {
      deptColumn.in(dept.visibleDeptIds());
      return;
    }
    if (selfApplies) {
      // 白名单为空(过滤到空)但本人授权在场: 本人条件独自救场
      userColumn.eq(selfUserId);
      return;
    }
    // 部门白名单为空且本人条件给不出: 恒假, 与任何真实部门都不相等
    deptColumn.eq(DENY_DEPT_ID);
  }

  /** 部门轴作用域(断言与点查用) */
  public DeptScope deptScope() {
    return dept;
  }

  /** 是否会给查询加条件, 部门轴过滤(含过滤到空)或有本人授权都是 true */
  public boolean filters() {
    return dept.shouldFilter() || Objects.nonNull(selfUserId);
  }

  /** 指定部门是否可见(点查, 不跑查询): 不过滤恒可见; 过滤时空 ID 不可见, 仅白名单内可见。 */
  public boolean containsDept(final Long deptId) {
    return dept.contains(deptId);
  }

  /** 指定用户是否可见(点查, 不跑查询): 不过滤恒可见; 是本人, 或所属部门在白名单内可见。 */
  public boolean containsUser(final Long targetUserId, final Long targetDeptId) {
    if (!filters()) {
      return true;
    }
    if (Objects.nonNull(selfUserId) && selfUserId.equals(targetUserId)) {
      return true;
    }
    return dept.contains(targetDeptId);
  }
}
