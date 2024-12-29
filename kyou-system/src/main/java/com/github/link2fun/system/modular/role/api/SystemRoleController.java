package com.github.link2fun.system.modular.role.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.framework.web.service.SysPermissionService;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.role.model.dto.SysRoleDetailDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRoleExcelDTO;
import com.github.link2fun.system.modular.role.model.dto.SysRolePageDTO;
import com.github.link2fun.system.modular.role.model.req.SysRoleAddReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeDataScopeReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeStatusReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleModifyReq;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
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
public class SystemRoleController extends BaseController {
  @Inject
  private ISystemRoleService roleService;

  @Inject
  private SysPermissionService permissionService;

  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemDeptService deptService;


  @SaCheckPermission("system:role:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysRole searchReq) {
    ActionContext context = ActionContext.current();
    Page<SysRolePageDTO> page = roleService.selectRoleList(context, Page.ofCurrentRequest(), searchReq, SysRolePageDTO.class);
    return getDataTable(page);
  }

  @Log(title = "角色管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:role:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysRole searchReq) {
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
  public AjaxResult getInfo(@Path Long roleId) {
    roleService.checkRoleDataScope(roleId);
    SysRoleDetailDTO role = roleService.selectRoleById(roleId, SysRoleDetailDTO.class);
    return success(role);
  }

  /**
   * 新增角色
   */
  @SaCheckPermission("system:role:add")
  @Log(title = "角色管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysRoleAddReq addReq) {
    if (!roleService.isRoleNameUnique(addReq.getRoleName(), null)) {
      return error("新增角色'" + addReq.getRoleName() + "'失败，角色名称已存在");
    } else if (!roleService.isRoleKeyUnique(addReq.getRoleKey(), null)) {
      return error("新增角色'" + addReq.getRoleName() + "'失败，角色权限已存在");
    }
    return toAjax(roleService.insertRole(addReq));

  }

  /**
   * 修改保存角色
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysRoleModifyReq modifyReq) {
    roleService.checkRoleAllowed(modifyReq.getRoleId());
    roleService.checkRoleDataScope(modifyReq.getRoleId());
    if (!roleService.isRoleNameUnique(modifyReq.getRoleName(), modifyReq.getRoleId())) {
      return error("修改角色'" + modifyReq.getRoleName() + "'失败，角色名称已存在");
    } else if (!roleService.isRoleKeyUnique(modifyReq.getRoleKey(), modifyReq.getRoleId())) {
      return error("修改角色'" + modifyReq.getRoleName() + "'失败，角色权限已存在");
    }

    if (roleService.updateRole(modifyReq) > 0) {
      // 更新缓存用户权限
      SessionUser currentUser = getCurrentUser();
      if (StringUtils.isNotNull(currentUser.getUser()) && !currentUser.getUser().isAdmin()) {
        currentUser.setPermissions(permissionService.getMenuPermission(currentUser.getUser()));
        currentUser.setUser(userService.selectUserByUserName(currentUser.getUser().getUserName()));
        SaSessionBizTool.setCurrentUser(currentUser);

      }
      return success();
    }
    return error("修改角色'" + modifyReq.getRoleName() + "'失败，请联系管理员");
  }

  /**
   * 保存角色数据权限
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/dataScope", method = MethodType.PUT)
  public AjaxResult dataScope(@Validated @Body SysRoleChangeDataScopeReq changeDataScopeReq) {
    roleService.checkRoleAllowed(changeDataScopeReq.getRoleId());
    roleService.checkRoleDataScope(changeDataScopeReq.getRoleId());
    return toAjax(roleService.authDataScope(changeDataScopeReq));
  }

  /**
   * 状态修改
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public AjaxResult changeStatus(@Validated @Body SysRoleChangeStatusReq changeStatusReq) {
    roleService.checkRoleAllowed(changeStatusReq.getRoleId());
    roleService.checkRoleDataScope(changeStatusReq.getRoleId());
    return toAjax(roleService.changeRoleStatus(changeStatusReq));
  }

  /**
   * 删除角色
   */
  @SaCheckPermission("system:role:remove")
  @Log(title = "角色管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{roleIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> roleIds) {
    return toAjax(roleService.deleteRoleByIds(roleIds));
  }

  /**
   * 获取角色选择框列表
   */
  @SaCheckPermission("system:role:query")
  @Mapping(value = "/optionselect", method = MethodType.GET)
  public AjaxResult optionselect() {
    ActionContext context = ActionContext.current();
    return success(roleService.selectRoleAll(context));
  }

  /**
   * 查询已分配用户角色列表
   */
  @SaCheckPermission("system:role:list")
  @Mapping(value = "/authUser/allocatedList", method = MethodType.GET)
  public TableDataInfo allocatedList(SysUser user) {
    final ActionContext context = ActionContext.current();

    Page<AllocatedUserDTO> list = userService.selectAllocatedList(context, Page.ofCurrentContext(), user);
    return getDataTable(list);
  }

  /**
   * 查询未分配用户角色列表
   */
  @SaCheckPermission("system:role:list")
  @Mapping(value = "/authUser/unallocatedList", method = MethodType.GET)
  public TableDataInfo unallocatedList(SysUser user) {

    final ActionContext context = ActionContext.current();
    Page<AllocatedUserDTO> list = userService.selectUnallocatedList(context, Page.ofCurrentContext(), user);
    return getDataTable(list);
  }

  /**
   * 取消授权用户
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/cancel", method = MethodType.PUT)
  public AjaxResult cancelAuthUser(@Body SysUserRole userRole) {
    return toAjax(roleService.removeUserRoleMapping(userRole.getUserId(), userRole.getRoleId()));
  }

  /**
   * 批量取消授权用户
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/cancelAll", method = MethodType.PUT)
  public AjaxResult cancelAuthUserAll(Long roleId, List<Long> userIds) {
    return toAjax(roleService.deleteAuthUsers(roleId, userIds));
  }

  /**
   * 批量选择用户授权
   */
  @SaCheckPermission("system:role:edit")
  @Log(title = "角色管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authUser/selectAll", method = MethodType.PUT)
  public AjaxResult selectAuthUserAll(Long roleId, List<Long> userIds) {
    roleService.checkRoleDataScope(roleId);
    return toAjax(roleService.insertAuthUsers(roleId, userIds));
  }

  /**
   * 获取对应角色部门树列表
   */
  @SaCheckPermission("system:role:query")
  @Mapping(value = "/deptTree/{roleId}", method = MethodType.GET)
  public AjaxResult deptTree(@Path("roleId") Long roleId) {
    final ActionContext context = ActionContext.current();
    AjaxResult ajax = AjaxResult.success();
    ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
    ajax.put("depts", deptService.selectDeptTreeList(context, new SysDept()));
    return ajax;
  }
}
