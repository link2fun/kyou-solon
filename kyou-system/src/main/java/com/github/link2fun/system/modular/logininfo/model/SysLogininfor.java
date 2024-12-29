package com.github.link2fun.system.modular.logininfo.model;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.QueryEntity;


import com.github.link2fun.system.modular.logininfo.model.proxy.SysLogininforProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * 系统访问记录表 sys_logininfor
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@Table("sys_logininfor")
public class SysLogininfor extends QueryEntity implements ProxyEntityAvailable<SysLogininfor, SysLogininforProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** ID */
  @Excel(name = "序号", cellType = Excel.ColumnType.NUMERIC)
  @Column(value = "info_id",primaryKey = true,generatedKey = true)
  private Long infoId;

  /** 用户账号 */
  @Excel(name = "用户账号")
  @Column("user_name")
  private String userName;

  /** 登录状态 0成功 1失败 */
  @Excel(name = "登录状态", readConverterExp = "0=成功,1=失败")
  @Column("status")
  private String status;

  /** 登录IP地址 */
  @Excel(name = "登录地址")
  @Column("ipaddr")
  private String ipaddr;

  /** 登录地点 */
  @Excel(name = "登录地点")
  @Column("login_location")
  private String loginLocation;

  /** 浏览器类型 */
  @Excel(name = "浏览器")
  @Column("browser")
  private String browser;

  /** 操作系统 */
  @Excel(name = "操作系统")
  @Column("os")
  private String os;

  /** 提示消息 */
  @Excel(name = "提示消息")
  @Column("msg")
  private String msg;

  /** 访问时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @Excel(name = "访问时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
  @Column("login_time")
  private LocalDateTime loginTime;

}
