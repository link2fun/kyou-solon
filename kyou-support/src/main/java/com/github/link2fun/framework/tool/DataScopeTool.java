package com.github.link2fun.framework.tool;

import cn.dev33.satoken.strategy.SaStrategy;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 数据范围工具类<br/>
 * 1. 用于构建数据范围过滤SQL语句
 * 
 * @author link2fun
 */
public class DataScopeTool {
  /** 全部数据权限 */
  public static final String DATA_SCOPE_ALL = "1";
  /** 自定数据权限 */
  public static final String DATA_SCOPE_CUSTOM = "2";
  /** 部门数据权限 */
  public static final String DATA_SCOPE_DEPT = "3";
  /** 部门及以下数据权限 */
  public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
  /** 仅本人数据权限 */
  public static final String DATA_SCOPE_SELF = "5";
  /** 数据权限过滤关键字 */
  public static final String DATA_SCOPE = "dataScope";

  /**
   * 构建部门数据范围过滤SQL语句。<br/>
   * 使用上下文用户的角色列表和默认的部门表别名, 且不使用功能权限过滤。
   *
   * @param context 上下文对象，包含当前用户等信息
   * @return 构建的部门数据范围过滤SQL语句
   */
  public static String buildDeptDataScopeFilterSQL(final ActionContext context) {
    return buildDataScopeFilterSQL(context, context.getCurrentUserNotNull().getUser().getRoles(), "",
        SysDept.TABLE_ALIAS, "");
  }

  /**
   * 构建数据范围过滤SQL语句。<br/>
   * 使用上下文用户的角色列表和默认的部门表别名和用户表别名。
   *
   * @param context            上下文对象，包含当前用户等信息
   * @param permissionRequired 需要的权限，上下文中的用户所拥有的角色需要包含该权限
   * @return 构建的数据范围过滤SQL语句
   */

  public static String buildDataScopeFilterSQL(final ActionContext context, final String permissionRequired) {
    final SessionUser currentUser = context.getCurrentUserNotNull();
    return buildDataScopeFilterSQL(context, currentUser.getUser().getRoles(), permissionRequired);
  }

  /**
   * 构建数据范围过滤SQL语句。<br/>
   * 使用默认的部门表别名和用户表别名。
   *
   * @param context            上下文对象，包含当前用户等信息
   * @param roleListProvided   提供的角色列表，用于确定数据范围
   * @param permissionRequired 需要的权限，上下文中的用户需要包含该权限
   * @return 构建的数据范围过滤SQL语句
   */
  public static String buildDataScopeFilterSQL(final ActionContext context, final List<RoleDTO> roleListProvided,
      final String permissionRequired) {
    return buildDataScopeFilterSQL(context, roleListProvided, permissionRequired,
        SysDept.TABLE_ALIAS, SysUser.TABLE_ALIAS);
  }

  /**
   * 构建数据范围过滤SQL语句。
   *
   * @param context            上下文对象，包含当前用户等信息
   * @param roleListProvided   提供的角色列表，用于确定数据范围
   * @param permissionRequired 需要的权限，上下文中的用户需要包含该权限
   * @param deptAlias          部门别名，用于构建SQL语句
   * @param userAlias          用户别名，用于构建SQL语句
   * @return 构建的数据范围过滤SQL语句
   */
  public static String buildDataScopeFilterSQL(final ActionContext context, final List<RoleDTO> roleListProvided,
      final String permissionRequired,
      final String deptAlias, final String userAlias) {
    if (context.isAdmin()) {
      // 如果是超级管理员，不过滤数据
      return "";
    }

    StringBuilder sqlString = new StringBuilder();
    List<String> conditions = new ArrayList<>();

    if (CollectionUtil.isNotEmpty(roleListProvided)) {
      for (RoleDTO role : roleListProvided) {
        // 如果角色中包含 admin 也视为管理员, 不过滤数据权限
        if (StrUtil.equalsAnyIgnoreCase(role.getRoleKey(), "admin")) {
          return "";
        }

        String dataScope = role.getDataScope();

        if (!DATA_SCOPE_CUSTOM.equals(dataScope) && conditions.contains(dataScope)) {
          // 不是自定义数据权限并且conditions已经存在该数据权限，跳过
          continue;
        }
        if (StringUtils.isNotBlank(permissionRequired) // 需要权限过滤
            && CollectionUtil.isNotEmpty(role.getPermissions()) // 角色权限不为空
            && !SaStrategy.instance.hasElement.apply(role.getPermissions(), permissionRequired) // 角色权限不包含传递过来的权限字符
        ) {
          // 如果有设置权限字符并且角色的权限字符不包含传递过来的权限字符，跳过
          continue;
        }
        if (DATA_SCOPE_ALL.equals(dataScope)) {
          // 全部数据 无需添加条件
          sqlString = new StringBuilder();
          conditions.add(dataScope);
          break;
        } else if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
          // 自定义数据权限
          sqlString.append(StringUtils.format(
              " OR {}.dept_id IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ", deptAlias,
              role.getRoleId()));

        } else if (DATA_SCOPE_DEPT.equals(dataScope)) {
          // 部门数据权限

          sqlString.append(StringUtils.format(" OR {}.dept_id = {} ", deptAlias, context.getDeptId()));
        } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
          // 部门及以下数据权限
          sqlString.append(StringUtils.format(
              " OR {}.dept_id IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} or find_in_set( {} , ancestors ) )",
              deptAlias, context.getDeptId(), context.getDeptId()));

        } else if (DATA_SCOPE_SELF.equals(dataScope)) {
          // 仅本人数据权限
          if (StringUtils.isNotBlank(userAlias)) {
            sqlString.append(StringUtils.format(" OR {}.user_id = {} ", userAlias, context.getUserId()));
          } else {
            // 数据权限为仅本人且没有userAlias别名不查询任何数据
            sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
          }
        }
        conditions.add(dataScope);
      }

    }

    // 多角色情况下，所有角色都不包含传递过来的权限字符，这个时候sqlString也会为空，所以要限制一下,不查询任何数据
    if (StringUtils.isEmpty(conditions)) {
      sqlString.append(StringUtils.format(" OR {}.dept_id = 0 ", deptAlias));
    }

    return "(" + StrUtil.removePrefix(StrUtil.trimToEmpty(sqlString.toString()), "OR") + ")";
  }

  /** 是否是超级管理员, 超级管理员用户 ID 为 1 */
  public static boolean isAdmin(final Long userId) {
    return Objects.nonNull(userId) && 1L == userId;
  }
}
