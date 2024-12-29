package com.github.link2fun.framework.config.easyquery;

import com.easy.query.core.basic.extension.interceptor.EntityInterceptor;
import com.easy.query.core.expression.sql.builder.EntityInsertExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityUpdateExpressionBuilder;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.text.Convert;
import com.github.link2fun.support.utils.reflect.ReflectUtils;
import org.noear.solon.annotation.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class EasyQueryEntityInterceptor implements EntityInterceptor {
  private final static String DEFAULT_ACTION_USER = "system";
  @Override
  public void configureInsert(Class<?> entityClass, EntityInsertExpressionBuilder entityInsertExpressionBuilder, Object entity) {

    String actionUser = Optional.ofNullable(ActionContext.current()).map(ActionContext::getSessionUser).map(SessionUser::getUsername).map(Convert::toStr).orElse(DEFAULT_ACTION_USER);


    LocalDateTime now = LocalDateTime.now();
    ReflectUtils.setFieldValueIfExists(entity, Fields.createTime, now);
    ReflectUtils.setFieldValueIfExists(entity, Fields.createBy, actionUser);
    ReflectUtils.setFieldValueIfExists(entity, Fields.updateTime, now);
    ReflectUtils.setFieldValueIfExists(entity, Fields.updateBy,actionUser);
    ReflectUtils.setFieldValueIfExists(entity, Fields.deleteFlag, UserConstants.NORMAL);
    ReflectUtils.setFieldValueIfExists(entity, Fields.revision, 0);

  }

  @Override
  public void configureUpdate(Class<?> entityClass, EntityUpdateExpressionBuilder entityUpdateExpressionBuilder, Object entity) {
    String actionUser = Optional.ofNullable(ActionContext.current()).map(ActionContext::getSessionUser).map(SessionUser::getUsername).map(Convert::toStr).orElse(DEFAULT_ACTION_USER);


    LocalDateTime now = LocalDateTime.now();
    ReflectUtils.setFieldValueIfExists(entity, Fields.updateTime, now);
    ReflectUtils.setFieldValueIfExists(entity, Fields.updateBy, actionUser);

  }

  @Override
  public String name() {
    return "EasyQueryEntityInterceptor";
  }

  @Override
  public boolean apply(Class<?> entityClass) {
    return true;
  }


  interface Fields {

    String revision = "revision";
    String createTime = "createTime";
    String createBy = "createBy";
    String deleteFlag = "deleteFlag";

    String updateTime = "updateTime";
    String updateBy = "updateBy";

  }
}
