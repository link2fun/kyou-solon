package com.github.link2fun.support.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单响应
 * {@link com.github.link2fun.support.core.domain.entity.SysMenu }
 */
@Data
public class SysMenuDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 菜单ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long menuId;
  /** 菜单名称 */
  private String menuName;
  /** 父菜单ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long parentId;
  /** 显示顺序 */
  private Integer orderNum;
  /** 路由地址 */
  private String path;
  /** 组件路径 */
  private String component;
  /** 路由参数 */
  private String query;
  /** 是否外链 */
  private String isFrame;
  /** 是否缓存 */
  private String isCache;
  /** 菜单类型 */
  private String menuType;
  /** 是否显示 */
  private String visible;
  /** 状态 */
  private String status;
  /** 权限标识 */
  private String perms;
  /** 菜单图标 */
  private String icon;
  /** 备注 */
  private String remark;
  /** 子菜单 */
  private List<SysMenuDTO> children = new ArrayList<>();
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
