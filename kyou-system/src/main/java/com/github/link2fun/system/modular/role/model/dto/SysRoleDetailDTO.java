package com.github.link2fun.system.modular.role.model.dto;


import lombok.Data;
import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.enums.RelationTypeEnum;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.github.link2fun.support.annotation.Excel;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.easy.query.core.annotation.UpdateIgnore;

/**
 * this file automatically generated by easy-query struct dto mapping
 * 当前文件是easy-query自动生成的 结构化dto 映射
 * {@link com.github.link2fun.support.core.domain.entity.SysRole }
 * @author easy-query
 */
@Data
public class SysRoleDetailDTO {


  /** 角色ID */
  @Excel(name = "角色序号", cellType = Excel.ColumnType.STRING)
  @JsonSerialize(using = ToStringSerializer.class)
  private Long roleId;
  /** 角色名称 */
  @Excel(name = "角色名称")
  private String roleName;
  /** 角色权限 */
  @Excel(name = "角色权限")
  private String roleKey;
  /** 角色排序 */
  @Excel(name = "角色排序")
  private Integer roleSort;
  /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
  @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
  private String dataScope;
  /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
  private Boolean menuCheckStrictly;
  /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
  private Boolean deptCheckStrictly;
  /** 角色状态（0正常 1停用） */
  @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
  private String status;
  /** 删除标志（0代表存在 2代表删除） */
  private String delFlag;
  /** 备注 */
  private String remark;
  /** 角色和菜单关联关系 */
  @Navigate(value = RelationTypeEnum.ManyToMany)
  private List<InternalRoleMenu> roleMenuList;
  /** 角色和部门关联关系 */
  @Navigate(value = RelationTypeEnum.ManyToMany)
  private List<InternalRoleDept> roleDeptList;
  /** 创建者 */
  @UpdateIgnore
  private String createBy;
  /** 创建时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @UpdateIgnore
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime updateTime;


  /**
   * {@link com.github.link2fun.support.core.domain.entity.SysRoleDept }
   *
   */
  @Data
  public static class InternalRoleDept {
    /** 部门ID */
    private Long deptId;


  }


  /**
   * {@link com.github.link2fun.support.core.domain.entity.SysRoleMenu }
   *
   */
  @Data
  public static class InternalRoleMenu {
    /** 菜单ID */
    private Long menuId;


  }

}
