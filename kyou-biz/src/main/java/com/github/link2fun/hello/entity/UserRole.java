package com.github.link2fun.hello.entity;

import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.hello.entity.proxy.UserRoleProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

@Data
@EntityProxy
@FieldNameConstants
@Table(value = "demo_user_role")
public class UserRole implements ProxyEntityAvailable<UserRole, UserRoleProxy> {
  /** 用户的唯一标识符 */
  private String userId;

  /** 角色的唯一标识符 */
  private String roleId;
}
