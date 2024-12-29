package com.github.link2fun.support.annotation;

import com.github.link2fun.support.interceptor.RateLimiterInterceptor;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.enums.LimitType;
import org.noear.solon.annotation.Around;

import java.lang.annotation.*;

/**
 * 限流注解
 *
 * @author ruoyi
 */
@Around(value = RateLimiterInterceptor.class)
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
  /**
   * 限流key
   */
  String key() default CacheConstants.RATE_LIMIT_KEY;

  /**
   * 限流时间,单位秒
   */
  int time() default 60;

  /**
   * 限流次数
   */
  int count() default 100;

  /**
   * 限流类型
   */
  LimitType limitType() default LimitType.GLOBAL;
}
