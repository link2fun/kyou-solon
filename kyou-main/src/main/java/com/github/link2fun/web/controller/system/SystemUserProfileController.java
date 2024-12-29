package com.github.link2fun.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.file.FileUploadUtils;
import com.github.link2fun.support.utils.file.MimeTypeUtils;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/user/profile")
public class SystemUserProfileController extends BaseController {


  @Inject
  private ISystemUserService userService;


  @Inject
  private KyouProperties ruoYiProperties;

  /**
   * 个人信息
   */
  @SaCheckLogin
  @Mapping(method = MethodType.GET)
  public AjaxResult profile() {
    SessionUser currentUser = getCurrentUser();
    SysUserDTO user = currentUser.getUser();
    AjaxResult ajax = AjaxResult.success(user);
    ajax.put("roleGroup", userService.selectUserRoleGroup(currentUser.getUsername()));
    ajax.put("postGroup", userService.selectUserPostGroup(currentUser.getUsername()));
    return ajax;
  }

  /**
   * 修改用户
   */
  @SaCheckLogin
  @Log(title = "个人信息", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult updateProfile(@Body SysUser updateUserRequest) {
    final long userId = StpUtil.getLoginIdAsLong();

    final SessionUser currentUser = SaSessionBizTool.getCurrentUser();

    final SysUser user = userService.getById(userId);


    user.setNickName(updateUserRequest.getNickName());
    user.setEmail(updateUserRequest.getEmail());
    user.setPhonenumber(updateUserRequest.getPhonenumber());
    user.setSex(updateUserRequest.getSex());

    if (!userService.isColumnValueUnique(SysUserProxy.TABLE.phonenumber(), user.getPhonenumber(), user.getUserId())) {
      return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
    }
    if (!userService.isColumnValueUnique(SysUserProxy.TABLE.email(),user.getEmail(), user.getUserId())) {

      return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
    }
    if (userService.updateUserProfile(user) > 0) {
      // 更新缓存用户信息
      final SysUserDTO sysUserDTO = userService.selectUserByUserName(user.getUserName());
      currentUser.setUser(sysUserDTO);
      SaSessionBizTool.setCurrentUser(currentUser);

      return success();
    }
    return error("修改个人信息异常，请联系管理员");
  }

  /**
   * 修改密码
   */
  @SaCheckLogin
  @Log(title = "个人信息", businessType = BusinessType.UPDATE)
  @Mapping(value = "/updatePwd", method = MethodType.PUT)
  public AjaxResult updatePwd(String oldPassword, String newPassword) {
    SessionUser currentUser = getCurrentUser();
    String userName = currentUser.getUsername();
    final Long userId = currentUser.getUserId();
    final SysUser user = userService.getById(userId);
    final String password = user.getPassword();
    if (!SecurityUtils.matchesPassword(oldPassword, password)) {
      return error("修改密码失败，旧密码错误");
    }
    if (SecurityUtils.matchesPassword(newPassword, password)) {
      return error("新密码不能与旧密码相同");
    }
    newPassword = SecurityUtils.encryptPassword(newPassword);
    if (userService.resetUserPwd(userName, newPassword)) {
      return success();
    }
    return error("修改密码异常，请联系管理员");
  }

  /**
   * 头像上传
   */
  @SaCheckLogin
  @Log(title = "用户头像", businessType = BusinessType.UPDATE)
  @Mapping(value = "/avatar", method = MethodType.POST)
  public AjaxResult avatar(@Param("avatarfile") UploadedFile file) throws Exception {
    if (!file.isEmpty()) {
      SessionUser currentUser = getCurrentUser();
      String avatar = FileUploadUtils.upload(ruoYiProperties.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
      if (userService.updateUserAvatar(currentUser.getUsername(), avatar)) {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("imgUrl", avatar);
        // 更新缓存用户头像
        currentUser.getUser().setAvatar(avatar);

        SaSessionBizTool.setCurrentUser(currentUser);
        return ajax;
      }
    }
    return error("上传图片异常，请联系管理员");
  }
}
