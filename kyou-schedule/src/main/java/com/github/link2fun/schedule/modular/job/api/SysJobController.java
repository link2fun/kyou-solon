package com.github.link2fun.schedule.modular.job.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import com.github.link2fun.schedule.util.ScheduleUtils;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.exception.job.TaskException;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.poi.ExcelUtil;

import org.noear.java_cron.CronUtils;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;

import java.util.List;

/**
 * 调度任务信息操作处理
 */
@Controller
@Mapping("/monitor/job")
public class SysJobController extends BaseController {
  @Inject
  private ISysJobService jobService;

  /**
   * 查询定时任务列表
   */
  @SaCheckPermission(value = {"monitor:job:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysJob sysJob) {

    Page<SysJob> list = jobService.selectJobList(Page.ofCurrentContext(), sysJob);
    return getDataTable(list);
  }

  /**
   * 导出定时任务列表
   */
  @SaCheckPermission(value = {"monitor:job:export"})
  @Log(title = "定时任务", businessType = BusinessType.EXPORT)
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysJob sysJob) {
    Page<SysJob> list = jobService.selectJobList(Page.ofAll(), sysJob);
    ExcelUtil<SysJob> util = new ExcelUtil<SysJob>(SysJob.class);
    util.exportExcel(response, list.getRecords(), "定时任务");
  }

  /**
   * 获取定时任务详细信息
   */
  @SaCheckPermission(value = {"monitor:job:query"})
  @Mapping(value = "/{jobId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path("jobId") Long jobId) {
    return success(jobService.selectJobById(jobId));
  }

  /**
   * 新增定时任务
   */
  @SaCheckPermission(value = {"monitor:job:add"})
  @Log(title = "定时任务", businessType = BusinessType.INSERT)
  @Mapping(value = "/add", method = MethodType.POST)
  public AjaxResult add(@Body SysJob job) throws TaskException {
    if (!CronUtils.isValid(job.getCronExpression())) {
      return error("新增任务'" + job.getJobName() + "'失败，Cron表达式不正确");
    } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串存在违规");
    } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
      return error("新增任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
    }
    job.setCreateBy(getUsername());
    return toAjax(jobService.insertJob(job));
  }

  /**
   * 修改定时任务
   */
  @SaCheckPermission(value = {"monitor:job:edit"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "", method = MethodType.PUT)
  public AjaxResult edit(@Body SysJob job) throws TaskException {
    if (!CronUtils.isValid(job.getCronExpression())) {
      return error("修改任务'" + job.getJobName() + "'失败，Cron表达式不正确");
    } else if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS})) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), new String[]{Constants.HTTP, Constants.HTTPS})) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
    } else if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串存在违规");
    } else if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
      return error("修改任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
    }
    job.setUpdateBy(getUsername());
    return toAjax(jobService.updateJob(job));
  }

  /**
   * 定时任务状态修改
   */
  @SaCheckPermission(value = {"monitor:job:changeStatus"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public AjaxResult changeStatus(@Body SysJob job) {
    SysJob newJob = jobService.selectJobById(job.getJobId());
    newJob.setStatus(job.getStatus());
    return toAjax(jobService.changeStatus(newJob));
  }

  /**
   * 定时任务立即执行一次
   */
  @SaCheckPermission(value = {"monitor:job:changeStatus"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "/run", method = MethodType.PUT)
  public AjaxResult run(@Body SysJob job) throws Throwable {
    boolean result = jobService.run(job);
    return result ? success() : error("任务不存在或已过期！");
  }

  /**
   * 删除定时任务
   */
  @SaCheckPermission(value = {"monitor:job:remove"})
  @Log(title = "定时任务", businessType = BusinessType.DELETE)
  @Mapping(value = "/{jobIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> jobIds) {
    jobService.deleteJobByIds(jobIds);
    return success();
  }
}
