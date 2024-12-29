package com.github.link2fun.support.core.domain.entity;

import com.easy.query.core.annotation.Table;
import com.easy.query.core.annotation.*;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.support.core.domain.BaseEntity;


import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Email;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.NotNull;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;


/**
 * 部门表 sys_dept
 *
 * @author ruoyi
 */
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Table("sys_dept")
@EntityProxy
@EasyAlias("deptProxy")
public class SysDept extends BaseEntity implements ProxyEntityAvailable<SysDept, SysDeptProxy> {

  /** 部门表名 */
  public static final String TABLE_NAME = "sys_dept";
  /** 部门表别名 */
  public static final String TABLE_ALIAS = "dept";

  @Serial
  private static final long serialVersionUID = 1L;

  /** 部门ID */
  @Column(primaryKey = true, value = "dept_id", generatedKey = true)
  private Long deptId;

  /** 父部门ID */
  @Column(value = "parent_id")
  private Long parentId;

  /** 祖级列表 */
  @Column(value = "ancestors")
  private String ancestors;

  /** 部门名称 */
  @NotBlank(message = "部门名称不能为空")
  @Length(max = 30, message = "部门名称长度不能超过30个字符")
  @Column(value = "dept_name")
  private String deptName;

  /** 显示顺序 */
  @NotNull(message = "显示顺序不能为空")
  @Column(value = "order_num")
  private Integer orderNum;

  /** 负责人 */
  @Column(value = "leader")
  private String leader;

  /** 联系电话 */
  @Length(max = 11, message = "联系电话长度不能超过11个字符")
  @Column(value = "phone")
  private String phone;

  /** 邮箱 */
  @Email(message = "邮箱格式不正确")
  @Length(max = 50, message = "邮箱长度不能超过50个字符")
  @Column(value = "email")
  private String email;

  /** 部门状态:0正常,1停用 */
  @Column(value = "status")
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  @Column(value = "del_flag")
  private String delFlag;

  /** 父部门名称 */
  @ColumnIgnore
  private String parentName;

  /** 子部门 */
  @ColumnIgnore
  private List<SysDept> children = new ArrayList<>();


}
