package com.github.link2fun.support.core.domain.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserGroupProxy;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户和群组关联 sys_user_group
 */
@Data
@EntityProxy
@Table("sys_user_group")
public class SysUserGroup implements Serializable, ProxyEntityAvailable<SysUserGroup, SysUserGroupProxy> {

  /** 用户ID */
  @Column(value = "user_id", primaryKey = true)
  private Long userId;

  /** 用户组ID */
  @Column(value = "group_id", primaryKey = true)
  private Long groupId;
}
