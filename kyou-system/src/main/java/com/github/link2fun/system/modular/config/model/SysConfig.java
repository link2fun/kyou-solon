package com.github.link2fun.system.modular.config.model;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.system.modular.config.model.proxy.SysConfigProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;

/**
 * 参数配置表 sys_config
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("sys_config")
@FieldNameConstants
@EasyAlias("config")
@EntityProxy
public class SysConfig extends BaseEntity implements ProxyEntityAvailable<SysConfig, SysConfigProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 参数主键 */
  @Excel(name = "参数主键", cellType = Excel.ColumnType.NUMERIC)
  @JsonSerialize(using = ToStringSerializer.class)
  @Column(primaryKey = true, value = "config_id")
  private Long configId;

  /** 参数名称 */
  @Excel(name = "参数名称")
  @Column("config_name")
  private String configName;

  /** 参数键名 */
  @Excel(name = "参数键名")
  @Column("config_key")
  private String configKey;

  /** 参数键值 */
  @Excel(name = "参数键值")
  @Column("config_value")
  private String configValue;

  /** 系统内置（Y是 N否） */
  @Excel(name = "系统内置", readConverterExp = "Y=是,N=否")
  @Column("config_type")
  private String configType;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

}
