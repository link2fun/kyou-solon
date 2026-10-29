package com.github.link2fun.system.modular.group.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 群组响应
 * {@link com.github.link2fun.system.modular.group.model.SysGroup }
 */
@Data
public class SysGroupDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 群组ID */
  private Long groupId;
  /** 群组名称 */
  private String groupName;
  /** 显示顺序 */
  private Integer groupSort;
  /** 状态 */
  private String status;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
