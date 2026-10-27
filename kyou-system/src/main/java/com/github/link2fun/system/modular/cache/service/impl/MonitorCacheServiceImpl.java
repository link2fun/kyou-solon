package com.github.link2fun.system.modular.cache.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.system.domain.SysCache;
import com.github.link2fun.system.modular.cache.service.IMonitorCacheService;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RedissonClient;
import org.redisson.api.redisnode.RedisNode;
import org.redisson.api.redisnode.RedisNodes;
import org.redisson.api.redisnode.RedisSingle;

import java.math.BigDecimal;
import java.util.*;

/**
 * 缓存监控实现; Redis INFO 文本的解析与命名空间目录都收在这里, 不暴露给调用方
 */
@Component
public class MonitorCacheServiceImpl implements IMonitorCacheService {

  /** Redis 单节点句柄 */
  @Inject
  private RedissonClient redissonClient;

  /** 通用缓存操作 */
  @Inject
  private RedisCache redisCache;

  /**
   * 读取 Redis 运行指标
   *
   * @return info 原始指标、dbSize 键总数、commandStats 命令调用分布
   */
  @Override
  public Map<String, Object> getCacheInfo() {
    final RedisSingle node = redissonClient.getRedisNodes(RedisNodes.SINGLE);
    final Map<String, String> info = node.getInstance().info(RedisNode.InfoSection.ALL);
    final Map<String, String> commandStats = node.getInstance().info(RedisNode.InfoSection.COMMANDSTATS);
    final Map<String, String> keySpace = node.getInstance().info(RedisNode.InfoSection.KEYSPACE);

    final int dbSize = keySpace.values().stream()
      .map(value -> new BigDecimal(StrUtil.subBetween(value, "keys=", ",expires")))
      .reduce(BigDecimal::add)
      .orElse(BigDecimal.ZERO)
      .intValue();

    final List<Map<String, String>> pieList = new ArrayList<>();
    commandStats.forEach((key, property) -> {
      final Map<String, String> data = new HashMap<>(2);
      data.put("name", StrUtil.removePrefix(key, "cmdstat_"));
      data.put("value", StrUtil.subBetween(property, "calls=", ",usec"));
      pieList.add(data);
    });

    final Map<String, Object> result = new LinkedHashMap<>(3);
    result.put("info", info);
    result.put("dbSize", dbSize);
    result.put("commandStats", pieList);
    return result;
  }

  /**
   * 列出已知的缓存命名空间(前缀与中文名)
   *
   * @return 命名空间目录
   */
  @Override
  public List<SysCache> listCacheNamespaces() {
    // sa-token 的键前缀随 token-name 配置变化, 因此运行期读取而非编译期常量
    final String tokenName = Solon.cfg().getProperty("sa-token.token-name");
    final List<SysCache> caches = new ArrayList<>();
    caches.add(new SysCache(tokenName + ":login:session:", "用户信息"));
    caches.add(new SysCache(tokenName + ":login:token:", "会话信息"));
    caches.add(new SysCache(tokenName + ":custom:session:", "权限缓存"));
    caches.add(new SysCache(CacheConstants.SYS_CONFIG_KEY, "配置信息"));
    caches.add(new SysCache(CacheConstants.SYS_DICT_KEY, "数据字典"));
    caches.add(new SysCache(CacheConstants.CAPTCHA_CODE_KEY, "验证码"));
    caches.add(new SysCache(CacheConstants.REPEAT_SUBMIT_KEY, "防重提交"));
    caches.add(new SysCache(CacheConstants.RATE_LIMIT_KEY, "限流处理"));
    caches.add(new SysCache(CacheConstants.PWD_ERR_CNT_KEY, "密码错误次数"));
    return caches;
  }

  /**
   * 按命名空间前缀扫描键名
   *
   * @param cacheName 命名空间前缀
   * @return 匹配的键名
   */
  @Override
  public Iterable<String> listCacheKeys(final String cacheName) {
    return redisCache.keys(cacheName + "*");
  }

  /**
   * 读取单个缓存键的值
   *
   * @param cacheName 命名空间前缀, 用于回显
   * @param cacheKey  键名
   * @return 键名与值的快照
   */
  @Override
  public SysCache getCacheValue(final String cacheName, final String cacheKey) {
    final Object cacheValue = redisCache.get(cacheKey);
    return new SysCache(cacheName, cacheKey, JSONUtil.toJsonStr(cacheValue));
  }

  /**
   * 按命名空间前缀批量删除
   *
   * @param cacheName 命名空间前缀
   */
  @Override
  public void clearCacheByName(final String cacheName) {
    redisCache.del(redisCache.keys(cacheName + "*"));
  }

  /**
   * 删除单个缓存键
   *
   * @param cacheKey 键名
   */
  @Override
  public void clearCacheByKey(final String cacheKey) {
    redisCache.del(cacheKey);
  }

  /**
   * 清空当前库的全部缓存
   */
  @Override
  public void clearAllCache() {
    redisCache.del(redisCache.keys("*"));
  }
}
