package com.github.link2fun.system.modular.group.model;

import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.system.modular.group.model.proxy.SysGroupProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;


/** 系统用户群组 sys_group */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@FieldNameConstants
@EasyAlias("group")
@Table(value = "sys_group")
public class SysGroup extends BaseEntity implements ProxyEntityAvailable<SysGroup, SysGroupProxy>{


  /** table_name */
  public static final String TABLE_NAME = "sys_group";
  /** table alias */
  public static final String TABLE_ALIAS = "group";

  @Serial
  private static final long serialVersionUID = 1L;


  /** 群组序号 */
  @Column(value = "group_id", primaryKey = true,generatedKey = true)
  private Long groupId;

  /** 群组名称 */
  @Column(value = "group_name")
  @NotBlank(message = "群组名称不能为空")
  @Length(max = 50, message = "群组名称长度不能超过50")
  private String groupName;


  /** 群组排序 */
  @Column(value = "group_sort")
  @NotNull(message = "群组排序不能为空")
  private Integer groupSort;


  /** 群组状态 */
  @Column(value = "status")
  private String status;


}
