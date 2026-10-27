package com.github.link2fun.system.modular.menu.model.req;

import lombok.Data;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;
import org.noear.solon.validation.annotation.Pattern;

/**
 * {@link com.github.link2fun.support.core.domain.entity.SysMenu}
 * 修改菜单请求
 */
@Data
public class SysMenuModifyReq {

  /** 菜单ID */
  @NotNull(message = "菜单ID不能为空")
  private Long menuId;

  /** 菜单名称 */
  @NotBlank(message = "菜单名称不能为空")
  @Length(max = 50, message = "菜单名称长度不能超过50个字符")
  private String menuName;

  /** 父菜单ID */
  private Long parentId;

  /** 显示顺序 */
  @NotNull(message = "显示顺序不能为空")
  private Integer orderNum;

  /** 路由地址 */
  @Length(max = 200, message = "路由地址不能超过200个字符")
  private String path;

  /** 组件路径 */
  @Length(max = 200, message = "组件路径不能超过255个字符")
  private String component;

  /** 路由参数 */
  private String query;

  /** 是否为外链（0是 1否） */
  private String isFrame;

  /** 是否缓存（0缓存 1不缓存） */
  private String isCache;

  /** 菜单类型（M目录 C菜单 F按钮） */
  @NotBlank(message = "菜单类型不能为空")
  private String menuType;

  /** 菜单状态（0显示 1隐藏） */
  private String visible;

  /** 菜单状态（0正常 1停用） */
  private String status;

  /** 权限字符串 */
  @Length(max = 100, message = "权限字符串长度不能超过100个字符")
  @Pattern(value = "^[^,]*$", message = "权限字符串不能包含逗号")
  private String perms;

  /** 菜单图标 */
  private String icon;

  /** 备注 */
  private String remark;

}
