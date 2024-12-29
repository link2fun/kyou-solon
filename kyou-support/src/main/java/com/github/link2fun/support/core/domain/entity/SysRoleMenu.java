package com.github.link2fun.support.core.domain.entity;



import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleMenuProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 角色和菜单关联 sys_role_menu
 *
 * @author ruoyi
 */
@Data
@FieldNameConstants
@EntityProxy
@EasyAlias("roleMenu")
@Table("sys_role_menu")
public class SysRoleMenu implements Serializable , ProxyEntityAvailable<SysRoleMenu, SysRoleMenuProxy> {
  /** 角色ID */
  @Column(value = "role_id",primaryKey = true)
  private Long roleId;

  /** 菜单ID */
  @Column(value = "menu_id",primaryKey = true)
  private Long menuId;


}
