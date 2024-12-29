package com.github.link2fun.support.context.config;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.context.config.service.IConfigHolder;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;

@Slf4j
public class ConfigContext {

  private static IConfigHolder configHolder;

  static {
    Solon.context().getBeanAsync(IConfigHolder.class, holder -> configHolder = holder);
  }


  public static void reloadAll() {
    configHolder.reloadAll();
  }


  /**
   * 获取 指定编码的 参数配置
   *
   * @param configKey 参数编码
   * @return 参数配置
   */
  public static Object get(String configKey) {
    return configHolder.get(configKey);
  }

  public static String getString(String configKey) {
    return Convert.toStr(get(configKey));
  }

  public static String getString(String configKey, String defaultValue) {
    return Convert.toStr(configKey, defaultValue);
  }

  @SuppressWarnings("unused")
  public static Object getWithDefault(String configKey, Object defaultValue) {
    Object value = get(configKey);
    return ObjectUtil.defaultIfNull(value, defaultValue);
  }

  // ===== 以下是更新配置的方法 =====

  /** 添加系统常量 */
  public static void put(String configKey, Object configValue) {
    if (ObjectUtil.hasEmpty(configKey, configValue)) {
      return;
    }
    configHolder.put(configKey, configValue);
  }

  /** 删除常量，系统常量无法删除，在sysConfig已判断 */
  public static void remove(String configKey) {
    if (ObjectUtil.hasEmpty(configKey)) {
      return;
    }
    configHolder.remove(configKey);
  }

  /**
   * 重命名参数
   *
   * @param oldConfigKey 旧参数编码
   * @param newConfigKey 新参数编码
   */

  public static void rename(String oldConfigKey, String newConfigKey) {
    if (ObjectUtil.hasEmpty(oldConfigKey, newConfigKey)) {
      log.error("重命名参数失败，参数编码不能为空");
      return;
    }
    ConfigContext.put(newConfigKey, ConfigContext.get(oldConfigKey));
    ConfigContext.remove(oldConfigKey);
  }


  public static boolean getBoolean(String configKey, boolean defaultVal) {
    return Convert.toBool(get(configKey), defaultVal);
  }


  /**
   * 构建缓存 key, 用于缓存
   *
   * @param configKey 配置key
   * @return 缓存key
   */
  public static String buildCacheKey(String configKey) {
    return CacheConstants.SYS_CONFIG_KEY + configKey;
  }
}
