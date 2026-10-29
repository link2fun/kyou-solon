package com.github.link2fun.support.core.domain.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 岗位响应
 * {@link com.github.link2fun.support.core.domain.entity.SysPost }
 */
@Data
public class SysPostDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 岗位ID */
  @JsonSerialize(using = ToStringSerializer.class)
  @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
  private Long postId;
  /** 岗位编码 */
  @Excel(name = "岗位编码")
  private String postCode;
  /** 岗位名称 */
  @Excel(name = "岗位名称")
  private String postName;
  /** 岗位排序 */
  @Excel(name = "岗位排序")
  private Integer postSort;
  /** 状态 */
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;
  /** 备注 */
  private String remark;
  /** 是否已分配给用户 */
  private boolean flag = false;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
