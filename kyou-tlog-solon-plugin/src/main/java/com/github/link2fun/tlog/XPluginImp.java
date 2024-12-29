package com.github.link2fun.tlog;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;


/**
 * @author link2fun
 * TLog 插件
 */
@Slf4j
public class XPluginImp implements Plugin {
  /**
   * 启动（保留，为兼容性过度）
   *
   * @param context 应用上下文
   */
  @Override
  public void start(final AppContext context) throws Throwable {

    // 这里无需注入， 也不能注入， 因为一个注解只能添加一个拦截器，项目中已经为 Mapping.class 添加了日志拦截器
    // 上下文的日志链路信息通过 Filter 进行处理
//    final TLogWebInterceptor tLogWebInterceptor = new TLogWebInterceptor();
//    context.beanInterceptorAdd(Mapping.class, tLogWebInterceptor);
//    context.beanInterceptorAdd();

    log.info("[插件-TLog] 加载完成");
  }
}
