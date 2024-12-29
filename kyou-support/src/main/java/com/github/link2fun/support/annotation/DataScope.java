package com.github.link2fun.support.annotation;

import com.github.link2fun.framework.interceptor.DataScopeInterceptor;
import org.noear.solon.annotation.Around;

import java.lang.annotation.*;

/**
 * 数据权限过滤注解
 *
 * @author ruoyi
 * @link <a href="https://solon.noear.org/article/617">@Around 使用说明（AOP）</a>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Around(DataScopeInterceptor.class)
public @interface DataScope {
  /**
   * 部门表的别名
   */
  String deptAlias() default "";

  /**
   * 用户表的别名
   */
  String userAlias() default "";

  /**
   * 权限字符（用于多个角色匹配符合要求的权限）默认根据权限注解@ss获取，多个权限用逗号分隔开来
   */
  String permission() default "";
}
