package com.github.link2fun.schedule.modular.job.engine;

import com.github.link2fun.schedule.constant.ScheduleConstants;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.support.exception.ServiceException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.core.util.RankEntity;
import org.noear.solon.scheduling.ScheduledAnno;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.noear.solon.scheduling.scheduled.JobHandler;
import org.noear.solon.scheduling.scheduled.JobHolder;
import org.noear.solon.scheduling.scheduled.JobInterceptor;
import org.noear.solon.scheduling.scheduled.manager.IJobManager;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 调度绑定的纯内存行为: 暂停语义、未注册抛错、拦截器链路由、clearAll 快照删除、同名重绑自愈
 */
class JobBindingTest {

  /** 与引擎同构的内存假件: jobGetAll 是活视图, started 之后的 jobAdd 无条件启动 */
  private static final class FakeJobManager implements IJobManager {
    private final Map<String, JobHolder> jobMap = new HashMap<>();
    private final Set<String> running = new HashSet<>();
    private final List<JobInterceptor> interceptors = new ArrayList<>();
    private boolean started;

    void startEngine() {
      this.started = true;
    }

    @Override
    public void addJobInterceptor(int index, JobInterceptor jobInterceptor) {
      interceptors.add(jobInterceptor);
    }

    @Override
    public boolean hasJobInterceptor() {
      return !interceptors.isEmpty();
    }

    @Override
    public List<RankEntity<JobInterceptor>> getJobInterceptors() {
      final List<RankEntity<JobInterceptor>> ranked = new ArrayList<>();
      interceptors.forEach(interceptor -> ranked.add(new RankEntity<>(interceptor, 0)));
      return ranked;
    }

    @Override
    public JobHolder jobAdd(String name, Scheduled scheduled, JobHandler handler) {
      return jobAdd(name, scheduled, handler, null);
    }

    @Override
    public JobHolder jobAdd(String name, Scheduled scheduled, JobHandler handler, Map<String, String> data) {
      final JobHolder holder = jobMap.computeIfAbsent(name, n -> new JobHolder(this, n, scheduled, handler));
      if (started) {
        // AbstractJobManager: isStarted 后 jobAdd 直接 jobStart, 不看 enable
        running.add(name);
      }
      return holder;
    }

    @Override
    public boolean jobExists(String name) {
      return jobMap.containsKey(name);
    }

    @Override
    public JobHolder jobGet(String name) {
      return jobMap.get(name);
    }

    @Override
    public Map<String, JobHolder> jobGetAll() {
      // 与引擎一致: 返回底层 map 的只读活视图
      return Collections.unmodifiableMap(jobMap);
    }

    @Override
    public void jobRemove(String name) {
      jobMap.remove(name);
      running.remove(name);
    }

    @Override
    public void jobStart(String name, Map<String, String> data) {
      // 引擎对未注册名字静默 no-op
      if (jobMap.containsKey(name)) {
        running.add(name);
      }
    }

    @Override
    public void jobStop(String name) {
      if (jobMap.containsKey(name)) {
        running.remove(name);
      }
    }

    @Override
    public boolean isStarted() {
      return started;
    }

    @Override
    public void start() {
    }
  }

  private static SysJob job(final String name, final String status) {
    return job(name, status, "0/20 * * * * ?");
  }

  private static SysJob job(final String name, final String status, final String cron) {
    final SysJob job = new SysJob();
    job.setJobName(name);
    job.setCronExpression(cron);
    job.setStatus(status);
    return job;
  }

  @Test
  @DisplayName("运行期 bind 暂停任务: 注册后立即停掉, 不随 jobAdd 自动开跑")
  void bindPausedJobWhileRunningStopsImmediately() {
    final FakeJobManager manager = new FakeJobManager();
    manager.startEngine();
    final JobBinding binding = new JobBinding(manager);

    binding.bind(job("paused", ScheduleConstants.Status.PAUSE.getValue()));

    assertTrue(manager.jobExists("paused"), "暂停任务应已注册(供后续恢复)");
    assertFalse(manager.running.contains("paused"), "暂停任务不应处于运行态");
    assertFalse(manager.jobGet("paused").getScheduled().enable(), "enable 应为 false");
  }

