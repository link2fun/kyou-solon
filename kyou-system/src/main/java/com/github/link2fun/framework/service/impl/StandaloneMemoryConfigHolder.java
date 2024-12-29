package com.github.link2fun.framework.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Dict;
import com.github.link2fun.support.context.config.service.IConfigHolder;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.config.service.ISystemConfigService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StandaloneMemoryConfigHolder implements IConfigHolder {


  private static final Dict CONFIG_HOLDER = Dict.create();


  private ISystemConfigService configService;

  public StandaloneMemoryConfigHolder(final ISystemConfigService configService) {
    this.configService = configService;
  }

  @Override
  public void reloadAll() {

    log.info("[系统配置-内存] 初始化开始");

    List<SysConfig> configList = configService.listAll();
    // 先清除一下现在的缓存
    CONFIG_HOLDER.clear();

    // 将新的值缓存起来
    configList.forEach(config -> CONFIG_HOLDER.put(config.getConfigKey(), config.getConfigValue()));

    log.info("[系统配置-内存] 初始化完成, 共 {} 条配置", CollectionUtil.size(configList));

  }

  @Override
  public Object get(String configKey) {
    return CONFIG_HOLDER.get(configKey);
  }

  @Override
  public void put(String configKey, Object configValue) {
    CONFIG_HOLDER.put(configKey, configValue);
  }

  @Override
  public void remove(String configKey) {
    CONFIG_HOLDER.remove(configKey);
  }
}
