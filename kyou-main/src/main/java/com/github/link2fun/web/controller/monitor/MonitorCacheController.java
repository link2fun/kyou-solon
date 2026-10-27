package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.system.modular.cache.service.IMonitorCacheService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;

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
  public AjaxResult getInfo() {
    return AjaxResult.successData(cacheService.getCacheInfo());
  }

  /** 缓存命名空间目录 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getNames", method = MethodType.GET)
  public AjaxResult cache() {
    return AjaxResult.successData(cacheService.listCacheNamespaces());
  }

  /** 按命名空间扫描键名 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getKeys/{cacheName}", method = MethodType.GET)
  public AjaxResult getCacheKeys(@Path String cacheName) {
    return AjaxResult.successData(cacheService.listCacheKeys(cacheName));
  }

  /** 读取单个缓存键的值 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getValue/{cacheName}/{cacheKey}", method = MethodType.GET)
  public AjaxResult getCacheValue(@Path String cacheName, @Path String cacheKey) {
    return AjaxResult.successData(cacheService.getCacheValue(cacheName, cacheKey));
  }

  /** 按命名空间清理 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheName/{cacheName}", method = MethodType.DELETE)
  public AjaxResult clearCacheName(@Path String cacheName) {
    cacheService.clearCacheByName(cacheName);
    return AjaxResult.success();
  }

  /** 清理单个缓存键 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheKey/{cacheKey}", method = MethodType.DELETE)
  public AjaxResult clearCacheKey(@Path String cacheKey) {
    cacheService.clearCacheByKey(cacheKey);
    return AjaxResult.success();
  }

  /** 清空全部缓存 */
  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheAll", method = MethodType.DELETE)
  public AjaxResult clearCacheAll() {
    cacheService.clearAllCache();
    return AjaxResult.success();
  }
}
