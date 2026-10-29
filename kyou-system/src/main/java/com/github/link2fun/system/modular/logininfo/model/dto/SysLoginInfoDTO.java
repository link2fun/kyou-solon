package com.github.link2fun.system.modular.logininfo.model.dto;

import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志响应
 * {@link com.github.link2fun.system.modular.logininfo.model.SysLoginInfo }
 */
@Data
public class SysLoginInfoDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 访问ID */
  @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
  private Long infoId;
  /** 用户名称 */
  @Excel(name = "用户账号")
  private String userName;
  /** 登录状态 */
  @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
  private String status;
  /** 登录IP地址 */
  @Excel(name = "登录地址")
  private String ipaddr;
  /** 登录地点 */
  @Excel(name = "登录地点")
  private String loginLocation;
  /** 浏览器类型 */
  @Excel(name = "浏览器")
  private String browser;
  /** 操作系统 */
  @Excel(name = "操作系统")
  private String os;
  /** 提示消息 */
  @Excel(name = "提示消息")
  private String msg;
  /** 登录时间 */
  @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime loginTime;
}
