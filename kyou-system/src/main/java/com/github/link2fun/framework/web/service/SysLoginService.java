package com.github.link2fun.framework.web.service;


import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.framework.manager.AsyncManager;
import com.github.link2fun.framework.manager.factory.AsyncFactory;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.exception.user.*;
import com.github.link2fun.support.utils.DateUtils;
import com.github.link2fun.support.utils.MessageUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.ip.IpUtils;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.system.tool.SystemConfigContext;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

/**
 * 登录校验方法
 *
 * @author ruoyi
 */
@Component
public class SysLoginService {

  @Inject
  private RedisCache redisCache;

  @Inject
  private ISystemUserService userService;


  @Inject
  private SysPasswordService passwordService;

  /**
   * 登录验证
   *
   * @param username 用户名
   * @param password 密码
   * @param code     验证码
   * @param uuid     唯一标识
   * @return 结果
   */
  public String login(String username, String password, String code, String uuid) {
    // 验证码校验
    validateCaptcha(username, code, uuid);
    // 登录前置校验
    loginPreCheck(username, password);
    // 用户验证
    final SysUserDTO sysUser = userService.selectUserByUserName(username);
    if (sysUser == null) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("user.not.exists")));
      throw new UserPasswordNotMatchException();
    }

//    if (!passwordService.matches(username,password, sysUser.getPassword())) {
//      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("user.password.not.match")));
//      throw new UserPasswordNotMatchException();
//    }

    StpUtil.login(sysUser.getUserId());
    AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.getMessage("user.login.success")));
    final SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

//    final SaSession tokenSession = StpUtil.getTokenSession();


    SessionUser sessionUser = new SessionUser();
    sessionUser.loadTokenInfoAndTokenSession();

    sessionUser.setUserId(sysUser.getUserId());
    sessionUser.setDeptId(sysUser.getDeptId());
    sessionUser.setTokenInfo(tokenInfo);

    SaSessionBizTool.setUserAgent(sessionUser);

    final List<String> permissionList = StpUtil.getPermissionList();
    sessionUser.setPermissions(permissionList);

    sessionUser.setUser(sysUser);

    SaSessionBizTool.setCurrentUser(sessionUser);


    recordLoginInfo(sessionUser.getUserId());
    // 生成token
    return tokenInfo.getTokenValue();
  }

  /**
   * 校验验证码
   *
   * @param username 用户名
   * @param code     验证码
   * @param uuid     唯一标识
   */
  public void validateCaptcha(String username, String code, String uuid) {
    if (!SystemConfigContext.accountCaptchaEnabled()) {
      // 验证码没有启用, 无需校验
      return;
    }
    String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
    String captcha = redisCache.get(verifyKey);
    redisCache.del(verifyKey);
    if (captcha == null) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("user.jcaptcha.expire")));
      throw new CaptchaExpireException();
    }
    if (!code.equalsIgnoreCase(captcha)) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("user.jcaptcha.error")));
      throw new CaptchaException();
    }
  }

  /**
   * 登录前置校验
   *
   * @param username 用户名
   * @param password 用户密码
   */
  public void loginPreCheck(String username, String password) {
    // 用户名或密码为空 错误
    if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("not.null")));
      throw new UserNotExistsException();
    }
    // 密码如果不在指定范围内 错误

//    if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
//      || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
//      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
//      throw new UserPasswordNotMatchException();
//    }
    // 用户名不在指定范围内 错误
    if (username.length() < UserConstants.USERNAME_MIN_LENGTH
      || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("user.password.not.match")));
      throw new UserPasswordNotMatchException();
    }
    // IP黑名单校验
//    String blackStr = configService.selectConfigByKey("sys.login.blackIPList");
    String blackStr = SystemConfigContext.loginBlackIpList();
    if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
      AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.getMessage("login.blocked")));
      throw new BlackListException();
    }
  }

  /**
   * 记录登录信息
   *
   * @param userId 用户ID
   */
  public void recordLoginInfo(Long userId) {
    SysUser sysUser = new SysUser();
    sysUser.setUserId(userId);
    sysUser.setLoginIp(IpUtils.getIpAddr());
    sysUser.setLoginDate(DateUtils.getNowDate());
    userService.updateUserProfile(sysUser);
  }
}
