package com.github.link2fun.support.core.domain.entity;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleDeptProxy;
import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 角色和部门关联 sys_role_dept
 *
 * @author ruoyi
 */
@Data
@FieldNameConstants
@EntityProxy
@EasyAlias("roleDept")
@Table("sys_role_dept")
public class SysRoleDept implements Serializable, ProxyEntityAvailable<SysRoleDept, SysRoleDeptProxy> {

  /** 角色ID */
  @Column(value = "role_id", primaryKey = true)
  private Long roleId;

  /** 部门ID */
  @Column(value = "dept_id", primaryKey = true)
  private Long deptId;


}
