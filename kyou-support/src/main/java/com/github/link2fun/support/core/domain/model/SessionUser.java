package com.github.link2fun.support.core.domain.model;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/** 当前会话用户 没有特殊标注的情况下 指的是  sa-token 的 AccountSession */
@Data
public class SessionUser implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 部门ID */
  private Long deptId;

  /** transient 不序列化 */
  private transient SaTokenInfo tokenInfo;
  /** tokenSession 不序列化 */
  private transient SaSession tokenSession;



  /** 当前会话的 token/passkey 来自 tokenSession */
  @JsonProperty("token")
  public String getToken() {
    return Optional.ofNullable(tokenInfo).map(SaTokenInfo::getTokenValue).orElse(null);
  }

  /** 登录时间: tokenSession */
  public Long getLoginTime() {
    // 登录了,
//    return getTokenSession().getCreateTime();
    return Optional.ofNullable(tokenSession).map(SaSession::getCreateTime).orElse(null);
  }

  public Long getTimeout() {
//    return getTokenSession().getTimeout();
    return Optional.ofNullable(tokenSession).map(SaSession::getTimeout).orElse(null);
  }


  /** 过期时间 */
  public Long getExpireTime() {

    return getLoginTime() + (getTimeout() == -1 ? 864000 : getTimeout());
  }

  /** 登录IP地址 */
  public String getIpaddr() {
//    return getTokenSession().getString("ipaddr");
    return Optional.ofNullable(tokenSession).map(session -> session.getString("ipaddr")).orElse(null);
  }

  public void setIpaddr(String ipaddr) {
    Optional.ofNullable(tokenSession).ifPresent(session -> session.set("ipaddr", ipaddr));
  }

  /** 登录地点 */
  public String  getLoginLocation() {
    return Optional.ofNullable(tokenSession).map(session -> session.getString("loginLocation")).orElse(null);
  }

  public void setLoginLocation(String loginLocation) {
    Optional.ofNullable(tokenSession).ifPresent(session -> session.set("loginLocation", loginLocation));
  }

  /** 浏览器类型 */
  public String getBrowser() {
    return Optional.ofNullable(tokenSession).map(session -> session.getString("browser")).orElse(null);
  }

  public void setBrowser(String browser) {
    Optional.ofNullable(tokenSession).ifPresent(session -> session.set("browser", browser));
  }

  /** 操作系统 */
  public String getOs() {
    return Optional.ofNullable(tokenSession).map(session -> session.getString("os")).orElse(null);
  }

  public void setOs(String os) {
    Optional.ofNullable(tokenSession).ifPresent(session -> session.set("os", os));
  }

  /** 权限列表 */
  private List<String> permissions;

  /**
   * 用户信息
   */
  private SysUserDTO user;

  /** 从当前会话中加载 tokenInfo 和 tokenSession */
  public SessionUser loadTokenInfoAndTokenSession() {
    this.tokenInfo = StpUtil.getTokenInfo();
    this.tokenSession = StpUtil.getTokenSession();
    return this;
  }


  public String getUsername() {
    return Optional.ofNullable(user).map(SysUserDTO::getUserName).orElse(null);
  }

  public SessionUser() {
  }

  public SessionUser(SysUserDTO user, List<String> permissions) {
    this.tokenSession = StpUtil.getTokenSession();
    this.tokenInfo = StpUtil.getTokenInfo();
    this.user = user;
    this.permissions = permissions;
  }

  public SessionUser(Long userId, Long deptId, SysUserDTO user, List<String> permissions) {
    this.tokenSession = StpUtil.getTokenSession();
    this.tokenInfo = StpUtil.getTokenInfo();
    this.userId = userId;
    this.deptId = deptId;
    this.user = user;
    this.permissions = permissions;
  }

}
