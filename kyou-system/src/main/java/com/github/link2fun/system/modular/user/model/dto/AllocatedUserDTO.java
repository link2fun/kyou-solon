package com.github.link2fun.system.modular.user.model.dto;

import com.easy.query.core.annotation.EntityProxy;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 已分配用户
 * {@link com.github.link2fun.support.core.domain.entity.SysUser }
 */
@Data
@EntityProxy
public class AllocatedUserDTO implements Serializable {

  /** 用户ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long userId;

  /** 部门ID */
  @JsonSerialize(using = ToStringSerializer.class)
  private Long deptId;

  @SuppressWarnings("EasyQueryFieldMissMatch")
  private String deptName;

  /** 用户名 */
  private String userName;

  /** 昵称 */
  private String nickName;

  /** 邮箱 */
  private String email;

  /** 手机号 */
  private String phonenumber;

  /** 用户状态 */
  private String status;

  /** 创建时间 */
  private LocalDateTime createTime;
}
