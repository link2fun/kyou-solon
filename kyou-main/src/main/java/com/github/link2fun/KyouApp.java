package com.github.link2fun;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.github.link2fun.context.buildinfo.BuildInfoContext;
import com.github.link2fun.framework.aop.LogInterceptor;
import com.github.link2fun.framework.manager.AsyncManager;
import com.github.link2fun.support.config.KyouProperties;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.Lifecycle;
import org.noear.solon.core.convert.Converter;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.noear.solon.serialization.jackson.JacksonStringSerializer;
import org.noear.solon.web.staticfiles.StaticMappings;
import org.noear.solon.web.staticfiles.repository.FileStaticRepository;

@Slf4j
@EnableScheduling
@SolonMain
public class KyouApp {
  public static void main(String[] args) {

    long start = System.currentTimeMillis();

    Solon.start(KyouApp.class, args, app -> {
      // 添加拦截器
      app.context().beanInterceptorAdd(Mapping.class, new LogInterceptor());

      app.context().lifecycle(0, new Lifecycle() {
        @Override
        public void start() {
        }

        @Override
        public void preStop() {
          // 关闭异步任务， 等待任务执行完成
          AsyncManager.me().shutdown();
        }
      });

      final long MAX_SAFE_INTEGER = 9007199254740991L;
      app.context().getBeanAsync(JacksonStringSerializer.class, jacksonSerializer -> {
        final Converter<Long, Object> converter = value -> {
          if (value != null && value < MAX_SAFE_INTEGER) {
            return value;
          }
          return String.valueOf(value);
        };
        jacksonSerializer.addEncoder(Long.class, converter);
        jacksonSerializer.addEncoder(long.class, converter);
      });

      /* 提示：path 可以是目录或单文件；repository 只能是目录（表示这个 path 映射到这个 repository 里） */

      // 1.添加本地绝对目录（例：/img/logo.jpg 映射地址为：/data/sss/app/img/logo.jpg）
      app.context().getBeanAsync(KyouProperties.class,
          cfg -> StaticMappings.add("/profile/", new FileStaticRepository(cfg.getProfile())));

      // 添加一个ThreadLocal工厂，使用TransmittableThreadLocal, 来支持跨线程传递
      // noinspection rawtypes
      app.factories().threadLocalFactory((applyFor, i) -> new TransmittableThreadLocal());
    });

    long times = System.currentTimeMillis() - start;
    log.info("Kyou {} 启动成功，耗时:【{}ms】", BuildInfoContext.buildInfoPrintStr(), times);
  }

}
