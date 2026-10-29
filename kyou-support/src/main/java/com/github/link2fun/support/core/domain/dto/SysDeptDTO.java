package com.github.link2fun.support.core.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 部门响应
 * {@link com.github.link2fun.support.core.domain.entity.SysDept }
 */
@Data
public class SysDeptDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 部门ID */
  private Long deptId;
  /** 父部门ID */
  private Long parentId;
  /** 祖级列表 */
  private String ancestors;
  /** 部门名称 */
  private String deptName;
  /** 显示顺序 */
  private Integer orderNum;
  /** 负责人 */
  private String leader;
  /** 联系电话 */
  private String phone;
  /** 邮箱 */
  private String email;
  /** 状态 */
  private String status;
  /** 删除标志 */
  private String delFlag;
  /** 子部门 */
  private List<SysDeptDTO> children = new ArrayList<>();
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
