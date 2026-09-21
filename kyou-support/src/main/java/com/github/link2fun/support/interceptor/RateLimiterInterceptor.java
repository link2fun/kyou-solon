package com.github.link2fun.support.interceptor;

import com.github.link2fun.support.annotation.RateLimiter;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.ip.IpUtils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Method;
import java.time.Duration;

/**
 * 限流拦截器
 *
 * @link <a href="https://solon.noear.org/article/35">AOP</a>
 */
@Slf4j
public class RateLimiterInterceptor implements Interceptor {
  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(final Invocation inv) throws Throwable {

    final RateLimiter rateLimiter = inv.method().getAnnotation(RateLimiter.class);

    if (rateLimiter == null) {
      // 如果没有注解，则直接执行
      return inv.invoke();
    }

    final RedissonClient redissonClient = Solon.context().getBean(RedissonClient.class);

    final String combineKey = getCombineKey(rateLimiter, inv.method().getMethod());
    final RRateLimiter limiter = redissonClient.getRateLimiter(combineKey);

    // 设置限流器的参数, 每 rateLimiter.time() 秒钟产生 rateLimiter.count() 个令牌
    final RateLimiterConfig config = limiter.getConfig();
    if (config.getRate() != rateLimiter.count() || config.getRateInterval() != rateLimiter.time() * 1000L) {
      // 需要删除原来的限流器
      limiter.delete();
      // 再重新设置限流器
      final boolean trySetRate = limiter.trySetRate(RateType.OVERALL, rateLimiter.count(), Duration.ofSeconds(rateLimiter.time()));
      if (!trySetRate) {
        log.warn("设置限流器参数失败: {}", combineKey);
      } else {
        log.debug("设置限流器参数成功: {}", combineKey);
      }
    }

    if (limiter.tryAcquire()) {
      return inv.invoke();
    }
    throw new ServiceException("服务器繁忙，请稍后再试");
  }

  /**
   * 获取组合的key
   *
   * @param rateLimiter 限流器注解
   * @param method      方法
   * @return 组合的key
   */
  public String getCombineKey(RateLimiter rateLimiter, Method method) {
    StringBuilder keyBuilder = new StringBuilder(rateLimiter.key());
    switch (rateLimiter.limitType()) {
      case IP:
        keyBuilder.append("IP:").append(IpUtils.getIpAddr()).append(":");
        break;
      case USER:
        final Long userId = ActionContext.current().getUserId();
        keyBuilder.append("USER:").append(userId == null ? "anonymous" : userId).append(":");
        break;
      case GLOBAL:
        keyBuilder.append("GLOBAL:");
      default:

        break;
    }
    Class<?> targetClass = method.getDeclaringClass();
    keyBuilder.append(targetClass.getSimpleName()).append(".").append(method.getName());
    return keyBuilder.toString();
  }
}
