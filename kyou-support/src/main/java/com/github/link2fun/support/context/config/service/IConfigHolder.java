package com.github.link2fun.support.context.config.service;

public interface IConfigHolder {


  /**
   * 初始化所有参数配置
   */
  void reloadAll();


  /**
   * 获取 指定编码的 参数配置
   *
   * @param configKey 参数编码
   * @return 参数配置
   */
  Object get(String configKey);

  /**
   * 替换更新 参数配置
   *
   * @param configKey   参数编码
   * @param configValue 参数值
   */
  void put(String configKey, Object configValue);


  /**
   * 删除 参数配置
   *
   * @param configKey 参数编码
   */
  void remove(String configKey);

}
