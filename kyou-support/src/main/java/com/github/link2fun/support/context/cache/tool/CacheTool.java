package com.github.link2fun.support.context.cache.tool;

import com.github.link2fun.support.context.cache.service.RedisCache;
import org.noear.solon.Solon;

import java.time.temporal.TemporalUnit;
import java.util.function.Supplier;

public class CacheTool {

  private static RedisCache redisCache;

  static {
    // 静态注入 RedisCache
    Solon.context().getBeanAsync(RedisCache.class, (bean) -> {
      redisCache = bean;
    });
  }


  public static <R> R cacheGet(final String prefix, final String param, Supplier<R> valueLoader) {
    R result = get(prefix + param);
    if (result == null) {
      result = valueLoader.get();
      set(prefix + param, result);
    }
    return result;
  }


  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key   缓存的键值
   * @param value 缓存的值
   */
  public static <T> void set(String key, T value) {
    redisCache.set(key, value);
  }


  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key      缓存的键值
   * @param value    缓存的值
   * @param timeout  时间
   * @param timeUnit 时间颗粒度
   */
  public static <T> void set(final String key, final T value, final Integer timeout, final TemporalUnit timeUnit) {
    redisCache.set(key, value, timeout, timeUnit);
  }


  /**
   * 获得缓存的基本对象。
   *
   * @param key 缓存键值
   * @return 缓存键值对应的数据
   */
  public static <T> T get(final String key) {
    return redisCache.get(key);
  }


}
