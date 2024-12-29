package com.github.link2fun.framework.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.context.config.service.IConfigHolder;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.config.service.ISystemConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

import static com.github.link2fun.support.context.config.ConfigContext.buildCacheKey;


@Slf4j
public class StandaloneRedisConfigHolder implements IConfigHolder {

  private RedisCache redisCache;
  private ISystemConfigService configService;

  public StandaloneRedisConfigHolder(ISystemConfigService configService, RedisCache redisCache) {
    this.configService = configService;
    this.redisCache = redisCache;
  }


  @Override
  public void reloadAll() {
    log.info("[系统配置-Redis] 初始化开始");

    List<SysConfig> configList = configService.listAll();

    // 把现在的缓存清理了
    Iterable<String> cachedKeys = redisCache.keys(buildCacheKey("*"));
    redisCache.del(cachedKeys);

    configList.forEach(config -> {
      String cacheKey = buildCacheKey(config.getConfigKey());
      redisCache.set(cacheKey, config.getConfigValue());
    });

    log.info("[系统配置-Redis] 初始化完成, 共 {} 条配置", CollectionUtil.size(configList));

  }

  @Override
  public Object get(String configKey) {
    String cacheKey = buildCacheKey(configKey);
    Object cacheValue = redisCache.get(cacheKey);

    if (Objects.nonNull(cacheValue)) {
      // 存在缓存值, 直接返回
      return cacheValue;
    }
    // 获取的值为null, 说明 key 可能不存在, 先判断一下
    if (redisCache.hasKey(cacheKey)) {
      // 有这个 key 说明 值就是 null, 进行返回
      return null;
    }
    // 没有这个key , 说明目前没有缓存这个 config, 去数据库查询一下
    String configValue = configService.selectConfigByKey(configKey);
    redisCache.set(configKey,configValue);

    return configValue;
  }

  @Override
  public void put(String configKey, Object configValue) {
    redisCache.set(buildCacheKey(configKey), configValue);
  }

  @Override
  public void remove(String configKey) {
    redisCache.del(buildCacheKey(configKey));

  }
}
