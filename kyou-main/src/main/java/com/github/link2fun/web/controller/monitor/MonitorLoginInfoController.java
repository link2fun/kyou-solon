package com.github.link2fun.web.controller.monitor;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.framework.auth.LoginGuard;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.logininfo.model.dto.SysLoginInfoDTO;
import com.github.link2fun.system.modular.logininfo.model.req.SysLoginInfoSearchReq;
import com.github.link2fun.system.modular.logininfo.service.ISystemLoginInfoService;
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
public class MonitorLoginInfoController {
  @Inject
  private ISystemLoginInfoService loginInfoService;

  @Inject
  private LoginGuard loginGuard;

  /** 查询登录日志列表 */
  @SaCheckPermission(value = "monitor:logininfor:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysLoginInfoSearchReq searchReq) {

    Page<SysLoginInfoDTO> list = loginInfoService.selectLoginInfoList(Page.ofCurrentContext(), searchReq, SysLoginInfoDTO.class);
    return TableDataInfo.of(list);
  }

  /** 导出登录日志 */
  @Log(title = "登录日志", businessType = BusinessType.EXPORT)
  @SaCheckPermission(value = "monitor:logininfor:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysLoginInfoSearchReq searchReq) {
    Page<SysLoginInfoDTO> list = loginInfoService.selectLoginInfoList(Page.ofAll(), searchReq, SysLoginInfoDTO.class);
    ExcelUtil<SysLoginInfoDTO> util = new ExcelUtil<>(SysLoginInfoDTO.class);
    util.exportExcel(context, list.getRecords(), "登录日志");
  }

  /** 批量删除登录日志 */
  @SaCheckPermission(value = "monitor:logininfor:remove")
  @Log(title = "登录日志", businessType = BusinessType.DELETE)
  @Mapping(value = "/{infoIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> infoIds) {
    return Resp.ofAffected(loginInfoService.deleteLoginInfoByIds(infoIds));
  }

  /** 清空登录日志 */
  @SaCheckPermission(value = "monitor:logininfor:remove")
  @Log(title = "登录日志", businessType = BusinessType.CLEAN)
  @Mapping(value = "/clean", method = MethodType.DELETE)
  public Resp<Void> clean() {
    loginInfoService.cleanLoginInfo();
    return Resp.success();
  }

  /** 解锁账户: 清除该账号的密码重试计数 */
  @SaCheckPermission(value = "monitor:logininfor:unlock")
  @Log(title = "账户解锁", businessType = BusinessType.OTHER)
  @Mapping(value = "/unlock/{userName}", method = MethodType.GET)
  public Resp<Void> unlock(@Path("userName") String userName) {
    loginGuard.unlock(userName);
    return Resp.success();
  }
}
