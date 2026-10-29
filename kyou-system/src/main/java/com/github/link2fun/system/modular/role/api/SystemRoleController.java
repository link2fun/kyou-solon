package com.github.link2fun.system.modular.role.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.model.dto.SysRoleDetailDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRoleExcelDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRoleListDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRolePageDTO;
import com.github.link2fun.system.modular.role.model.req.*;
import com.github.link2fun.system.modular.role.model.vo.RoleDeptTreeResponse;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.req.SysUserSearchReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

/**
 * 角色信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/role")
public class SystemRoleController {
  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemDeptService deptService;


  @SaCheckPermission("system:role:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysRoleSearchReq searchReq) {
    ActionContext context = ActionContext.current();
    Page<SysRolePageDTO> page = roleService.selectRoleList(context, Page.ofCurrentContext(), searchReq, SysRolePageDTO.class);
    return TableDataInfo.of(page);
  }

  @Log(title = "角色管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:role:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysRoleSearchReq searchReq) {
    ActionContext context = ActionContext.current();
    Page<SysRoleExcelDTO> list = roleService.selectRoleList(context, Page.ofAll(), searchReq, SysRoleExcelDTO.class);
    ExcelUtil<SysRoleExcelDTO> util = new ExcelUtil<>(SysRoleExcelDTO.class);
    util.exportExcel(response, list.getRecords(), "角色数据");
  }

  /**
   * 根据角色编号获取详细信息
   */
  @SaCheckPermission("system:role:query")
  @Mapping(value = "/{roleId}", method = MethodType.GET)
  public Resp<SysRoleDetailDTO> getInfo(@Path Long roleId) {
    SysRoleDetailDTO role = roleService.selectRoleDetail(ActionContext.current(), roleId, SysRoleDetailDTO.class);
    return Resp.successData(role);
  }

  /**
   * 新增角色
   */
  @SaCheckPermission("system:role:add")
  @Log(title = "角色管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysRoleAddReq addReq) {
    return Resp.ofAffected(roleService.insertRole(addReq));

  }

  /**
   * 修改保存角色
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysRoleModifyReq modifyReq) {
    return Resp.ofAffected(roleService.updateRole(ActionContext.current(), modifyReq));
  }

  /**
   * 保存角色数据权限
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/dataScope", method = MethodType.PUT)
  public Resp<Void> dataScope(@Validated @Body SysRoleChangeDataScopeReq changeDataScopeReq) {
    return Resp.ofAffected(roleService.authDataScope(ActionContext.current(), changeDataScopeReq));
  }

  /**
   * 状态修改
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public Resp<Void> changeStatus(@Validated @Body SysRoleChangeStatusReq changeStatusReq) {
    return Resp.ofAffected(roleService.changeRoleStatus(ActionContext.current(), changeStatusReq));
  }

  /**
   * 删除角色
   */
  @SaCheckPermission("system:role:remove")
  @Log(title = "角色管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{roleIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> roleIds) {
    return Resp.ofAffected(roleService.deleteRoleByIds(ActionContext.current(), roleIds));
  }

  /**
   * 获取角色选择框列表
   */
  @SaCheckPermission("system:role:query")
  @Mapping(value = "/optionselect", method = MethodType.GET)
  public Resp<List<SysRoleListDTO>> optionselect() {
    ActionContext context = ActionContext.current();
    return Resp.successData(roleService.selectRoleAll(context, SysRoleListDTO.class));
  }

  /**
   * 查询已分配用户角色列表
   */
  @SaCheckPermission("system:role:list")
  @Mapping(value = "/authUser/allocatedList", method = MethodType.GET)
  public TableDataInfo allocatedList(SysUserSearchReq user) {
    final ActionContext context = ActionContext.current();

    Page<AllocatedUserDTO> list = userService.selectAllocatedList(context, Page.ofCurrentContext(), user);
    return TableDataInfo.of(list);
  }

  /**
   * 查询未分配用户角色列表
   */
  @SaCheckPermission("system:role:list")
  @Mapping(value = "/authUser/unallocatedList", method = MethodType.GET)
  public TableDataInfo unallocatedList(SysUserSearchReq user) {

    final ActionContext context = ActionContext.current();
    Page<AllocatedUserDTO> list = userService.selectUnallocatedList(context, Page.ofCurrentContext(), user);
    return TableDataInfo.of(list);
  }

  /**
   * 取消授权用户
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/cancel", method = MethodType.PUT)
  public Resp<Void> cancelAuthUser(@Body SysUserRole userRole) {
    roleService.unassignUser(userRole.getRoleId(), userRole.getUserId());
    return Resp.success();
  }

  /**
   * 批量取消授权用户
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/cancelAll", method = MethodType.PUT)
  public Resp<Void> cancelAuthUserAll(Long roleId, List<Long> userIds) {
    roleService.unassignUsers(roleId, userIds);
    return Resp.success();
  }

  /**
   * 批量选择用户授权
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/selectAll", method = MethodType.PUT)
  public Resp<Void> selectAuthUserAll(Long roleId, List<Long> userIds) {
    roleService.assignUsers(ActionContext.current(), roleId, userIds);
    return Resp.success();
  }

  /**
   * 获取对应角色部门树列表
   */
  @SaCheckPermission("system:role:query")
  @Mapping(value = "/deptTree/{roleId}", method = MethodType.GET)
  public RoleDeptTreeResponse deptTree(@Path("roleId") Long roleId) {
    final ActionContext context = ActionContext.current();
    RoleDeptTreeResponse response = new RoleDeptTreeResponse();
    response.setCheckedKeys(deptService.selectDeptListByRoleId(roleId));
    response.setDepts(deptService.selectDeptTreeList(context, new SysDeptSearchReq()));
    return response;
  }
}
