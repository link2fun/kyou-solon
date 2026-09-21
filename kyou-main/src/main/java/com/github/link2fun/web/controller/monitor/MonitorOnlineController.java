package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.domain.SysUserOnline;
import com.github.link2fun.system.modular.useronline.service.ISystemUserOnlineService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


/**
 * 在线用户监控
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/online")
public class MonitorOnlineController extends BaseController {

  @Inject
  private ISystemUserOnlineService userOnlineService;

  @RepeatSubmit(message = "请勿频繁请求在线用户数据，请稍后再试")
  @SaCheckPermission(value = {"monitor:online:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(String ipaddr, String userName) {

    // 全部在线会话用户按当前请求分页
    final Page<SessionUser> onlineUserPage = Page.<SessionUser>ofCurrentContext()
      .buildPage(SaSessionBizTool.listOnlineSessionUsers());

    List<SysUserOnline> userOnlineList = new ArrayList<>();
    for (SessionUser selectedUser : onlineUserPage.getRecords()) {
      if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
        userOnlineList.add(userOnlineService.selectOnlineByInfo(ipaddr, userName, selectedUser));
      } else if (StringUtils.isNotEmpty(ipaddr)) {
        userOnlineList.add(userOnlineService.selectOnlineByIpaddr(ipaddr, selectedUser));
      } else if (StringUtils.isNotEmpty(userName) && StringUtils.isNotNull(selectedUser.getUser())) {
        userOnlineList.add(userOnlineService.selectOnlineByUserName(userName, selectedUser));
      } else {
        userOnlineList.add(userOnlineService.loginUserToUserOnline(selectedUser));
      }
    }
    Collections.reverse(userOnlineList);

    userOnlineList.removeIf(Objects::isNull);
    return getDataTable(onlineUserPage.convertToPage(userOnlineList));
  }

  /**
   * 强退用户
   */
  @SaCheckPermission(value = {"monitor:online:forceLogout"})
  @Log(title = "在线用户", businessType = BusinessType.FORCE)
  @Mapping(value = "/{tokenId}", method = MethodType.DELETE)
  public AjaxResult forceLogout(@Path String tokenId) {
    SaSessionBizTool.kickout(tokenId);
    return success();
  }
}
