package com.github.link2fun.system.modular.useronline.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.domain.SysUserOnline;
import com.github.link2fun.system.modular.useronline.service.ISystemUserOnlineService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SystemUserOnlineServiceImpl implements ISystemUserOnlineService {

  /**
   * 通过登录地址查询信息
   *
   * @param ipaddr 登录地址
   * @param user   用户信息
   * @return 在线用户信息
   */
  @Override
  public SysUserOnline selectOnlineByIpaddr(String ipaddr, SessionUser user) {
    if (StrUtil.equals(ipaddr, user.getIpaddr())) {
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * 通过用户名称查询信息
   *
   * @param userName 用户名称
   * @param user     用户信息
   * @return 在线用户信息
   */
  @Override
  public SysUserOnline selectOnlineByUserName(String userName, SessionUser user) {
    if (StrUtil.equals(userName, user.getUsername())) {
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * 通过登录地址/用户名称查询信息
   *
   * @param ipaddr   登录地址
   * @param userName 用户名称
   * @param user     用户信息
   * @return 在线用户信息
   */
  @Override
  public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, SessionUser user) {
    if (StrUtil.equals(ipaddr, user.getIpaddr()) && StrUtil.equals(userName, user.getUsername())) {
      return loginUserToUserOnline(user);
    }
    return null;
  }

  /**
   * 设置在线用户信息
   *
   * @param user 用户信息
   * @return 在线用户
   */
  @Override
  public SysUserOnline loginUserToUserOnline(SessionUser user) {
    if (ObjectUtil.isNull(user) || ObjectUtil.isNull(user.getUser())) {
      return null;
    }
    SysUserOnline sysUserOnline = new SysUserOnline();
    sysUserOnline.setTokenId(user.getToken());
    sysUserOnline.setUserName(user.getUsername());
    sysUserOnline.setIpaddr(user.getIpaddr());
    sysUserOnline.setLoginLocation(user.getLoginLocation());
    sysUserOnline.setBrowser(user.getBrowser());
    sysUserOnline.setOs(user.getOs());
    sysUserOnline.setLoginTime(user.getLoginTime());
    if (ObjectUtil.isNotNull(user.getUser().getDept())) {
      sysUserOnline.setDeptName(user.getUser().getDept().getDeptName());
    }
    return sysUserOnline;
  }

  /**
   * 查询在线用户列表, 支持按登录地址/用户名称过滤
   *
   * @param pageRequest 分页参数(由调用方从请求解析)
   * @param ipaddr      登录地址, 可空
   * @param userName    用户名称, 可空
   * @return 在线用户分页结果
   */
  @Override
  public Page<SysUserOnline> selectOnlineUserList(final Page<SessionUser> pageRequest,
                                                  final String ipaddr, final String userName) {
    // 会话列表来自 sa-token, 无法在库侧分页, 因此先取全量再按请求切片
    final Page<SessionUser> onlineUserPage = pageRequest.buildPage(SaSessionBizTool.listOnlineSessionUsers());

    final List<SysUserOnline> userOnlineList = new ArrayList<>();
    for (final SessionUser selectedUser : onlineUserPage.getRecords()) {
      if (StrUtil.isNotBlank(ipaddr) && StrUtil.isNotBlank(userName)) {
        userOnlineList.add(selectOnlineByInfo(ipaddr, userName, selectedUser));
      } else if (StrUtil.isNotBlank(ipaddr)) {
        userOnlineList.add(selectOnlineByIpaddr(ipaddr, selectedUser));
      } else if (StrUtil.isNotBlank(userName) && ObjectUtil.isNotNull(selectedUser.getUser())) {
        userOnlineList.add(selectOnlineByUserName(userName, selectedUser));
      } else {
        userOnlineList.add(loginUserToUserOnline(selectedUser));
      }
    }
    // 不匹配过滤条件的项为 null, 需剔除; 逆序让最近登录的排前面
    userOnlineList.removeIf(Objects::isNull);
    Collections.reverse(userOnlineList);
    return onlineUserPage.convert(userOnlineList);
  }
}
