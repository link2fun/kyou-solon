package com.github.link2fun.support.core.domain.entity;


import com.easy.query.core.annotation.*;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.support.core.domain.entity.proxy.SysPostProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;

/**
 * 岗位表 sys_post
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@EasyAlias("postProxy")
@Table("sys_post")
public class SysPost extends BaseEntity implements ProxyEntityAvailable<SysPost, SysPostProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 岗位序号 */
  @Excel(name = "岗位序号", cellType = Excel.ColumnType.NUMERIC)
  @JsonSerialize(using = ToStringSerializer.class)
  @Column(value = "post_id", primaryKey = true,generatedKey = true)
  private Long postId;

  /** 岗位编码 */
  @Excel(name = "岗位编码")
  @NotBlank(message = "岗位编码不能为空")
  @Length(max = 64, message = "岗位编码长度不能超过64个字符")
  @Column("post_code")
  private String postCode;

  /** 岗位名称 */
  @Excel(name = "岗位名称")
  @NotBlank(message = "岗位名称不能为空")
  @Length(max = 50, message = "岗位名称长度不能超过50个字符")
  @Column("post_name")
  private String postName;

  /** 岗位排序 */
  @Excel(name = "岗位排序")
  @NotNull(message = "显示顺序不能为空")
  @Column("post_sort")
  private Integer postSort;

  /** 状态（0正常 1停用） */
  @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
  @Column("status")
  private String status;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

  /** 用户是否存在此岗位标识 默认不存在 */
  @ColumnIgnore
  private boolean flag = false;

}
