package com.github.link2fun.support.core.domain.dto;

import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.enums.RelationTypeEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.framework.tool.DataScopeTool;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.annotation.Excels;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@FieldNameConstants
@NoArgsConstructor
@AllArgsConstructor
public class SysUserDTO implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;

  @Excel(name = "用户序号", cellType = Excel.ColumnType.NUMERIC, prompt = "用户编号")
  @JsonSerialize(using = ToStringSerializer.class)
  private Long userId;

  /** 部门ID */
  @Excel(name = "部门编号", type = Excel.Type.IMPORT)
  @JsonSerialize(using = ToStringSerializer.class)
  private Long deptId;

  public String getDeptName(){
    if(dept==null){
      return null;
    }
    return dept.getDeptName();
  }

  /** 用户账号 */
  @Excel(name = "用户账号")
  private String userName;

  /** 用户昵称 */
  @Excel(name = "用户昵称")
  private String nickName;

  /** 用户邮箱 */
  @Excel(name = "用户邮箱")
  private String email;

  /** 手机号码 */
  @Excel(name = "手机号码")
  private String phonenumber;

  /** 用户性别 */
  @Excel(name = "用户性别", readConverterExp = "0=男,1=女,2=未知")
  private String sex;

  /** 用户头像 */
  private String avatar;

  /** 密码 */
  @JsonIgnore
  private String password;

  /** 帐号状态（0正常 1停用） */
  @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;

  /** 最后登录IP */
  @Excel(name = "最后登录IP", type = Excel.Type.EXPORT)
  private String loginIp;

  /** 最后登录时间 */
  @Excel(name = "最后登录时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss", type = Excel.Type.EXPORT)
  private Date loginDate;

  private String createBy;
  private Date createTime;
  private String updateBy;
  private Date updateTime;
  private String remark;


  /** 部门对象 */
  @Excels({
    @Excel(name = "部门名称", targetAttr = "deptName", type = Excel.Type.EXPORT),
    @Excel(name = "部门负责人", targetAttr = "leader", type = Excel.Type.EXPORT)
  })
  @Navigate(value= RelationTypeEnum.OneToOne)
  private DeptDTO dept;

  /** 角色对象 */
  @Navigate(value = RelationTypeEnum.ManyToMany,
    mappingClass = SysUserRole.class,
    selfProperty = SysUser.Fields.userId,
    targetProperty = SysRole.Fields.roleId,
    selfMappingProperty = SysUser.Fields.userId,
    targetMappingProperty = SysUserRole.Fields.roleId
  )
  private List<RoleDTO> roles;


  public static SysUserDTO ofUserId(final Long userId) {
    SysUserDTO user = new SysUserDTO();
    user.setUserId(userId);
    return user;
  }

  /** 是否是超级管理员, 超级管理员用户 ID 为 1 */
  public boolean isAdmin() {
    return DataScopeTool.isAdmin(this.userId);
  }

}
