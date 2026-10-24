package com.github.link2fun.framework.config.easyquery;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ReflectUtil;
import com.easy.query.core.basic.extension.interceptor.EntityInterceptor;
import com.easy.query.core.expression.sql.builder.EntityInsertExpressionBuilder;
import com.easy.query.core.expression.sql.builder.EntityUpdateExpressionBuilder;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.model.SessionUser;
import org.noear.solon.annotation.Component;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class EasyQueryEntityInterceptor implements EntityInterceptor {
  private final static String DEFAULT_ACTION_USER = "system";
  @Override
  public void configureInsert(Class<?> entityClass, EntityInsertExpressionBuilder entityInsertExpressionBuilder, Object entity) {

    String actionUser = Optional.ofNullable(ActionContext.current()).map(ActionContext::getSessionUser).map(SessionUser::getUsername).map(Convert::toStr).orElse(DEFAULT_ACTION_USER);


    LocalDateTime now = LocalDateTime.now();
    setFieldValueIfExists(entity, Fields.createTime, now);
    setFieldValueIfExists(entity, Fields.createBy, actionUser);
    setFieldValueIfExists(entity, Fields.updateTime, now);
    setFieldValueIfExists(entity, Fields.updateBy,actionUser);
    setFieldValueIfExists(entity, Fields.deleteFlag, UserConstants.NORMAL);
    setFieldValueIfExists(entity, Fields.revision, 0);

  }

  @Override
  public void configureUpdate(Class<?> entityClass, EntityUpdateExpressionBuilder entityUpdateExpressionBuilder, Object entity) {
    String actionUser = Optional.ofNullable(ActionContext.current()).map(ActionContext::getSessionUser).map(SessionUser::getUsername).map(Convert::toStr).orElse(DEFAULT_ACTION_USER);


    LocalDateTime now = LocalDateTime.now();
    setFieldValueIfExists(entity, Fields.updateTime, now);
    setFieldValueIfExists(entity, Fields.updateBy, actionUser);

  }

  /** 字段存在则直接赋值, 不存在静默跳过(非全实体都有审计列) */
  private static void setFieldValueIfExists(Object entity, String fieldName, Object value) {
    Field field = ReflectUtil.getField(entity.getClass(), fieldName);
    if (field != null) {
      ReflectUtil.setFieldValue(entity, field, value);
    }
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
