package com.github.link2fun.framework.manager;

import cn.hutool.core.thread.ThreadFactoryBuilder;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.github.link2fun.support.utils.Threads;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 异步任务管理器
 *
 * @author ruoyi
 * @author link2fun
 */
@Slf4j
public class AsyncManager {


  /**
   * 异步操作任务调度线程池
   * 线程池使用 TtlExecutor 包装 方便 tlog 传递 traceId
   */
  private static final ExecutorService executor;

  static {
    // 核心线程数，根据实际情况调整
    int corePoolSize = 4;
    // 最大线程数，根据实际情况调整
    int maxPoolSize = 8;
    // 空闲线程的存活时间，根据实际情况调整
    long keepAliveTime = 60L;
    // 存活时间的单位
    TimeUnit unit = TimeUnit.SECONDS;
    // 工作队列，根据实际情况调整
    BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
    // 线程工厂，用于创建新的线程
    ThreadFactory threadFactory = new ThreadFactoryBuilder().setNamePrefix("async-pool-").build();
    // 拒绝策略，这里选择了"调用者运行"策略
    RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();

    ExecutorService logExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    executor = Objects.requireNonNull(TtlExecutors.getTtlExecutorService(logExecutor));
  }


  /**
   * 单例模式
   */
  private AsyncManager() {
  }

  private static final AsyncManager me = new AsyncManager();

  public static AsyncManager me() {
    return me;
  }

  /**
   * 执行任务
   *
   * @param task 任务
   */
  public void execute(TimerTask task) {
    executor.execute(task);
  }

  /**
   * 停止任务线程池
   */
  public void shutdown() {
    log.info("[关闭线程池] 开始关闭线程池");
    Threads.shutdownAndAwaitTermination(executor);
    log.info("[关闭线程池] 线程池已关闭");

  }
}
