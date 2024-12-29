package com.github.link2fun.framework.context.config;

import com.github.link2fun.framework.service.impl.StandaloneRedisConfigHolder;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.context.config.service.IConfigHolder;
import com.github.link2fun.system.modular.config.service.ISystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Condition;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;

@Slf4j
@Configuration
public class EnableConfigContextAutoConfiguration {

  @Bean
  @Condition(onMissingBean = IConfigHolder.class)
  public IConfigHolder configHolder(@Inject RedisCache redisCache,
                                    @Inject ISystemConfigService configService) {
    log.info("[系统配置] 当前使用 单机数据库 + Redis 进行系统配置管理");
    StandaloneRedisConfigHolder configHolder = new StandaloneRedisConfigHolder(configService, redisCache);
    configHolder.reloadAll();
    log.info("[系统配置] 系统配置加载完成");
    return configHolder;
  }
}
