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
  /** 启动: 注册本插件包内的 bean */
  @Override
  public void start(final AppContext context) throws Throwable {
    // 与宿主扫描重复时, 容器按 Class 去重
    context.beanScan(XPluginImp.class);

    // TLogWebInterceptor 不在此处注册, 日志链路信息由 Filter 处理
//    final TLogWebInterceptor tLogWebInterceptor = new TLogWebInterceptor();
//    context.beanInterceptorAdd(Mapping.class, tLogWebInterceptor);
//    context.beanInterceptorAdd();

    log.info("[插件-TLog] 加载完成");
  }
}
