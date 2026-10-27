package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.system.modular.useronline.service.ISystemUserOnlineService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;

/**
 * 在线用户监控
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/online")
public class MonitorOnlineController {

  @Inject
  private ISystemUserOnlineService userOnlineService;

  @RepeatSubmit(message = "请勿频繁请求在线用户数据，请稍后再试")
  @SaCheckPermission(value = {"monitor:online:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(String ipaddr, String userName) {
    return TableDataInfo.of(userOnlineService.selectOnlineUserList(Page.ofCurrentContext(), ipaddr, userName));
  }

  /**
   * 强退用户
   */
  @SaCheckPermission(value = {"monitor:online:forceLogout"})
  @Log(title = "在线用户", businessType = BusinessType.FORCE, mask = true)
  @Mapping(value = "/{tokenId}", method = MethodType.DELETE)
  public AjaxResult forceLogout(@Path String tokenId) {
    SaSessionBizTool.kickout(tokenId);
    return AjaxResult.success();
  }
}
