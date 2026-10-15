package com.github.link2fun.framework.auth;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.utils.LogUtils;
import com.github.link2fun.support.utils.Threads;
import com.github.link2fun.support.utils.ip.AddressUtils;
import com.github.link2fun.system.modular.logininfo.model.SysLoginInfo;
import com.github.link2fun.system.modular.logininfo.service.ISystemLoginInfoService;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import com.github.link2fun.system.modular.operlog.service.ISystemOperLogService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Slf4j
@Component
public class AuditSinkImpl implements AuditSink {

  @Inject
  private ClientInfoProvider clientInfoProvider;

  @Inject
  private ISystemLoginInfoService loginInfoService;

  @Inject
  private ISystemOperLogService operLogService;

  private Function<String, String> addressResolver = AddressUtils::getRealAddressByIP;

  private ExecutorService executor = buildTtlExecutor();

  public AuditSinkImpl() {
  }

  AuditSinkImpl(ClientInfoProvider clientInfoProvider, ISystemLoginInfoService loginInfoService,
                ISystemOperLogService operLogService, ExecutorService executor,
                Function<String, String> addressResolver) {
    this.clientInfoProvider = clientInfoProvider;
    this.loginInfoService = loginInfoService;
    this.operLogService = operLogService;
    this.executor = executor;
    this.addressResolver = addressResolver;
  }

  /** 记录登录/注销/注册事件 */
  @Override
  public void loginEvent(String username, String event, boolean success, String message) {
    final ClientInfo client = clientInfoProvider.capture();
    executor.execute(() -> {
      final UserAgent userAgent = UserAgentUtil.parse(client.getUserAgent());
      final String address = addressResolver.apply(client.getIp());
      log.info("{}{}{}{}{}", LogUtils.bracket(client.getIp()), address, LogUtils.bracket(username), LogUtils.bracket(event), LogUtils.bracket(message));

      final SysLoginInfo loginInfo = new SysLoginInfo();
      loginInfo.setUserName(username);
      loginInfo.setIpaddr(client.getIp());
      loginInfo.setLoginLocation(address);
      loginInfo.setBrowser(userAgent == null ? "Unknown" : userAgent.getBrowser().getName());
      loginInfo.setOs(userAgent == null ? "Unknown" : userAgent.getOs().getName());
      loginInfo.setMsg(message);
      loginInfo.setLoginTime(LocalDateTime.now());
      loginInfo.setStatus(success ? Constants.SUCCESS : Constants.FAIL);
      loginInfoService.insertLoginInfo(loginInfo);
    });
  }

  /** 记录操作日志 */
  @Override
  public void operEvent(SysOperLog operLog) {
    executor.execute(() -> {
      operLog.setOperLocation(addressResolver.apply(operLog.getOperIp()));
      operLogService.insertOperlog(operLog);
    });
  }

  /** 停机时排空在途审计任务 */
  @Override
  public void shutdown() {
    Threads.shutdownAndAwaitTermination(executor);
  }

  static ExecutorService buildTtlExecutor() {
    final ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS,
      new LinkedBlockingQueue<>(Integer.MAX_VALUE),
      new ThreadFactoryBuilder().setNamePrefix("audit-pool-").build(),
      new ThreadPoolExecutor.CallerRunsPolicy());
    return Objects.requireNonNull(TtlExecutors.getTtlExecutorService(pool));
  }
}
