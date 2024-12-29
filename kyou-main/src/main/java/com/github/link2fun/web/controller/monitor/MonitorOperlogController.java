package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.annotation.RateLimiter;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.enums.LimitType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.operlog.model.SysOperLog;
import com.github.link2fun.system.modular.operlog.service.ISystemOperLogService;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;

import java.util.List;


/**
 * 操作日志记录
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/operlog")
public class MonitorOperlogController extends BaseController {
  @Inject
  private ISystemOperLogService operLogService;

  @RateLimiter(count = 10, limitType = LimitType.USER)
  @SaCheckPermission(value = "monitor:operlog:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysOperLog searchReq) {

    Page<SysOperLog> list = operLogService.selectOperLogList(Page.ofCurrentContext(), searchReq);
    return getDataTable(list);
  }

  @Log(title = "操作日志", businessType = BusinessType.EXPORT)
  @SaCheckPermission(value = "monitor:operlog:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysOperLog operLog) {
    Page<SysOperLog> list = operLogService.selectOperLogList(Page.ofAll(), operLog);
    ExcelUtil<SysOperLog> util = new ExcelUtil<>(SysOperLog.class);
    util.exportExcel(response, list.getRecords(), "操作日志");
  }

  @Log(title = "操作日志", businessType = BusinessType.DELETE)
  @SaCheckPermission(value = "monitor:operlog:remove")
  @Mapping(value = "/{operIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> operIds) {
    return toAjax(operLogService.deleteOperLogByIds(operIds));
  }

  @Log(title = "操作日志", businessType = BusinessType.CLEAN)
  @SaCheckPermission(value = "monitor:operlog:remove")
  @Mapping(value = "/clean", method = MethodType.DELETE)
  public AjaxResult clean() {
    operLogService.cleanOperLog();
    return success();
  }
}
