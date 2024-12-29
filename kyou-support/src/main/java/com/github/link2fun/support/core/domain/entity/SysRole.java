package com.github.link2fun.support.core.domain.entity;

import com.easy.query.core.annotation.*;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.github.link2fun.support.annotation.Excel;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleProxy;
import com.github.link2fun.support.utils.uuid.IdUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.Length;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;
import java.util.List;

/**
 * 角色表 sys_role
 *
 * @author ruoyi
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table("sys_role")
@EntityProxy
@FieldNameConstants
@EasyAlias("role")
public class SysRole extends BaseEntity implements ProxyEntityAvailable<SysRole, SysRoleProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 角色ID */
  @Excel(name = "角色序号", cellType = Excel.ColumnType.STRING)
  @JsonSerialize(using = ToStringSerializer.class)
  @Column(value = "role_id", primaryKey = true, generatedKey = true)
  private Long roleId;

  /** 角色名称 */
  @Excel(name = "角色名称")
  @NotBlank(message = "角色名称不能为空")
  @Length(max = 30, message = "角色名称长度不能超过30个字符")
  @Column(value = "role_name")
  private String roleName;

  /** 角色权限 */
  @Excel(name = "角色权限")
  @NotBlank(message = "权限字符不能为空")
  @Length(max = 100, message = "权限字符长度不能超过100个字符")
  @Column(value = "role_key")
  private String roleKey;

  /** 角色排序 */
  @Excel(name = "角色排序")
  @Column(value = "role_sort")
  private Integer roleSort;

  /** 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限） */
  @Excel(name = "数据范围", readConverterExp = "1=所有数据权限,2=自定义数据权限,3=本部门数据权限,4=本部门及以下数据权限,5=仅本人数据权限")
  @Column(value = "data_scope")
  private String dataScope;

  /** 菜单树选择项是否关联显示（ 0：父子不互相关联显示 1：父子互相关联显示） */
  @Column(value = "menu_check_strictly")
  private Boolean menuCheckStrictly;

  /** 部门树选择项是否关联显示（0：父子不互相关联显示 1：父子互相关联显示 ） */
  @Column(value = "dept_check_strictly")
  private Boolean deptCheckStrictly;

  /** 角色状态（0正常 1停用） */
  @Excel(name = "角色状态", readConverterExp = "0=正常,1=停用")
  @Column(value = "status")
  private String status;

  /** 删除标志（0代表存在 2代表删除） */
  @Column(value = "del_flag")
  private String delFlag;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

  /** 角色和菜单关联关系 */
  @Navigate(value = RelationTypeEnum.ManyToMany,selfProperty = Fields.roleId, targetProperty = SysRoleMenu.Fields.roleId)
  private List<SysRoleMenu> roleMenuList;

  /** 角色关联的菜单 */
  @Navigate(value = RelationTypeEnum.ManyToMany,
    mappingClass = SysRoleMenu.class,
    selfProperty = Fields.roleId, selfMappingProperty = SysRoleMenu.Fields.roleId,
    targetProperty = SysMenu.Fields.menuId, targetMappingProperty = SysRoleMenu.Fields.menuId
  )
  private List<SysMenu> menuList;


  /** 角色和部门关联关系 */
  @Navigate(value = RelationTypeEnum.ManyToMany,selfProperty = Fields.roleId, targetProperty = SysRoleDept.Fields.roleId)
  private List<SysRoleDept> roleDeptList;

  /** 角色关联的部门 */
  @Navigate(value = RelationTypeEnum.ManyToMany,
    mappingClass = SysRoleDept.class,
    selfProperty = Fields.roleId, selfMappingProperty = SysRoleDept.Fields.roleId,
    targetProperty = SysDept.Fields.deptId, targetMappingProperty = SysRoleDept.Fields.deptId
  )
  private List<SysDept> deptList;



  public static SysRole ofRoleId(final Long roleId) {
    final SysRole role = new SysRole();
    role.setRoleId(roleId);
    return role;
  }

  public boolean isAdmin() {
    return isAdmin(this.roleId);
  }

  /**
   * 判断是否为管理员角色
   * 
   * @param roleId 角色ID
   * @return 如果是管理员角色返回true,否则返回false
   */
  public static boolean isAdmin(Long roleId) {
    return IdUtils.isIdValid(roleId) && 1L == roleId;
  }

  /**
   * 将当前角色实体转换为数据传输对象
   * 
   * @return RoleDTO 角色数据传输对象
   */
  public RoleDTO toRoleDTO() {
    RoleDTO dto = new RoleDTO();
    dto.setRoleId(this.roleId); // 角色ID
    dto.setRoleKey(this.roleKey); // 角色权限字符串
    dto.setRoleName(this.roleName); // 角色名称
    dto.setRoleSort(this.roleSort); // 显示顺序
    dto.setDataScope(this.dataScope); // 数据范围
    dto.setStatus(this.status); // 角色状态
    return dto;
  }

}
