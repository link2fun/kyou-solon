package com.github.link2fun.framework.plugin;

import com.github.link2fun.framework.interceptor.SameUrlDataInterceptor;
import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.context.cache.service.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

/** @link <a href="https://solon.noear.org/article/35">八、切面与环绕拦截（AOP）</a> */
@Slf4j
public class RepeatSubmitRegisterPluginImpl implements Plugin {
  /**
   * 启动（保留，为兼容性过度）
   *
   * @param context 应用上下文
   */
  @Override
  public void start(final AppContext context) throws Throwable {

    context.getBeanAsync(RedisCache.class, (redisCache) -> {
      context.beanInterceptorAdd(RepeatSubmit.class, new SameUrlDataInterceptor(redisCache));
      log.info("@RepeatSubmit 注解拦截器加载完成");
    });

  }
}
