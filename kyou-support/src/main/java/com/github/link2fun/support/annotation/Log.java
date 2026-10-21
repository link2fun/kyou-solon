package com.github.link2fun.support.annotation;

import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 *
 * @author ruoyi
 */
@SuppressWarnings({"unused"})
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {
  /**
   * 模块
   */
  String title() default "";

  /**
   * 功能
   */
  BusinessType businessType() default BusinessType.OTHER;

  /**
   * 操作人类别
   */
  OperatorType operatorType() default OperatorType.MANAGE;

  /**
   * 是否保存请求的参数
   */
  boolean isSaveRequestData() default true;

  /**
   * 是否保存响应的参数
   */
  boolean isSaveResponseData() default true;

  /**
   * 排除指定的请求参数
   */
  String[] excludeParamNames() default {};

  /**
   * 请求与响应日志是否脱敏（敏感字段值打码为 {@code ***}）。
   *
   * <p>默认 {@code false}，按接口显式开启：入参或响应中会出现口令、证件号、
   * 手机号、邮箱等字段的接口（用户资料类、改密类）应设为 {@code true}。
   * 开启后按运行时实际序列化内容判定，不受参数声明类型的子类替换影响。
   */
  boolean mask() default false;
}
