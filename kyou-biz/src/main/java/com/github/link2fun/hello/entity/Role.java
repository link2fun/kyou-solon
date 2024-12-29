package com.github.link2fun.hello.entity;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.hello.entity.proxy.RoleProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@FieldNameConstants
@EntityProxy
@Table(value = "demo_role")
public class Role implements ProxyEntityAvailable<Role, RoleProxy> {

  // 角色的唯一标识符
  @Column(primaryKey = true)
  private String id;

  // 角色的代码
  private String roleCode;

  // 角色的名称
  private String roleName;

}
