package com.github.link2fun.schedule.modular.job.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.schedule.modular.job.model.SysJobLog;
import com.github.link2fun.schedule.modular.job.service.ISysJobLogService;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;

import java.util.List;

/**
 * 调度日志操作处理
 *
 * @author link2fun
 */
@Controller
@Mapping("/monitor/jobLog")
public class SysJobLogController extends BaseController {

  @Inject
  private ISysJobLogService jobLogService;

  /**
   * 查询定时任务调度日志列表
   */
  @SaCheckPermission(value = {"monitor:job:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysJobLog sysJobLog) {
    Page<SysJobLog> page = jobLogService.pageSearchJobLog(Page.ofCurrentContext(), sysJobLog);
    return getDataTable(page);
  }

  /**
   * 导出定时任务调度日志列表
   */
  @SaCheckPermission(value = {"monitor:job:export"})
  @Log(title = "任务调度日志", businessType = BusinessType.EXPORT)
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysJobLog sysJobLog) {
    List<SysJobLog> list = jobLogService.selectJobLogList(sysJobLog);
    ExcelUtil<SysJobLog> util = new ExcelUtil<SysJobLog>(SysJobLog.class);
    util.exportExcel(response, list, "调度日志");
  }

  /**
   * 根据调度编号获取详细信息
   */
  @SaCheckPermission(value = {"monitor:job:query"})
  @Mapping(value = "/{jobLogId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long jobLogId) {
    return success(jobLogService.selectJobLogById(jobLogId));
  }


  /**
   * 删除定时任务调度日志
   */
  @SaCheckPermission(value = {"monitor:job:remove"})
  @Log(title = "定时任务调度日志", businessType = BusinessType.DELETE)
  @Mapping(value = "/{jobLogIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> jobLogIds) {
    return toAjax(jobLogService.deleteJobLogByIds(jobLogIds));
  }

  /**
   * 清空定时任务调度日志
   */
  @SaCheckPermission(value = {"monitor:job:remove"})
  @Log(title = "调度日志", businessType = BusinessType.CLEAN)
  @Mapping(value = "/clean", method = MethodType.DELETE)
  public AjaxResult clean() {
    jobLogService.cleanJobLog();
    return success();
  }
}
