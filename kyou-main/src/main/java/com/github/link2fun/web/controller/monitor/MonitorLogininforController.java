package com.github.link2fun.web.controller.monitor;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.framework.web.service.SysPasswordService;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.logininfo.model.SysLogininfor;
import com.github.link2fun.system.modular.logininfo.service.ISystemLogininforService;

import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;

import java.util.List;

/**
 * 系统访问记录
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/logininfor")
public class MonitorLogininforController extends BaseController {
  @Inject
  private ISystemLogininforService logininforService;

  @Inject
  private SysPasswordService passwordService;

  @SaCheckPermission(value = "monitor:logininfor:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysLogininfor searchReq) {

    Page<SysLogininfor> list = logininforService.selectLogininforList(Page.ofCurrentContext(), searchReq);
    return getDataTable(list);
  }

  @Log(title = "登录日志", businessType = BusinessType.EXPORT)
  @SaCheckPermission(value = "monitor:logininfor:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysLogininfor logininfor) {
    Page<SysLogininfor> list = logininforService.selectLogininforList(Page.ofAll(), logininfor);
    ExcelUtil<SysLogininfor> util = new ExcelUtil<>(SysLogininfor.class);
    util.exportExcel(context, list.getRecords(), "登录日志");
  }

  @SaCheckPermission(value = "monitor:logininfor:remove")
  @Log(title = "登录日志", businessType = BusinessType.DELETE)
  @Mapping(value = "/{infoIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> infoIds) {
    return toAjax(logininforService.deleteLogininforByIds(infoIds));
  }

  @SaCheckPermission(value = "monitor:logininfor:remove")
  @Log(title = "登录日志", businessType = BusinessType.CLEAN)
  @Mapping(value = "/clean", method = MethodType.DELETE)
  public AjaxResult clean() {
    logininforService.cleanLogininfor();
    return success();
  }

  @SaCheckPermission(value = "monitor:logininfor:unlock")
  @Log(title = "账户解锁", businessType = BusinessType.OTHER)
  @Mapping(value = "/unlock/{userName}", method = MethodType.GET)
  public AjaxResult unlock(@Path("userName") String userName) {
    passwordService.removePwdErrCnt(userName);
    return success();
  }
}
