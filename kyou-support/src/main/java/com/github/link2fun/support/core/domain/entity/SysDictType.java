package com.github.link2fun.support.core.domain.entity;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.support.core.domain.entity.proxy.SysDictTypeProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Pattern;

import java.io.Serial;

/**
 * 字典类型表 sys_dict_type
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@FieldNameConstants
@Table("sys_dict_type")
@EasyAlias("dictType")
public class SysDictType extends BaseEntity implements ProxyEntityAvailable<SysDictType, SysDictTypeProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 字典主键 */
  @Excel(name = "字典主键", cellType = Excel.ColumnType.NUMERIC)
  @Column(value = "dict_id",primaryKey = true)
  private Long dictId;

  /** 字典名称 */
  @Excel(name = "字典名称")
  @NotBlank(message = "字典名称不能为空")
  @Length(max = 100, message = "字典名称长度不能超过100个字符")
  @Column(value = "dict_name")
  private String dictName;

  /** 字典类型 */
  @Excel(name = "字典类型")
  @NotBlank(message = "字典类型不能为空")
  @Length(max = 100, message = "字典类型长度不能超过100个字符")
  @Pattern(value = "[A-Za-z0-9_]+", message = "字典类型只能包含字母、数字、下划线")
  @Column(value = "dict_type")
  private String dictType;

  /** 状态（0正常 1停用） */
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  @Column(value = "status")
  private String status;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

}
