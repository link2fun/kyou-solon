package com.github.link2fun.schedule.modular.job.interceptor;

import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.model.req.*;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import com.github.link2fun.support.core.page.Page;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.ContextEmpty;
import org.noear.solon.scheduling.annotation.Scheduled;
import org.noear.solon.scheduling.scheduled.Job;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 拦截器的纯内存行为: 按任务名直取行、非 DB 任务放行不记日志、成败日志落档
 */
class JobInterceptorImplTest {

  /** 只关心 jobGet 的假件: 记录入参, 返回预置行 */
  private static final class FakeJobService implements ISysJobService {
    private final List<String> jobGetCalls = new ArrayList<>();
    private SysJob row;

    @Override
    public SysJob jobGet(final String jobName) {
      jobGetCalls.add(jobName);
      return row;
    }

    @Override
    public <T> Page<T> selectJobList(final Page<T> pageRequest, final SysJobSearchReq searchReq, final Class<T> resultClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T selectJobById(final Long jobId, final Class<T> resultClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long pauseJob(final Long jobId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long resumeJob(final Long jobId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long deleteJob(final Long jobId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void deleteJobByIds(final List<Long> jobIds) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long changeStatus(final SysJobChangeStatusReq changeStatusReq) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void run(final Long jobId) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long insertJob(final SysJobAddReq addReq) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long updateJob(final SysJobModifyReq modifyReq) {
      throw new UnsupportedOperationException();
    }
  }

  /** 只关心 addJobLog 的假件: 收集落档的日志 */
  private static final class FakeLogService implements ISysJobLogService {
    private final List<SysJobLog> logs = new ArrayList<>();

    @Override
    public void addJobLog(final SysJobLog jobLog) {
      logs.add(jobLog);
    }

    @Override
    public <T> List<T> selectJobLogList(final SysJobLogSearchReq searchReq, final Class<T> resultClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> T selectJobLogById(final Long jobLogId, final Class<T> resultClass) {
      throw new UnsupportedOperationException();
    }

    @Override
    public long deleteJobLogByIds(final List<Long> logIds) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cleanJobLog() {
      throw new UnsupportedOperationException();
    }

    @Override
    public <T> Page<T> pageSearchJobLog(final Page<T> pageRequest, final SysJobLogSearchReq searchReq, final Class<T> resultClass) {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  @DisplayName("引擎键即任务名: 取行按 job.getName() 直取")
  void looksUpRowByJobNameVerbatim() throws Throwable {
    final FakeJobService jobService = new FakeJobService();
    jobService.row = dbRow("same", "SYSTEM");
    final JobInterceptorImpl interceptor = new JobInterceptorImpl(jobService, new FakeLogService());

    interceptor.doIntercept(engineJob("same"), ctx -> {
    });

    assertEquals(List.of("same"), jobService.jobGetCalls, "取行应拿到引擎键原样的任务名");
  }

  @Test
  @DisplayName("非 DB 任务(sys_job 无行)放行执行, 不落日志")
  void nonDbJobPassesThroughWithoutLogging() throws Throwable {
    final FakeJobService jobService = new FakeJobService();
    final FakeLogService logService = new FakeLogService();
    final JobInterceptorImpl interceptor = new JobInterceptorImpl(jobService, logService);
    final Context jobContext = ContextEmpty.create();
    final List<Context> handled = new ArrayList<>();

    interceptor.doIntercept(engineJob("anno", jobContext), handled::add);

    assertEquals(List.of(jobContext), handled, "放行时应把 job 自带的 context 原样交给 handler");
    assertTrue(logService.logs.isEmpty(), "无 sys_job 行的任务不应产生执行日志");
  }

  @Test
  @DisplayName("执行成功: 日志落任务名与行的组名, 状态 0")
  void successfulRunLoggedWithStatusZero() throws Throwable {
    final FakeJobService jobService = new FakeJobService();
    jobService.row = dbRow("same", "SYSTEM");
    final FakeLogService logService = new FakeLogService();
    final JobInterceptorImpl interceptor = new JobInterceptorImpl(jobService, logService);

    interceptor.doIntercept(engineJob("same"), ctx -> {
    });

    assertEquals(1, logService.logs.size());
    final SysJobLog jobLog = logService.logs.get(0);
    assertEquals("same", jobLog.getJobName());
    assertEquals("SYSTEM", jobLog.getJobGroup());
    assertEquals("0", jobLog.getStatus());
    assertNull(jobLog.getExceptionInfo());
  }

  @Test
  @DisplayName("执行失败: 状态 1, 异常信息落档, 失败不外抛")
  void failingRunLoggedWithStatusOne() {
    final FakeJobService jobService = new FakeJobService();
    jobService.row = dbRow("same", "SYSTEM");
    final FakeLogService logService = new FakeLogService();
    final JobInterceptorImpl interceptor = new JobInterceptorImpl(jobService, logService);

    assertDoesNotThrow(() -> interceptor.doIntercept(engineJob("same"), ctx -> {
      throw new IllegalStateException("boom");
    }));

    assertEquals(1, logService.logs.size());
    final SysJobLog jobLog = logService.logs.get(0);
    assertEquals("1", jobLog.getStatus());
    assertEquals("boom", jobLog.getJobMessage());
    assertNotNull(jobLog.getExceptionInfo());
  }

  private static Job engineJob(final String engineKey) {
    return engineJob(engineKey, ContextEmpty.create());
  }

  private static Job engineJob(final String engineKey, final Context context) {
    return new Job() {
      @Override
      public String getName() {
        return engineKey;
      }

      @Override
      public Scheduled getScheduled() {
        return null;
      }

      @Override
      public Context getContext() {
        return context;
      }
    };
  }

  private static SysJob dbRow(final String name, final String group) {
    final SysJob job = new SysJob();
    job.setJobName(name);
    job.setJobGroup(group);
    job.setInvokeTarget("ryTask.ryNoParams");
    return job;
  }
}
