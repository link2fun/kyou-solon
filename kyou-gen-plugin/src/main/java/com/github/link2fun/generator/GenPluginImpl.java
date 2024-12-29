package com.github.link2fun.generator;

import com.github.link2fun.generator.config.GenConfig;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;


public class GenPluginImpl implements Plugin {
  /**
   * 启动（保留，为兼容性过度）
   *
   * @param context 应用上下文
   */
  @Override
  public void start(final AppContext context) throws Throwable {
    context.cfg().loadAdd("generator.yml");
    final GenConfig genConfig = context.cfg().toBean("gen", GenConfig.class);
    context.wrapAndPut(GenConfig.class, genConfig);
  }
}
