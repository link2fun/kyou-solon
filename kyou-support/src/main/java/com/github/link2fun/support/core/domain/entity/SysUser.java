package com.github.link2fun.support.core.domain.entity;

import com.easy.query.core.annotation.Table;
import com.easy.query.core.annotation.*;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.xss.Xss;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Email;
import org.noear.solon.validation.annotation.Length;

import java.io.Serial;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * 用户对象 sys_user
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@Table(SysUser.TABLE_NAME)
@EntityProxy
@Data
@FieldNameConstants
@EasyAlias("user")
public class SysUser extends BaseEntity implements ProxyEntityAvailable<SysUser, SysUserProxy> {

  /** 用户表 表名 */
  public static final String TABLE_NAME = "sys_user";
  /** 用户表 别名 */
  public static final String TABLE_ALIAS = "user";

  @Serial
  private static final long serialVersionUID = 1L;

  /** 用户ID */
  @Column(value = "user_id", primaryKey = true,generatedKey = true)
  private Long userId;

  /** 部门ID */
  @Column(value = "dept_id")
  private Long deptId;

  /** 用户账号 */
  @Xss(message = "用户账号不能包含脚本字符")
  @Length(max = 30, message = "用户账号长度不能超过30个字符")
  @Column(value = "user_name")
  private String userName;

  /** 用户昵称 */
  @Xss(message = "用户昵称不能包含脚本字符")
  @Length(max = 30, message = "用户昵称长度不能超过30个字符")
  @Column(value = "nick_name")
  private String nickName;

  /** 用户邮箱 */

  @Email(message = "邮箱格式不正确")
  @Length(max = 50, message = "邮箱长度不能超过50个字符")
  @Column(value = "email")
  private String email;

  /** 手机号码 */

  @Length(max = 11, message = "手机号码长度不能超过11个字符")
  @Column(value = "phonenumber")
  private String phonenumber;

  /** 用户性别 */

  @Column(value = "sex")
  private String sex;

  /** 用户头像 */
  @Column(value = "avatar")
  private String avatar;

  /** 密码 */
  @Column(value = "password")
  private String password;

  /** 帐号状态（0正常 1停用） */

  @Column(value = "status")
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  @Column(value = "del_flag")
  private String delFlag;

  /** 最后登录IP */
  @Column(value = "login_ip")
  private String loginIp;
  /** 最后登录时间 */

  @Column(value = "login_date")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private Date loginDate;

  /** 备注 */
  @Column(value = "remark")
  private String remark;


  @Navigate(value = RelationTypeEnum.OneToOne,
    targetProperty = SysDept.Fields.deptId,
    selfProperty = SysUser.Fields.deptId)
  private SysDept dept;

  @Navigate(value = RelationTypeEnum.ManyToMany,
    mappingClass = SysUserRole.class,
    selfProperty = SysUser.Fields.userId,
    targetProperty = SysRole.Fields.roleId,
    selfMappingProperty = Fields.userId,
    targetMappingProperty = SysUserRole.Fields.roleId
  )
  private List<SysRole> roles;

  @ColumnIgnore
  private Long roleId;


  public static SysUser ofUserId(final Long userId) {
    SysUser user = new SysUser();
    user.setUserId(userId);
    return user;
  }


  /** 是否是超级管理员, 超级管理员用户 ID 为 1 */
  public boolean isAdmin() {
    return isAdmin(this.userId);
  }

  /** 是否是超级管理员, 超级管理员用户 ID 为 1 */
  public static boolean isAdmin(final Long userId) {
    return Objects.nonNull(userId) && 1L == userId;
  }

}
