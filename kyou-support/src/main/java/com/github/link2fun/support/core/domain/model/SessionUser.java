package com.github.link2fun.support.core.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * 当前会话用户 没有特殊标注的情况下 指的是 sa-token 的 AccountSession
 *
 * <p>纯数据对象: 会话读写一律经 {@code SaSessionBizTool}, 本类不感知登录框架。
 */
@Data
public class SessionUser implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 用户ID */
  private Long userId;

  /** 部门ID */
  private Long deptId;

  /** 当前会话的 token/passkey */
  @JsonProperty("token")
  private String token;

  /** 登录时间 */
  private Long loginTime;

  /** 登录IP地址 */
  private String ipaddr;

  /** 登录地点 */
  private String loginLocation;

  /** 浏览器类型 */
  private String browser;

  /** 操作系统 */
  private String os;

  /** 权限列表 */
  private List<String> permissions;

  /** 用户信息 */
  private SysUserDTO user;

  /** 登录账号名, 用户信息未加载时为空 */
  public String getUsername() {
    return Optional.ofNullable(user).map(SysUserDTO::getUserName).orElse(null);
  }

}
