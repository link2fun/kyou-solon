package com.github.link2fun.support.core.domain.entity;



import com.easy.query.core.annotation.*;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserRoleProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

/**
 * 用户和角色关联 sys_user_role
 *
 * @author ruoyi
 */
@FieldNameConstants
@Data
@EntityProxy
@Table("sys_user_role")
@EasyAlias("userRole")
public class SysUserRole implements ProxyEntityAvailable<SysUserRole, SysUserRoleProxy> {
  /** 用户ID */
  @Column(value = "user_id",primaryKey = true)
  private Long userId;

  /** 角色ID */
  @Column(value = "role_id",primaryKey = true)
  private Long roleId;


}
