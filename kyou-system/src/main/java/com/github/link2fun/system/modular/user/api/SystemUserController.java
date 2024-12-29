package com.github.link2fun.system.modular.user.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/user")
public class SystemUserController extends BaseController {
  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemDeptService deptService;

  @Inject
  private ISystemPostService postService;

  /**
   * 获取用户列表
   */
  @SaCheckPermission("system:user:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysUser searchReq) {
    final ActionContext context = ActionContext.current();

    Page<SysUserDTO> list = userService.selectUserList(context, Page.ofCurrentContext(), searchReq, SysUserDTO.class);
    return getDataTable(list);
  }

  @Log(title = "用户管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:user:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysUser user) {
    Page<SysUserDTO> pageData = userService.selectUserList(ActionContext.current(), Page.ofLimit(Long.MAX_VALUE), user, SysUserDTO.class);
    List<SysUserDTO> dataList = pageData.getRecords();


    ExcelUtil<SysUserDTO> util = new ExcelUtil<>(SysUserDTO.class);

    util.exportExcel(response, dataList, "用户数据");
  }

  @Log(title = "用户管理", businessType = BusinessType.IMPORT)
  @SaCheckPermission("system:user:import")
  @Mapping(value = "/importData", method = MethodType.POST)
  public AjaxResult importData(UploadedFile file, boolean updateSupport) {
    final ActionContext context = ActionContext.current();
    ExcelUtil<SysUserDTO> util = new ExcelUtil<>(SysUserDTO.class);
    List<SysUserDTO> userList = util.importExcel(file.getContent());
    String operName = getUsername();
    String message = userService.importUser(context, userList, updateSupport, operName);
    return success(message);
  }

  @Mapping(value = "/importTemplate", method = MethodType.POST)
  public void importTemplate(Context response) {
    ExcelUtil<SysUser> util = new ExcelUtil<>(SysUser.class);
    util.importTemplateExcel(response, "用户数据");
  }

  /**
   * 根据用户编号获取详细信息
   */
  @SaCheckPermission("system:user:query")
  @Mapping(value = "/{userId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path(value = "userId") Long userId) {
    ActionContext context = ActionContext.current();
    userService.checkUserDataScope(context, userId);
    AjaxResult ajax = AjaxResult.success();
    List<SysRole> roles = roleService.selectRoleAll(context);
    ajax.put("roles",
      SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    ajax.put("posts", postService.selectPostAll());
    if (StringUtils.isNotNull(userId)) {
      SysUserDTO sysUser = userService.selectUserById(userId);
      ajax.put(AjaxResult.DATA_TAG, sysUser);
      ajax.put("postIds", postService.selectPostListByUserId(userId)
        .stream().map(Convert::toStr).collect(Collectors.toList()));
      ajax.put("roleIds", sysUser.getRoles().stream().map(RoleDTO::getRoleId)
        .map(Convert::toStr)
        .collect(Collectors.toList()));
    }
    return ajax;
  }

  /**
   * 新增用户
   */
  @SaCheckPermission("system:user:add")
  @Log(title = "用户管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysUserReq.AddReq addReq) {
    addReq.setCreateBy(getUsername());
    Long userId = userService.insertUser(addReq);
    return AjaxResult.success(userId);
  }

  /**
   * 修改用户
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysUserReq.UpdateReq user) {
    final ActionContext context = ActionContext.current();
    userService.checkUserAllowed(user.getUserId());
    userService.checkUserDataScope(context, user.getUserId());


    if (!userService.isColumnValueUnique(SysUserProxy.TABLE.userName(), user.getUserName(), user.getUserId())) {
      return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
    }

    if (!userService.isColumnValueUnique(SysUserProxy.TABLE.phonenumber(), user.getPhonenumber(), user.getUserId())) {
      return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
    }

    if (!userService.isColumnValueUnique(SysUserProxy.TABLE.email(), user.getEmail(), user.getUserId())) {
      return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
    }
    user.setUpdateBy(getUsername());
    return toAjax(userService.updateUser(user));
  }

  /**
   * 删除用户
   */
  @SaCheckPermission("system:user:remove")
  @Log(title = "用户管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{userIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> userIds) {
    if (CollectionUtil.contains(userIds, getUserId())) {
      return error("当前用户不能删除");
    }
    ActionContext context = ActionContext.current();
    return toAjax(userService.deleteUserByIds(context, userIds));
  }

  /**
   * 重置密码
   */
  @SaCheckPermission("system:user:resetPwd")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/resetPwd", method = MethodType.PUT)
  public AjaxResult resetPwd(@Body SysUser user) {
    ActionContext context = ActionContext.current();
    userService.checkUserAllowed(user.getUserId());
    userService.checkUserDataScope(context, user.getUserId());
    user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
    user.setUpdateBy(getUsername());
    return toAjax(userService.resetPwd(user));
  }

  /**
   * 状态修改
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public AjaxResult changeStatus(@Body SysUser user) {
    ActionContext context = ActionContext.current();
    userService.checkUserAllowed(user.getUserId());
    userService.checkUserDataScope(context, user.getUserId());
    user.setUpdateBy(getUsername());
    return toAjax(userService.updateUserStatus(user));
  }

  /**
   * 根据用户编号获取授权角色
   */
  @SaCheckPermission("system:user:query")
  @Mapping(value = "/authRole/{userId}", method = MethodType.GET)
  public AjaxResult authRole(@Path("userId") Long userId) {
    AjaxResult ajax = AjaxResult.success();
    SysUserDTO user = userService.selectUserById(userId);
    List<RoleDTO> roles = roleService.selectRolesByUserId(userId);
    ajax.put("user", user);
    ajax.put("roles",
      SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
    return ajax;
  }

  /**
   * 用户授权角色
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authRole", method = MethodType.PUT)
  public AjaxResult insertAuthRole(Long userId, List<Long> roleIds) {
    ActionContext context = ActionContext.current();
    userService.checkUserDataScope(context, userId);
    userService.insertUserAuth(userId, roleIds);
    return success();
  }

  /**
   * 获取部门树列表
   */
  @SaCheckPermission("system:user:list")
  @Mapping(value = "/deptTree", method = MethodType.GET)
  public AjaxResult deptTree(SysDept dept) {

    return success(deptService.selectDeptTreeList(ActionContext.current(), dept));
  }
}
