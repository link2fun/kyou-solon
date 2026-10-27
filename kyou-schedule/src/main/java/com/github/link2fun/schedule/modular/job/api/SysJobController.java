package com.github.link2fun.schedule.modular.job.api;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.schedule.modular.job.model.SysJob;
import com.github.link2fun.schedule.modular.job.model.req.*;
import com.github.link2fun.schedule.modular.job.service.ISysJobService;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.exception.job.TaskException;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

/**
 * 调度任务信息操作处理
 */
@Controller
@Mapping("/monitor/job")
public class SysJobController {
  @Inject
  private ISysJobService jobService;

  /**
   * 查询定时任务列表
   */
  @SaCheckPermission(value = {"monitor:job:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysJobSearchReq sysJob) {

    Page<SysJob> list = jobService.selectJobList(Page.ofCurrentContext(), sysJob);
    return TableDataInfo.of(list);
  }

  /**
   * 导出定时任务列表
   */
  @SaCheckPermission(value = {"monitor:job:export"})
  @Log(title = "定时任务", businessType = BusinessType.EXPORT)
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysJobSearchReq sysJob) {
    Page<SysJob> list = jobService.selectJobList(Page.ofAll(), sysJob);
    ExcelUtil<SysJob> util = new ExcelUtil<>(SysJob.class);
    util.exportExcel(response, list.getRecords(), "定时任务");
  }

  /**
   * 获取定时任务详细信息
   */
  @SaCheckPermission(value = {"monitor:job:query"})
  @Mapping(value = "/{jobId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path("jobId") Long jobId) {
    return AjaxResult.successData(jobService.selectJobById(jobId));
  }

  /**
   * 新增定时任务
   */
  @SaCheckPermission(value = {"monitor:job:add"})
  @Log(title = "定时任务", businessType = BusinessType.INSERT)
  @Mapping(value = "/add", method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysJobAddReq addReq) throws TaskException {
    return AjaxResult.ofAffected(jobService.insertJob(addReq));
  }

  /**
   * 修改定时任务
   */
  @SaCheckPermission(value = {"monitor:job:edit"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "", method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysJobModifyReq modifyReq) throws TaskException {
    return AjaxResult.ofAffected(jobService.updateJob(modifyReq));
  }

  /**
   * 定时任务状态修改
   */
  @SaCheckPermission(value = {"monitor:job:changeStatus"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public AjaxResult changeStatus(@Validated @Body SysJobChangeStatusReq changeStatusReq) {
    return AjaxResult.ofAffected(jobService.changeStatus(changeStatusReq));
  }

  /**
   * 定时任务立即执行一次
   */
  @SaCheckPermission(value = {"monitor:job:changeStatus"})
  @Log(title = "定时任务", businessType = BusinessType.UPDATE)
  @Mapping(value = "/run", method = MethodType.PUT)
  public AjaxResult run(@Validated @Body SysJobRunReq runReq) throws Throwable {
    boolean result = jobService.run(runReq.getJobId());
    return result ? AjaxResult.success() : AjaxResult.error("任务不存在或已过期！");
  }

  /**
   * 删除定时任务
   *
   * <p>只停止后续调度, 已经开始的那一次执行会自然跑完, 不会因删除而被中断。
   */
  @SaCheckPermission(value = {"monitor:job:remove"})
  @Log(title = "定时任务", businessType = BusinessType.DELETE)
  @Mapping(value = "/{jobIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> jobIds) {
    jobService.deleteJobByIds(jobIds);
    return AjaxResult.success();
  }
}
