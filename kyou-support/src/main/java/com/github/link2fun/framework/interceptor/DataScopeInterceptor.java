package com.github.link2fun.framework.interceptor;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.tool.DataScopeTool;
import com.github.link2fun.support.annotation.DataScope;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.QueryEntity;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.utils.StringUtils;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;

import java.util.List;
import java.util.Objects;


/**
 * 数据过滤处理
 * <p> 通过注解@DataScope实现数据权限过滤
 *
 * @link <a href="https://solon.noear.org/article/617">@Around 使用说明（AOP）</a>
 */
public class DataScopeInterceptor implements Interceptor {

  /**
   * 数据范围过滤
   *
   * @param queryEntity 当前操作对象
   * @param context     用户
   * @param deptAlias   部门别名
   * @param userAlias   用户别名
   * @param permission  权限字符
   */
  public static void dataScopeFilter(final QueryEntity queryEntity, ActionContext context, String deptAlias, String userAlias, String permission) {
    final List<RoleDTO> roleList = context.getRoles();
    final String filterSQL = DataScopeTool.buildDataScopeFilterSQL(context, roleList, permission, deptAlias, userAlias);

    if (StrUtil.isNotBlank(filterSQL) && Objects.nonNull(queryEntity)) {
      queryEntity.getParams().setDataScope(filterSQL);
    }
  }


  protected void handleDataScope(final ActionContext context, final QueryEntity baseEntity, DataScope controllerDataScope) {
    // 获取当前的用户
    if (StringUtils.isNotNull(context.getSessionUser())) {
      // 如果是超级管理员，则不过滤数据

      if (!context.isAdmin()) {
        String permission = StringUtils.nullToDefault(controllerDataScope.permission(), "");
        dataScopeFilter(baseEntity, context, controllerDataScope.deptAlias(),
          controllerDataScope.userAlias(), permission);
      }
    }
  }

  /**
   * 拼接权限sql前先清空params.dataScope参数防止注入
   */
  private void clearDataScope(final QueryEntity queryEntity) {
    if (StringUtils.isNotNull(queryEntity)) {
      queryEntity.getParams().put(DataScopeTool.DATA_SCOPE, "");
    }
  }

  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(final Invocation inv) throws Throwable {
    final DataScope dataScope = inv.method().getAnnotation(DataScope.class);
    if (Objects.isNull(dataScope)) {
      // 不是通过配置的注解进来的, 直接放行
      return inv.invoke();
    }
    // 从参数中获取当前操作对象 ActionContext
    ActionContext actionContext = null;
    for (final Object arg : inv.args()) {
      if (arg instanceof ActionContext) {
        actionContext = (ActionContext) arg;
        break;
      }
    }
    if (Objects.isNull(actionContext)) {
      // 尝试从上下文获取
      actionContext = ActionContext.current();
      if (Objects.isNull(actionContext)) {
        // 没有找到 ActionContext 对象, 抛出异常
        throw new IllegalStateException("ActionContext 未传递, 无法进行数据权限过滤");
      }
    }


    for (Object entity : inv.args()) {
      if (entity instanceof final QueryEntity baseEntity) {
        clearDataScope(baseEntity);
        handleDataScope(actionContext, baseEntity, dataScope);
      }
    }

    return inv.invoke();
  }
}
