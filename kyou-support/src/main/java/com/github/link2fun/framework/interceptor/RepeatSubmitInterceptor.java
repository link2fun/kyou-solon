package com.github.link2fun.framework.interceptor;

import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.core.domain.R;
import com.github.link2fun.support.context.cache.service.RedisCache;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;

import java.util.Objects;

/**
 * 重复提交拦截器
 */
public abstract class RepeatSubmitInterceptor implements Interceptor {

  protected RedisCache redisCache;

  public RepeatSubmitInterceptor(RedisCache redisCache) {
    this.redisCache = redisCache;
  }

  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(final Invocation inv) throws Throwable {

    final RepeatSubmit repeatSubmit = inv.method().getAnnotation(RepeatSubmit.class);
    if (Objects.isNull(repeatSubmit)) {
      // 如果没有注解，则直接放行
      return inv.invoke();
    }

    // 有注解, 则进行处理
    final Context context = Context.current();
    if (isRepeatSubmit(context, repeatSubmit)) {
      // 重复提交
      return R.fail(repeatSubmit.message());
    }

    return inv.invoke();
  }

  /**
   * 判断是否重复提交
   *
   * @param context      上下文
   * @param repeatSubmit 重复提交注解
   * @return 如果重复提交返回true，否则返回false
   */
  protected abstract boolean isRepeatSubmit(final Context context, final RepeatSubmit repeatSubmit);
}
