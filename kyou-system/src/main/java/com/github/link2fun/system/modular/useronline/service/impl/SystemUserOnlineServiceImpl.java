package com.github.link2fun.system.modular.useronline.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.system.domain.SysUserOnline;
import com.github.link2fun.system.modular.useronline.service.ISystemUserOnlineService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

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
}
