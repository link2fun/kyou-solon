package com.github.link2fun.system.modular.cache.service;

import com.github.link2fun.system.domain.SysCache;

import java.util.List;
import java.util.Map;

/**
 * 缓存监控: 运行指标、命名空间目录与键值浏览/清理
 */
public interface IMonitorCacheService {

  /**
   * 读取 Redis 运行指标
   *
   * @return info 原始指标、dbSize 键总数、commandStats 命令调用分布
   */
  Map<String, Object> getCacheInfo();

  /**
   * 列出已知的缓存命名空间(前缀与中文名)
   *
   * @return 命名空间目录
   */
  List<SysCache> listCacheNamespaces();

  /**
   * 按命名空间前缀扫描键名
   *
   * @param cacheName 命名空间前缀
   * @return 匹配的键名
   */
  Iterable<String> listCacheKeys(String cacheName);

  /**
   * 读取单个缓存键的值
   *
   * @param cacheName 命名空间前缀, 用于回显
   * @param cacheKey  键名
   * @return 键名与值的快照
   */
  SysCache getCacheValue(String cacheName, String cacheKey);

  /**
   * 按命名空间前缀批量删除
   *
   * @param cacheName 命名空间前缀
   */
  void clearCacheByName(String cacheName);

  /**
   * 删除单个缓存键
   *
   * @param cacheKey 键名
   */
  void clearCacheByKey(String cacheKey);

  /**
   * 清空当前库的全部缓存
   */
  void clearAllCache();
}
