package com.github.link2fun.support.core.domain.entity;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.support.core.domain.entity.proxy.SysDictDataProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;

/**
 * 字典数据表 sys_dict_data
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@FieldNameConstants
@Table("sys_dict_data")
@EasyAlias("dictData")
public class SysDictData extends BaseEntity implements ProxyEntityAvailable<SysDictData, SysDictDataProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 字典编码 */
  @Excel(name = "字典编码", cellType = Excel.ColumnType.NUMERIC)
  @Column(value = "dict_code",primaryKey = true)
  private Long dictCode;

  /** 字典排序 */
  @Excel(name = "字典排序", cellType = Excel.ColumnType.NUMERIC)
  @NotNull(message = "字典排序不能为空")
  @Column("dict_sort")
  private Long dictSort;

  /** 字典标签 */
  @Excel(name = "字典标签")
  @NotBlank(message = "字典标签不能为空")
  @Length(max = 100, message = "字典标签长度不能超过100个字符")
  private String dictLabel;

  /** 字典键值 */
  @Excel(name = "字典键值")
  @NotBlank(message = "字典键值不能为空")
  @Length(max = 100, message = "字典键值长度不能超过100个字符")
  private String dictValue;

  /** 字典类型 */
  @Excel(name = "字典类型")
  @NotBlank(message = "字典类型不能为空")
  @Length(max = 100, message = "字典类型长度不能超过100个字符")
  private String dictType;

  /** 样式属性（其他样式扩展） */
  @Length(max = 100, message = "样式属性长度不能超过100个字符")
  private String cssClass;

  /** 表格字典样式 */
  private String listClass;

  /** 是否默认（Y是 N否） */
  @Excel(name = "是否默认", readConverterExp = "Y=是,N=否")
  private String isDefault;

  /** 状态（0正常 1停用） */
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  private String status;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

}
