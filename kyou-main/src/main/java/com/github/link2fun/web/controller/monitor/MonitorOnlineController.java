package com.github.link2fun.web.controller.monitor;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.annotation.RepeatSubmit;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.domain.SysUserOnline;
import com.github.link2fun.system.modular.useronline.service.ISystemUserOnlineService;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Path;
import org.noear.solon.core.handle.MethodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  private static final Logger log = LoggerFactory.getLogger(MonitorOnlineController.class);
  @Inject
  private ISystemUserOnlineService userOnlineService;

  @RepeatSubmit(message = "请勿频繁请求在线用户数据，请稍后再试")
  @SaCheckPermission(value = {"monitor:online:list"})
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(String ipaddr, String userName) {

    SaSessionBizTool.getCurrentUser();

    final List<String> tokenCacheKeyList_All = StpUtil.searchTokenValue("", 0, Integer.MAX_VALUE, false);

    // Page 分割 sessionIdList
    final Page<String> tokenCacheKeyListPage = Page.<String>ofCurrentContext().buildPage(tokenCacheKeyList_All);

    final List<String> tokenKeyList = tokenCacheKeyListPage.getRecords();

    List<SysUserOnline> userOnlineList = new ArrayList<>();
    for (String cacheKey : tokenKeyList) {
      // passkey:login:token:b61dd4b1-b822-4f4f-b9a2-05ff6c7fa777
      // 取最后一个冒号后面的值
      String tokenValue = StrUtil.subAfter(cacheKey, ":", true);
      SaSession tokenSession = StpUtil.getTokenSessionByToken(tokenValue);


      final Object loginId = StpUtil.getLoginIdByToken(tokenValue);
      // tokenValue 找不到对应的 loginId , 应该是个异常的 token， 直接注销掉
      if (loginId == null) {
        log.error("tokenValue: {} not found loginId, logout it", tokenValue);
        StpUtil.logoutByTokenValue(tokenValue);
        continue;
      }
      final SaSession session = StpUtil.getSessionByLoginId(loginId);
      session.setToken(tokenValue);
      SessionUser selectedUser = session.getModel(Constants.SESSION_USER, SessionUser.class);
      selectedUser.setTokenSession(tokenSession);
//      selectedUser.setTokenInfo();
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
    return getDataTable(tokenCacheKeyListPage.convertToPage(userOnlineList));
  }

  /**
   * 强退用户
   */
  @SaCheckPermission(value = {"monitor:online:forceLogout"})
  @Log(title = "在线用户", businessType = BusinessType.FORCE)
  @Mapping(value = "/{tokenId}", method = MethodType.DELETE)
  public AjaxResult forceLogout(@Path String tokenId) {
    StpUtil.kickoutByTokenValue(tokenId);
    return success();
  }
}
