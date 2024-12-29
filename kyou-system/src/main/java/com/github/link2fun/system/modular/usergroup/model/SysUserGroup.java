package com.github.link2fun.system.modular.usergroup.model;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.QueryEntity;

import com.github.link2fun.system.modular.usergroup.model.proxy.SysUserGroupProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@Table(value = "sys_user_group")
public class SysUserGroup extends QueryEntity implements ProxyEntityAvailable<SysUserGroup, SysUserGroupProxy> {

  /** 用户ID */
  @Column(value = "user_id",primaryKey = true)
  private Long userId;

  /** 用户组ID */
  @Column(value = "group_id",primaryKey = true)
  private Long groupId;
}
