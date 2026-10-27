package com.github.link2fun.system.modular.useronline.service;


import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.domain.SysUserOnline;

/**
 * 在线用户 服务层
 *
 * @author ruoyi
 */
public interface ISystemUserOnlineService {
  /**
   * 通过登录地址查询信息
   *
   * @param ipaddr 登录地址
   * @param user   用户信息
   * @return 在线用户信息
   */
  SysUserOnline selectOnlineByIpaddr(String ipaddr, SessionUser user);

  /**
   * 通过用户名称查询信息
   *
   * @param userName 用户名称
   * @param user     用户信息
   * @return 在线用户信息
   */
  SysUserOnline selectOnlineByUserName(String userName, SessionUser user);

  /**
   * 通过登录地址/用户名称查询信息
   *
   * @param ipaddr   登录地址
   * @param userName 用户名称
   * @param user     用户信息
   * @return 在线用户信息
   */
  SysUserOnline selectOnlineByInfo(String ipaddr, String userName, SessionUser user);

  /**
   * 设置在线用户信息
   *
   * @param user 用户信息
   * @return 在线用户
   */
  SysUserOnline loginUserToUserOnline(SessionUser user);

  /**
   * 查询在线用户列表, 支持按登录地址/用户名称过滤
   *
   * <p>枚举全部在线会话后按给定分页切片; 过滤条件为空时不过滤。
   *
   * @param pageRequest 分页参数(由调用方从请求解析)
   * @param ipaddr      登录地址, 可空
   * @param userName    用户名称, 可空
   * @return 在线用户分页结果
   */
  Page<SysUserOnline> selectOnlineUserList(Page<SessionUser> pageRequest, String ipaddr, String userName);
}
