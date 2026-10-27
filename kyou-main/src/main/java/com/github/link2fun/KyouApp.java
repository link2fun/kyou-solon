package com.github.link2fun;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.github.link2fun.context.buildinfo.BuildInfoContext;
import com.github.link2fun.framework.aop.LogInterceptor;
import com.github.link2fun.framework.auth.AuditSink;
import com.github.link2fun.support.config.KyouProperties;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.Lifecycle;
import org.noear.solon.core.convert.Converter;
import org.noear.solon.core.event.AppLoadEndEvent;
import org.noear.solon.scheduling.annotation.EnableScheduling;
import org.noear.solon.serialization.jackson.JacksonStringSerializer;
import org.noear.solon.web.staticfiles.StaticMappings;
import org.noear.solon.web.staticfiles.repository.FileStaticRepository;

import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@EnableScheduling
@SolonMain
public class KyouApp {
  public static void main(String[] args) {

    long start = System.currentTimeMillis();

    Solon.start(KyouApp.class, args, app -> {
      final LogInterceptor logInterceptor = new LogInterceptor();
      app.context().beanInterceptorAdd(Mapping.class, logInterceptor);
      final AtomicReference<AuditSink> auditSinkRef = new AtomicReference<>();
      app.context().getBeanAsync(AuditSink.class, sink -> {
        auditSinkRef.set(sink);
        logInterceptor.setAuditSink(sink);
      });

      app.context().lifecycle(0, new Lifecycle() {
        @Override
        public void start() {
        }

        @Override
        public void preStop() {
          final AuditSink auditSink = auditSinkRef.get();
          if (auditSink != null) {
            auditSink.shutdown();
          }
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
        // 日志与 API 响应共用同一份映射器：日期格式、Long 转换等对外呈现完全一致
        logInterceptor.setObjectMapper(jacksonSerializer.getSerializeConfig().getMapper());
      });

      // 请求体只接受声明过的字段: 多传的字段一律 400, 避免客户端用 password/delFlag 这类
      // 未声明字段改写服务端本该自己决定的列。
      //
      // 必须挂在 AppLoadEnd 之后: JacksonEntityConverter 的构造器会 newMapper(...) 整个替换掉
      // 反序列化映射器, 早于它配置会被覆盖成 Solon 默认的 failOnUnknown=false
      app.context().onEvent(AppLoadEndEvent.class, e ->
        Solon.context().getBeanAsync(JacksonStringSerializer.class, serializer ->
          serializer.getDeserializeConfig().getMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)));

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
