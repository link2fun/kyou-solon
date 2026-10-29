package com.github.link2fun.support.core.domain.dto;

import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型响应
 * {@link com.github.link2fun.support.core.domain.entity.SysDictType }
 */
@Data
public class SysDictTypeDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 字典主键 */
  @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
  private Long dictId;
  /** 字典名称 */
  @Excel(name = "字典名称")
  private String dictName;
  /** 字典类型 */
  @Excel(name = "字典类型")
  private String dictType;
  /** 状态 */
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;
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
}
