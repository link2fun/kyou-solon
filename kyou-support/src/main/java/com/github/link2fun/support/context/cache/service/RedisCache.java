package com.github.link2fun.support.context.cache.service;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 缓存接口
 *
 * @author ruoyi
 **/
public interface RedisCache {

  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key   缓存的键值
   * @param value 缓存的值
   */
  <T> void set(final String key, final T value);

  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key      缓存的键值
   * @param value    缓存的值
   * @param timeout  时间
   * @param timeUnit 时间颗粒度
   */
  <T> void set(final String key, final T value, final Integer timeout, final TemporalUnit timeUnit);


  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key      缓存的键值
   * @param value    缓存的值
   * @param duration 有效时间
   * @param <T>      缓存对象
   */
  <T> void set(final String key, final T value, final Duration duration);

  /**
   * 设置有效时间
   *
   * @param key     Redis键
   * @param timeout 超时时间(单位:秒)
   * @return true=设置成功；false=设置失败
   */
  boolean expire(final String key, final long timeout);

  /**
   * 设置有效时间
   *
   * @param key     Redis键
   * @param timeout 超时时间
   * @param unit    时间单位
   * @return true=设置成功；false=设置失败
   */
  boolean expire(final String key, final long timeout, final TemporalUnit unit);

  /**
   * 获取有效时间
   *
   * @param key Redis键
   * @return 有效时间
   */
  long getExpire(final String key);

  /**
   * 判断 key是否存在
   *
   * @param key 键
   * @return true 存在 false不存在
   */
  Boolean hasKey(String key);

  /**
   * 获得缓存的基本对象。
   *
   * @param key 缓存键值
   * @return 缓存键值对应的数据
   */
  <T> T get(final String key);


  /**
   * 获取缓存的基本对象, 如果没有缓存值的话，返回默认值
   * @param key 缓存键值
   * @param defaultValue 默认值
   *                     @return 缓存键值对应的数据
   */
  default <T> T getOrDefault(final String key, final T defaultValue) {
    T value = get(key);
    return value == null ? defaultValue : value;
  }

  /**
   * 删除单个对象
   *
   * @param key 缓存键值
   */
  boolean del(final String key);

  /**
   * 删除集合对象
   *
   * @param keys 多个对象
   * @return 是否删除完成
   */
  boolean del(final Iterable<String> keys);

  /**
   * 缓存List数据
   *
   * @param key      缓存的键值
   * @param dataList 待缓存的List数据
   */
  <T> void setList(final String key, final List<T> dataList);

  /**
   * 获得缓存的list对象
   *
   * @param key 缓存的键值
   * @return 缓存键值对应的数据
   */
  <T> List<T> getList(final String key);

  /**
   * 缓存Set
   *
   * @param key     缓存键值
   * @param dataSet 缓存的数据
   * @return 缓存数据的对象
   */
  <T> boolean setSet(final String key, final Set<T> dataSet);

  /**
   * 获得缓存的set
   *
   * @param key 缓存键值
   * @return 缓存键值对应的数据
   */
  <T> Set<T> getSet(final String key);

  /**
   * 缓存Map
   *
   * @param key     缓存键值
   * @param dataMap 缓存的数据
   */
  <T> void setMap(final String key, final Map<String, T> dataMap);

  /**
   * 获得缓存的Map
   *
   * @param key 缓存的键值
   * @return 缓存键值对应的数据
   */
  <T> Map<String, T> getMap(final String key);


  /**
   * 获得缓存的基本对象列表
   *
   * @param pattern 字符串前缀
   * @return 对象列表
   */
  Iterable<String> keys(final String pattern);
}
