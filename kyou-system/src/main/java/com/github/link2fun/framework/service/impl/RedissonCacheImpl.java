package com.github.link2fun.framework.service.impl;

import com.github.link2fun.support.context.cache.service.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.api.options.KeysScanOptions;

import java.time.Duration;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RedissonCacheImpl implements RedisCache {


  private final RedissonClient redissonClient;

  public RedissonCacheImpl(RedissonClient redissonClient) {
    this.redissonClient = redissonClient;
  }


  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key   缓存的键值
   * @param value 缓存的值
   */
  @Override
  public <T> void set(final String key, final T value) {
    redissonClient.getBucket(key).set(value);
  }

  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key      缓存的键值
   * @param value    缓存的值
   * @param timeout  时间
   * @param timeUnit 时间颗粒度
   */
  @Override
  public <T> void set(final String key, final T value, final Integer timeout, final TemporalUnit timeUnit) {
    final Duration duration = Duration.of(timeout, timeUnit);
    redissonClient.getBucket(key).set(value, duration);
  }

  /**
   * 缓存基本的对象，Integer、String、实体类等
   *
   * @param key      缓存的键值
   * @param value    缓存的值
   * @param duration 有效时间
   */
  @Override
  public <T> void set(final String key, final T value, final Duration duration) {
    redissonClient.getBucket(key).set(value, duration);
  }

  /**
   * 设置有效时间
   *
   * @param key     Redis键
   * @param timeout 超时时间(单位:秒)
   * @return true=设置成功；false=设置失败
   */
  @Override
  public boolean expire(final String key, final long timeout) {

    final RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.expire(Duration.ofSeconds(timeout));
  }

  /**
   * 设置有效时间
   *
   * @param key     Redis键
   * @param timeout 超时时间
   * @param unit    时间单位
   * @return true=设置成功；false=设置失败
   */
  @Override
  public boolean expire(final String key, final long timeout, final TemporalUnit unit) {
    final RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.expire(Duration.of(timeout, unit));
  }

  /**
   * 获取有效时间
   *
   * @param key Redis键
   * @return 有效时间
   */
  @Override
  public long getExpire(final String key) {
    final RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.remainTimeToLive();
  }

  /**
   * 判断 key是否存在
   *
   * @param key 键
   * @return true 存在 false不存在
   */
  @Override
  public Boolean hasKey(final String key) {

    final RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.isExists();
  }

  /**
   * 获得缓存的基本对象。
   *
   * @param key 缓存键值
   * @return 缓存键值对应的数据
   */
  @Override
  public <T> T get(final String key) {

    final RBucket<T> bucket = redissonClient.getBucket(key);
    return bucket.get();
  }

  /**
   * 删除单个对象
   *
   * @param key 键
   */
  @Override
  public boolean del(final String key) {
    final RBucket<Object> bucket = redissonClient.getBucket(key);
    return bucket.delete();
  }

  /**
   * 删除集合对象
   *
   * @param keys 多个对象
   * @return 是否删除成功
   */
  @Override
  public boolean del(final Iterable<String> keys) {

    for (final String key : keys) {
      final RBucket<Object> bucket = redissonClient.getBucket(key);
      bucket.delete();
    }

    return true;
  }

  /**
   * 缓存List数据
   *
   * @param key      缓存的键值
   * @param dataList 待缓存的List数据
   */
  @Override
  public <T> void setList(final String key, final List<T> dataList) {
    final RList<T> list = redissonClient.getList(key);
    list.clear();
    list.addAll(dataList);
  }

  /**
   * 获得缓存的list对象
   *
   * @param key 缓存的键值
   * @return 缓存键值对应的数据
   */
  @Override
  public <T> List<T> getList(final String key) {
    final RList<T> list = redissonClient.getList(key);
    return list.readAll();
  }

  /**
   * 缓存Set
   *
   * @param key     缓存键值
   * @param dataSet 缓存的数据
   * @return 缓存数据的对象
   */
  @Override
  public <T> boolean setSet(final String key, final Set<T> dataSet) {
    final RSet<T> set = redissonClient.getSet(key);
    set.clear();
    return set.addAll(dataSet);
  }

  /**
   * 获得缓存的set
   *
   * @param key 缓存的键值
   * @return 缓存键值对应的数据
   */
  @Override
  public <T> Set<T> getSet(final String key) {
    final RSet<T> set = redissonClient.getSet(key);
    return set.readAll();
  }

  /**
   * 缓存Map
   *
   * @param key     缓存的键值
   * @param dataMap 缓存的数据
   */
  @Override
  public <T> void setMap(final String key, final Map<String, T> dataMap) {
    final RMap<String, T> map = redissonClient.getMap(key);
    map.clear();
    map.putAll(dataMap);
  }

  /**
   * 获得缓存的Map
   *
   * @param key 缓存的键值
   * @return 缓存键值对应的数据
   */
  @Override
  public <T> Map<String, T> getMap(final String key) {
    final RMap<String, T> map = redissonClient.getMap(key);
    return map.readAllMap();
  }


  /**
   * 获得缓存的基本对象列表
   *
   * @param pattern 字符串前缀
   * @return 对象列表
   */
  @Override
  public Iterable<String> keys(final String pattern) {
    return redissonClient.getKeys().getKeys(KeysScanOptions.defaults().pattern(pattern));
  }
}
