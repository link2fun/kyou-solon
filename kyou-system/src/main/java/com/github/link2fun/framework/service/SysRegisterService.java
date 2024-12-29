package com.github.link2fun.framework.service;


import com.github.link2fun.framework.manager.AsyncManager;
import com.github.link2fun.framework.manager.factory.AsyncFactory;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.domain.model.RegisterBody;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.exception.user.CaptchaException;
import com.github.link2fun.support.exception.user.CaptchaExpireException;
import com.github.link2fun.support.utils.MessageUtils;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.system.tool.SystemConfigContext;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

/**
 * 注册校验方法
 *
 * @author ruoyi
 */
@Component
public class SysRegisterService {
  @Inject
  private ISystemUserService userService;


  @Inject
  private RedisCache redisCache;

  /**
   * 注册
   */
  public String register(RegisterBody registerBody) {
    String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
    SysUser sysUser = new SysUser();
    sysUser.setUserName(username);

    // 验证码开关
    boolean captchaEnabled = SystemConfigContext.accountCaptchaEnabled();
    if (captchaEnabled) {
      validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
    }

    if (StringUtils.isEmpty(username)) {
      msg = "用户名不能为空";
    } else if (StringUtils.isEmpty(password)) {
      msg = "用户密码不能为空";
    } else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
      || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
      msg = "账户长度必须在2到20个字符之间";
    } else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
      || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
      msg = "密码长度必须在5到20个字符之间";
    } else if (!userService.isColumnValueUnique(SysUserProxy.TABLE.userName(),sysUser.getUserName(), sysUser.getUserId() )) {
      msg = "保存用户'" + username + "'失败，注册账号已存在";
    } else {
      sysUser.setNickName(username);
      sysUser.setPassword(SecurityUtils.encryptPassword(password));
      boolean regFlag = userService.registerUser(sysUser);
      if (!regFlag) {
        msg = "注册失败,请联系系统管理人员";
      } else {
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.getMessage("user.register.success")));
      }
    }
    return msg;
  }

  /**
   * 校验验证码
   *
   * @param username 用户名
   * @param code     验证码
   * @param uuid     唯一标识
   * @return 结果
   */
  public void validateCaptcha(String username, String code, String uuid) {
    String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
    String captcha = redisCache.get(verifyKey);
    redisCache.del(verifyKey);
    if (captcha == null) {
      throw new CaptchaExpireException();
    }
    if (!code.equalsIgnoreCase(captcha)) {
      throw new CaptchaException();
    }
  }
}
