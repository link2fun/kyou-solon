package com.github.link2fun.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.PasswordUtils;
import com.github.link2fun.support.utils.file.FileUploadUtils;
import com.github.link2fun.support.utils.file.MimeTypeUtils;
import com.github.link2fun.system.modular.user.model.req.SysUserProfileModifyReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.web.model.vo.AvatarResponse;
import com.github.link2fun.web.model.vo.UserProfileResponse;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Validated;

/**
 * 个人信息 业务处理
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/user/profile")
public class SystemUserProfileController {


  @Inject
  private ISystemUserService userService;


  @Inject
  private KyouProperties ruoYiProperties;

  /**
   * 个人信息
   */
  @SaCheckLogin
  @Mapping(method = MethodType.GET)
  public UserProfileResponse profile() {
    SessionUser currentUser = ActionContext.current().getCurrentUserNotNull();
    SysUserDTO user = currentUser.getUser();
    UserProfileResponse response = new UserProfileResponse();
    response.setData(user);
    response.setRoleGroup(userService.selectUserRoleGroup(currentUser.getUsername()));
    response.setPostGroup(userService.selectUserPostGroup(currentUser.getUsername()));
    return response;
  }

  /**
   * 修改用户
   */
  @SaCheckLogin
  @Log(title = "个人信息", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> updateProfile(@Validated @Body SysUserProfileModifyReq modifyReq) {
    final Long userId = ActionContext.current().getUserId();
    return Resp.ofAffected(userService.updateMyProfile(userId, modifyReq));
  }

  /**
   * 修改密码
   */
  @SaCheckLogin
  @Log(title = "个人信息", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(value = "/updatePwd", method = MethodType.PUT)
  public Resp<Void> updatePwd(String oldPassword, String newPassword) {
    SessionUser currentUser = ActionContext.current().getCurrentUserNotNull();
    String userName = currentUser.getUsername();
    final Long userId = currentUser.getUserId();
    final SysUser user = userService.getByIdNotNull(userId);
    final String password = user.getPassword();
    if (!PasswordUtils.matchesPassword(oldPassword, password)) {
      return Resp.error("修改密码失败，旧密码错误");
    }
    if (PasswordUtils.matchesPassword(newPassword, password)) {
      return Resp.error("新密码不能与旧密码相同");
    }
    newPassword = PasswordUtils.encryptPassword(newPassword);
    if (userService.resetUserPwd(userName, newPassword)) {
      return Resp.success();
    }
    return Resp.error("修改密码异常，请联系管理员");
  }

  /**
   * 头像上传
   */
  @SaCheckLogin
  @Log(title = "用户头像", businessType = BusinessType.UPDATE)
  @Mapping(value = "/avatar", method = MethodType.POST)
  public Resp<Void> avatar(@Param("avatarfile") UploadedFile file) throws Exception {
    if (!file.isEmpty()) {
      SessionUser currentUser = ActionContext.current().getCurrentUserNotNull();
      String avatar = FileUploadUtils.upload(ruoYiProperties.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
      if (userService.updateMyAvatar(currentUser.getUserId(), currentUser.getUsername(), avatar)) {
        AvatarResponse response = new AvatarResponse();
        response.setImgUrl(avatar);
        return response;
      }
    }
    return Resp.error("上传图片异常，请联系管理员");
  }
}
