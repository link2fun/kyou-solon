package com.github.link2fun.system.modular.role.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.utils.uuid.IdUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色行响应
 * {@link com.github.link2fun.support.core.domain.entity.SysRole }
 */
@Getter
@Setter
public class SysRoleListDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 角色ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long roleId;
  /** 角色名称 */
  private String roleName;
  /** 角色权限字符串 */
  private String roleKey;
  /** 显示顺序 */
  private Integer roleSort;
  /** 数据范围 */
  private String dataScope;
  /** 菜单树选择项是否关联显示 */
  private Boolean menuCheckStrictly;
  /** 部门树选择项是否关联显示 */
  private Boolean deptCheckStrictly;
  /** 状态 */
  private String status;
  /** 删除标志 */
  private String delFlag;
  /** 备注 */
  private String remark;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;

  /** 是否是超级管理员角色, 该角色 ID 为 1 */
  public boolean isSuperAdminRole() {
    return IdUtils.isIdValid(roleId) && 1L == roleId;
  }
}
