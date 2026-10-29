package com.github.link2fun.support.core.domain.dto;

import com.github.link2fun.support.annotation.Excel;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典值响应
 * {@link com.github.link2fun.support.core.domain.entity.SysDictData }
 */
@Data
public class SysDictDataDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 字典编码 */
  @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
  private Long dictCode;
  /** 字典排序 */
  @Excel(name = "字典排序", cellType = Excel.ColumnType.NUMERIC)
  private Long dictSort;
  /** 字典标签 */
  @Excel(name = "字典标签")
  private String dictLabel;
  /** 字典键值 */
  @Excel(name = "字典键值")
  private String dictValue;
  /** 字典类型 */
  @Excel(name = "字典类型")
  private String dictType;
  /** 样式属性 */
  private String cssClass;
  /** 回显样式 */
  private String listClass;
  /** 是否默认 */
  @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
  private String isDefault;
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