  @Test
  @DisplayName("运行期 bind 正常任务: 注册即运行")
  void bindNormalJobWhileRunningKeepsRunning() {
    final FakeJobManager manager = new FakeJobManager();
    manager.startEngine();
    final JobBinding binding = new JobBinding(manager);

    binding.bind(job("active", ScheduleConstants.Status.NORMAL.getValue()));

    assertTrue(manager.running.contains("active"));
    assertTrue(manager.jobGet("active").getScheduled().enable());
  }

  @Test
  @DisplayName("同键重复 bind: 旧 holder 被移除, 新 cron 生效")
  void bindSameNameReplacesOldHolder() {
    final FakeJobManager manager = new FakeJobManager();
    manager.startEngine();
    final JobBinding binding = new JobBinding(manager);

    binding.bind(job("dup", ScheduleConstants.Status.NORMAL.getValue(), "0/20 * * * * ?"));
    final JobHandler firstHandler = manager.jobGet("dup").getHandler();
    binding.bind(job("dup", ScheduleConstants.Status.NORMAL.getValue(), "0/5 * * * * ?"));

    assertEquals(1, manager.jobGetAll().size(), "同名只应留一个 holder");
    assertEquals("0/5 * * * * ?", manager.jobGet("dup").getScheduled().cron(),
      "holder 应被换成新的 ScheduledAnno, 而不是复用旧的");
    assertNotSame(firstHandler, manager.jobGet("dup").getHandler(), "handler 也应换成新注册的那个");
  }

  @Test
  @DisplayName("启动期 bind 暂停任务: 只注册不启动, enable 留给引擎 postStart 读取")
  void bindPausedJobBeforeEngineStartDoesNotRun() {
    final FakeJobManager manager = new FakeJobManager();
    final JobBinding binding = new JobBinding(manager);

    binding.bind(job("paused", ScheduleConstants.Status.PAUSE.getValue()));
    manager.startEngine();

    assertTrue(manager.jobExists("paused"));
    assertFalse(manager.running.contains("paused"));
    assertFalse(manager.jobGet("paused").getScheduled().enable());
  }

  @Test
  @DisplayName("triggerNow 走 JobHolder.handle 的拦截器链")
  void triggerNowRoutesThroughInterceptorChain() throws Throwable {
    final FakeJobManager manager = new FakeJobManager();
    final List<String> seen = new ArrayList<>();
    manager.addJobInterceptor(0, (job, handler) -> {
      seen.add(job.getName());
      // 真实 handler 会反射调用业务 bean, 纯内存环境到不了那一步, 吞掉即可
      try {
        handler.handle(job.getContext());
      } catch (Throwable ignored) {
      }
    });
    final JobBinding binding = new JobBinding(manager);

    binding.bind(job("manual", ScheduleConstants.Status.PAUSE.getValue()));
    binding.triggerNow("manual");

    assertEquals(List.of("manual"), seen, "手动执行应经过注册的拦截器");
  }

  @Test
  @DisplayName("start/stop/triggerNow 遇未注册名字抛业务异常, 暴出库与引擎漂移")
  void unboundNameThrows() {
    final JobBinding binding = new JobBinding(new FakeJobManager());

    assertThrows(ServiceException.class, () -> binding.start("ghost"));
    assertThrows(ServiceException.class, () -> binding.stop("ghost"));
    assertThrows(ServiceException.class, () -> binding.triggerNow("ghost"));
  }

  @Test
  @DisplayName("unbind 未注册名字幂等不抛")
  void unbindUnknownNameIsIdempotent() {
    final JobBinding binding = new JobBinding(new FakeJobManager());

    assertDoesNotThrow(() -> binding.unbind("ghost"));
  }

  @Test
  @DisplayName("clearAll 在活视图上删多个任务不抛 ConcurrentModificationException")
  void clearAllRemovesEveryJobWithoutCme() {
    final FakeJobManager manager = new FakeJobManager();
    manager.jobAdd("a", new ScheduledAnno(), ctx -> {
    });
    manager.jobAdd("b", new ScheduledAnno(), ctx -> {
    });
    manager.jobAdd("c", new ScheduledAnno(), ctx -> {
    });
    final JobBinding binding = new JobBinding(manager);

    binding.clearAll();

    assertTrue(manager.jobGetAll().isEmpty());
  }
}
