package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.system.domain.SysCache;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisNode;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.api.redisnode.RedisSingle;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
  private RedissonClient redissonClient;

  @Inject
  private RedisCache redisCache;

  private final static List<SysCache> caches = new ArrayList<>();

  static {
    final String tokenName = Solon.cfg().getProperty("sa-token.token-name");
    caches.add(new SysCache(tokenName + ":login:session:", "用户信息"));
    caches.add(new SysCache(tokenName + ":login:token:", "会话信息"));
    caches.add(new SysCache(tokenName + ":custom:session:", "权限缓存"));
    caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
    caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
    caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
    caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
    caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
    caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));


  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(method = MethodType.GET)
  public AjaxResult getInfo() {


    final RedisSingle node = redissonClient.getRedisNodes(RedisNodes.SINGLE);
    final Map<String, String> info = node.getInstance().info(RedisNode.InfoSection.ALL);
    final Map<String, String> commandStats = node.getInstance().info(RedisNode.InfoSection.COMMANDSTATS);
    final Map<String, String> keySpace = node.getInstance().info(RedisNode.InfoSection.KEYSPACE);


    final int dbSize = keySpace.values().stream()
      .map(value -> {
        final String keyCountStr = StrUtil.subBetween(value, "keys=", ",expires");
        return new BigDecimal(keyCountStr);
      }).reduce(BigDecimal::add)
      .orElse(BigDecimal.ZERO)
      .intValue();

    Map<String, Object> result = new HashMap<>(3);
    result.put("info", info);
    result.put("dbSize", dbSize);

    List<Map<String, String>> pieList = new ArrayList<>();
    commandStats.keySet().forEach(key -> {
      Map<String, String> data = new HashMap<>(2);
      String property = commandStats.get(key);
      data.put("name", StrUtil.removePrefix(key, "cmdstat_"));
      data.put("value", StrUtil.subBetween(property, "calls=", ",usec"));
      pieList.add(data);
    });
    result.put("commandStats", pieList);
    return AjaxResult.success(result);
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getNames", method = MethodType.GET)
  public AjaxResult cache() {
    return AjaxResult.success(caches);
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getKeys/{cacheName}", method = MethodType.GET)
  public AjaxResult getCacheKeys(@Path String cacheName) {
    Iterable<String> cacheKeys = redisCache.keys(cacheName + "*");
    return AjaxResult.success(cacheKeys);
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/getValue/{cacheName}/{cacheKey}", method = MethodType.GET)
  public AjaxResult getCacheValue(@Path String cacheName, @Path String cacheKey) {

    Object cacheValue = redisCache.get(cacheKey);
    SysCache sysCache = new SysCache(cacheName, cacheKey, JSONUtil.toJsonStr(cacheValue));
    return AjaxResult.success(sysCache);
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheName/{cacheName}", method = MethodType.DELETE)
  public AjaxResult clearCacheName(@Path String cacheName) {
    Iterable<String> cacheKeys = redisCache.keys(cacheName + "*");
    redisCache.del(cacheKeys);
    return AjaxResult.success();
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheKey/{cacheKey}", method = MethodType.DELETE)
  public AjaxResult clearCacheKey(@Path String cacheKey) {
    redisCache.del(cacheKey);
    return AjaxResult.success();
  }

  @SaCheckPermission("monitor:cache:list")
  @Mapping(value = "/clearCacheAll", method = MethodType.DELETE)
  public AjaxResult clearCacheAll() {
    Iterable<String> cacheKeys = redisCache.keys("*");
    redisCache.del(cacheKeys);
    return AjaxResult.success();
  }
}
