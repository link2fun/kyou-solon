package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.system.domain.SysCache;
import com.github.link2fun.system.modular.cache.service.IMonitorCacheService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;

import java.util.List;
import java.util.Map;

/**
 * 缓存监控
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/cache")
public class MonitorCacheController {

  @Inject
  private IMonitorCacheService cacheService;

  /** 缓存运行指标 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(method = MethodType.GET)
  public Resp<Map<String, Object>> getInfo() {
    return Resp.successData(cacheService.getCacheInfo());
  }

  /** 缓存命名空间目录 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getNames", method = MethodType.GET)
  public Resp<List<SysCache>> cache() {
    return Resp.successData(cacheService.listCacheNamespaces());
  }

  /** 按命名空间扫描键名 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getKeys/{cacheName}", method = MethodType.GET)
  public Resp<Iterable<String>> getCacheKeys(@Path String cacheName) {
    return Resp.successData(cacheService.listCacheKeys(cacheName));
  }

  /** 读取单个缓存键的值 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getValue/{cacheName}/{cacheKey}", method = MethodType.GET)
  public Resp<SysCache> getCacheValue(@Path String cacheName, @Path String cacheKey) {
    return Resp.successData(cacheService.getCacheValue(cacheName, cacheKey));
  }

  /** 按命名空间清理 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheName/{cacheName}", method = MethodType.DELETE)
  public Resp<Void> clearCacheName(@Path String cacheName) {
    cacheService.clearCacheByName(cacheName);
    return Resp.success();
  }

  /** 清理单个缓存键 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheKey/{cacheKey}", method = MethodType.DELETE)
  public Resp<Void> clearCacheKey(@Path String cacheKey) {
    cacheService.clearCacheByKey(cacheKey);
    return Resp.success();
  }

  /** 清空全部缓存 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheAll", method = MethodType.DELETE)
  public Resp<Void> clearCacheAll() {
    cacheService.clearAllCache();
    return Resp.success();
  }
}
