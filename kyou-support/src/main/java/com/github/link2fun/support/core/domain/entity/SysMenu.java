package com.github.link2fun.support.core.domain.entity;

import com.easy.query.core.annotation.*;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.support.core.domain.entity.proxy.SysMenuProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;


/**
 * 菜单权限表 sys_menu
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@FieldNameConstants
@EasyAlias("menu")
@Table("sys_menu")
public class SysMenu extends BaseEntity implements ProxyEntityAvailable<SysMenu, SysMenuProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 菜单ID */
  @Column(value = "menu_id",primaryKey = true,generatedKey = true)
  @JsonSerialize(using = ToStringSerializer.class)
  private Long menuId;

  /** 菜单名称 */
  @NotBlank(message = "菜单名称不能为空")
  @Length(max = 50, message = "菜单名称长度不能超过50个字符")
  @Column(value = "menu_name")
  private String menuName;

  /** 父菜单名称 */
  @ColumnIgnore
  private String parentName;

  /** 父菜单ID */
  @Column(value = "parent_id")
  @JsonSerialize(using = ToStringSerializer.class)
  private Long parentId;

  /** 显示顺序 */
  @NotNull(message = "显示顺序不能为空")
  @Column(value = "order_num")
  private Integer orderNum;

  /** 路由地址 */
  @Length(max = 200, message = "路由地址不能超过200个字符")
  @Column(value = "path")
  private String path;

  /** 组件路径 */
  @Length(max = 200, message = "组件路径不能超过255个字符")
  @Column(value = "component")
  private String component;

  /** 路由参数 */
  @Column(value = "query")
  private String query;

  /** 是否为外链（0是 1否） */
  @Column(value = "is_frame")
  private String isFrame;

  /** 是否缓存（0缓存 1不缓存） */
  @Column(value = "is_cache")
  private String isCache;

  /** 类型（M目录 C菜单 F按钮） */
  @NotBlank(message = "菜单类型不能为空")
  @Column(value = "menu_type")
  private String menuType;

  /** 显示状态（0显示 1隐藏） */
  @Column(value = "visible")
  private String visible;

  /** 菜单状态（0正常 1停用） */
  @Column(value = "status")
  private String status;

  /** 权限字符串 */
  @Length(max = 100, message = "权限字符串长度不能超过100个字符")
  @Column(value = "perms")
  private String perms;

  /** 菜单图标 */
  @Column(value = "icon")
  private String icon;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

  /** 子菜单 */
  @ColumnIgnore
  private List<SysMenu> children = new ArrayList<>();

}
