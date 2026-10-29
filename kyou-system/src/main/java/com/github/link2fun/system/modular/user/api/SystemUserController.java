package com.github.link2fun.system.modular.user.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.github.link2fun.framework.auth.DataScopeRowGuard;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysPostDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dept.model.req.SysDeptSearchReq;
import com.github.link2fun.system.modular.dept.service.ISystemDeptService;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.req.SysUserChangeStatusReq;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import com.github.link2fun.system.modular.user.model.req.SysUserResetPwdReq;
import com.github.link2fun.system.modular.user.model.req.SysUserSearchReq;
import com.github.link2fun.system.modular.user.model.vo.UserAuthRoleResponse;
import com.github.link2fun.system.modular.user.model.vo.UserDetailResponse;
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
public class SystemUserController {
  @Inject
  private ISystemUserService userService;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemDeptService deptService;

  @Inject
  private DataScopeRowGuard dataScopeRowGuard;

  @Inject
  private ISystemPostService postService;

  /**
   * 获取用户列表
   */
  @SaCheckPermission("system:user:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysUserSearchReq searchReq) {
    final ActionContext context = ActionContext.current();

    Page<SysUserDTO> list = userService.selectUserList(context, Page.ofCurrentContext(), searchReq, SysUserDTO.class);
    return TableDataInfo.of(list);
  }

  // 搜索条件可带手机号/邮箱，导出接口也要脱敏，否则查询参数会明文进操作日志
  @Log(title = "用户管理", businessType = BusinessType.EXPORT, mask = true)
  @SaCheckPermission("system:user:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysUserSearchReq user) {
    Page<SysUserDTO> pageData = userService.selectUserList(ActionContext.current(), Page.ofAll(), user, SysUserDTO.class);
    List<SysUserDTO> dataList = pageData.getRecords();


    ExcelUtil<SysUserDTO> util = new ExcelUtil<>(SysUserDTO.class);

    util.exportExcel(response, dataList, "用户数据");
  }

  @Log(title = "用户管理", businessType = BusinessType.IMPORT)
  @SaCheckPermission("system:user:import")
  @Mapping(value = "/importData", method = MethodType.POST)
  public Resp<Void> importData(UploadedFile file, boolean updateSupport) {
    final ActionContext context = ActionContext.current();
    ExcelUtil<SysUserDTO> util = new ExcelUtil<>(SysUserDTO.class);
    List<SysUserDTO> userList = util.importExcel(file.getContent());
    String operName = ActionContext.current().getUsername();
    String message = userService.importUser(context, userList, updateSupport, operName);
    return Resp.successMessage(message);
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
  public UserDetailResponse getInfo(@Path(value = "userId") Long userId) {
    ActionContext context = ActionContext.current();
    dataScopeRowGuard.assertUserVisible(context, userId);
    UserDetailResponse response = new UserDetailResponse();
    // 可分配角色已剔除超管角色(非超管用户不能持有), 拼接由 Controller 负责
    response.setRoles(roleService.selectAssignableRoles(context, userId));
    response.setPosts(postService.selectPostAll(SysPostDTO.class));
    if (ObjectUtil.isNotNull(userId)) {
      SysUserDTO sysUser = userService.selectUserById(userId);
      response.setData(sysUser);
      response.setPostIds(postService.selectPostListByUserId(userId)
        .stream().map(Convert::toStr).collect(Collectors.toList()));
      // 编辑回显须含停用角色(sysUser.getRoles() 已按启用过滤), 否则整行保存会误剥离停用角色关联
      response.setRoleIds(roleService.selectRolesByUserId(userId).stream()
        .filter(RoleDTO::getFlag)
        .map(RoleDTO::getRoleId)
        .map(Convert::toStr)
        .collect(Collectors.toList()));
    }
    return response;
  }

  /**
   * 新增用户
   */
  @SaCheckPermission("system:user:add")
  @Log(title = "用户管理", businessType = BusinessType.INSERT, mask = true)
  @Mapping(method = MethodType.POST)
  public Resp<Long> add(@Validated @Body SysUserReq.AddReq addReq) {
    Long userId = userService.insertUser(addReq);
    return Resp.successData(userId);
  }

  /**
   * 修改用户
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysUserReq.UpdateReq user) {
    return Resp.ofAffected(userService.updateUser(ActionContext.current(), user));
  }

  /**
   * 删除用户
   */
  @SaCheckPermission("system:user:remove")
  @Log(title = "用户管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{userIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> userIds) {
    ActionContext context = ActionContext.current();
    return Resp.ofAffected(userService.deleteUserByIds(context, userIds));
  }

  /**
   * 重置密码
   */
  @SaCheckPermission("system:user:resetPwd")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(value = "/resetPwd", method = MethodType.PUT)
  public Resp<Void> resetPwd(@Validated @Body SysUserResetPwdReq resetPwdReq) {
    return Resp.ofAffected(userService.resetPwd(ActionContext.current(), resetPwdReq));
  }

  /**
   * 状态修改
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(value = "/changeStatus", method = MethodType.PUT)
  public Resp<Void> changeStatus(@Validated @Body SysUserChangeStatusReq changeStatusReq) {
    return Resp.ofAffected(userService.updateUserStatus(ActionContext.current(), changeStatusReq));
  }

  /**
   * 根据用户编号获取授权角色
   */
  @SaCheckPermission("system:user:query")
  @Mapping(value = "/authRole/{userId}", method = MethodType.GET)
  public UserAuthRoleResponse authRole(@Path("userId") Long userId) {
    UserAuthRoleResponse response = new UserAuthRoleResponse();
    SysUserDTO user = userService.selectUserById(userId);
    response.setUser(user);
    response.setRoles(roleService.selectAssignableRolesByUserId(userId));
    return response;
  }

  /**
   * 用户授权角色
   */
  @SaCheckPermission("system:user:edit")
  @Log(title = "用户管理", businessType = BusinessType.GRANT)
  @Mapping(value = "/authRole", method = MethodType.PUT)
  public Resp<Void> insertAuthRole(Long userId, List<Long> roleIds) {
    userService.insertUserAuth(ActionContext.current(), userId, roleIds);
    return Resp.success();
  }

  /**
   * 获取部门树列表
   */
  @SaCheckPermission("system:user:list")
  @Mapping(value = "/deptTree", method = MethodType.GET)
  public Resp<List<TreeSelect>> deptTree(SysDeptSearchReq searchReq) {
    return Resp.successData(deptService.selectDeptTreeList(ActionContext.current(), searchReq));
  }
}
