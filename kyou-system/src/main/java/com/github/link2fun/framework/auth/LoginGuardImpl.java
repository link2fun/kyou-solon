package com.github.link2fun.framework.auth;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.LoginBody;
import com.github.link2fun.support.core.domain.model.RegisterBody;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.exception.user.*;
import com.github.link2fun.support.utils.DateUtils;
import com.github.link2fun.support.utils.MessageUtils;
import com.github.link2fun.support.utils.ip.IpUtils;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.system.tool.SystemConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Slf4j
@Component
public class LoginGuardImpl implements LoginGuard {

  @Inject
  private ISystemUserService userService;

  @Inject
  private RedisCache redisCache;

  @Inject
  private PasswordEncoder passwordEncoder;

  @Inject
  private AuditSink auditSink;

  @Inject
  private ClientInfoProvider clientInfoProvider;

  @Inject(value = "${user.password.maxRetryCount}", required = false)
  private int maxRetryCount = 3;

  @Inject(value = "${user.password.lockTime}", required = false)
  private int lockTime = 30;

  private Messages messages = MessageUtils::getMessage;

  private SessionLogin sessionLogin = SaSessionBizTool::login;

  private Supplier<String> currentUsername = () -> ActionContext.current().getUsername();

  private BooleanSupplier loggedIn = SaSessionBizTool::isLogin;

  private Runnable sessionLogout = SaSessionBizTool::logout;

  public LoginGuardImpl() {
  }

  LoginGuardImpl(ISystemUserService userService, RedisCache redisCache, PasswordEncoder passwordEncoder,
                 AuditSink auditSink, ClientInfoProvider clientInfoProvider,
                 int maxRetryCount, int lockTime, Messages messages, SessionLogin sessionLogin,
                 Supplier<String> currentUsername, BooleanSupplier loggedIn, Runnable sessionLogout) {
    this.userService = userService;
    this.redisCache = redisCache;
    this.passwordEncoder = passwordEncoder;
    this.auditSink = auditSink;
    this.clientInfoProvider = clientInfoProvider;
    this.maxRetryCount = maxRetryCount;
    this.lockTime = lockTime;
    this.messages = messages;
    this.sessionLogin = sessionLogin;
    this.currentUsername = currentUsername;
    this.loggedIn = loggedIn;
    this.sessionLogout = sessionLogout;
  }

  /** 登录: 验证码 → 前置检查 → 凭证校验 → 状态校验 → 建立会话, 成功后回写登录信息 */
  @Override
  public String login(LoginBody loginBody) {
    final String username = loginBody.getUsername();
    final String password = loginBody.getPassword();
    final String clientIp = clientInfoProvider.capture().getIp();

    verifyCaptcha(username, loginBody.getCode(), loginBody.getUuid());
    preCheck(username, password, clientIp);

    final SysUserDTO sysUser = userService.selectUserByUserName(username);
    if (sysUser == null) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.not.exists"));
      throw new UserPasswordNotMatchException();
    }
    verifyPassword(username, password, sysUser.getPassword());
    verifyAccountEnabled(username, sysUser);

    final SessionUser sessionUser = sessionLogin.login(sysUser);
    auditSink.loginEvent(username, Constants.LOGIN_SUCCESS, true, messages.get("user.login.success"));
    recordLoginInfo(sessionUser.getUserId(), clientIp);
    return sessionUser.getToken();
  }

  /** 注销当前会话并记录审计 */
  @Override
  public void logout() {
    if (!loggedIn.getAsBoolean()) {
      return;
    }
    String username = null;
    try {
      username = currentUsername.get();
    } catch (RuntimeException e) {
      log.warn("注销时读取账号名失败, 审计将缺少账号名: {}", e.getMessage());
    }
    sessionLogout.run();
    auditSink.loginEvent(username, Constants.LOGOUT, true, messages.get("user.logout.success"));
  }

  /** 解锁账号: 清除密码重试计数 */
  @Override
  public void unlock(String username) {
    clearPasswordRetry(username);
  }

  /** 注册: 注册开关 → 验证码 → 账号规则 → 落库, 任一步不满足即抛异常 */
  @Override
  public void register(RegisterBody registerBody) {
    final String username = registerBody.getUsername();
    final String password = registerBody.getPassword();
    if (!SystemConfigContext.accountRegisterEnabled()) {
      throw new ServiceException("当前系统没有开启注册功能！");
    }
    verifyCaptcha(username, registerBody.getCode(), registerBody.getUuid());
    if (StrUtil.isBlank(username)) {
      throw new ServiceException("用户名不能为空");
    }
    if (StrUtil.isBlank(password)) {
      throw new ServiceException("用户密码不能为空");
    }
    if (username.length() < UserConstants.USERNAME_MIN_LENGTH
      || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
      throw new ServiceException("账户长度必须在" + UserConstants.USERNAME_MIN_LENGTH
        + "到" + UserConstants.USERNAME_MAX_LENGTH + "个字符之间");
    }
    if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
      || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
      throw new ServiceException("密码长度必须在" + UserConstants.PASSWORD_MIN_LENGTH
        + "到" + UserConstants.PASSWORD_MAX_LENGTH + "个字符之间");
    }
    if (!userService.isUserNameAvailable(username)) {
      throw new ServiceException("保存用户'" + username + "'失败，注册账号已存在");
    }
    final SysUser sysUser = new SysUser();
    sysUser.setUserName(username);
    sysUser.setNickName(username);
    sysUser.setPassword(passwordEncoder.encode(password));
    if (!userService.registerUser(sysUser)) {
      throw new ServiceException("注册失败, 请联系系统管理人员");
    }
    auditSink.loginEvent(username, Constants.REGISTER, true, messages.get("user.register.success"));
  }

  /** 验证码校验: 开关开启时生效, 校验后删除缓存 */
  private void verifyCaptcha(String username, String code, String uuid) {
    if (!SystemConfigContext.accountCaptchaEnabled()) {
      return;
    }
    final String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + ObjectUtil.defaultIfNull(uuid, "");
    final String captcha = redisCache.get(verifyKey);
    redisCache.del(verifyKey);
    if (captcha == null) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.jcaptcha.expire"));
      throw new CaptchaExpireException();
    }
    if (StrUtil.isBlank(code) || !code.equalsIgnoreCase(captcha)) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.jcaptcha.error"));
      throw new CaptchaException();
    }
  }

  /** 前置检查: 账号密码非空、账号长度、IP 黑名单 */
  private void preCheck(String username, String password, String clientIp) {
    if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("not.null"));
      throw new UserNotExistsException();
    }
    if (username.length() < UserConstants.USERNAME_MIN_LENGTH
      || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.password.not.match"));
      throw new UserPasswordNotMatchException();
    }
    if (IpUtils.isMatchedIp(SystemConfigContext.loginBlackIpList(), clientIp)) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("login.blocked"));
      throw new BlackListException();
    }
  }

  /** 凭证校验: 超限即锁定, 不匹配则累加重试计数, 通过则清零 */
  private void verifyPassword(String username, String rawPassword, String encodedPassword) {
    final Integer retryCount = redisCache.getOrDefault(pwdErrCntKey(username), 0);
    if (retryCount >= maxRetryCount) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false,
        messages.get("user.password.retry.limit.exceed", maxRetryCount, lockTime));
      throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
    }
    if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
      redisCache.set(pwdErrCntKey(username), retryCount + 1, Duration.of(lockTime, ChronoUnit.MINUTES));
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.password.not.match"));
      throw new UserPasswordNotMatchException();
    }
    clearPasswordRetry(username);
  }

  /** 账号状态校验: 停用账号拒绝登录 */
  private void verifyAccountEnabled(String username, SysUserDTO sysUser) {
    if (UserConstants.USER_DISABLE.equals(sysUser.getStatus())) {
      auditSink.loginEvent(username, Constants.LOGIN_FAIL, false, messages.get("user.blocked"));
      throw new UserBlockedException();
    }
  }

  /** 回写登录 IP 与登录时间 */
  private void recordLoginInfo(Long userId, String ip) {
    final SysUser sysUser = new SysUser();
    sysUser.setUserId(userId);
    sysUser.setLoginIp(ip);
    sysUser.setLoginDate(DateUtils.getNowDate());
    userService.updateUserProfile(sysUser);
  }

  /** 密码重试计数在缓存中的键 */
  private String pwdErrCntKey(String username) {
    return CacheConstants.PWD_ERR_CNT_KEY + username;
  }

  /** 清除密码重试计数 */
  private void clearPasswordRetry(String username) {
    if (Boolean.TRUE.equals(redisCache.hasKey(pwdErrCntKey(username)))) {
      redisCache.del(pwdErrCntKey(username));
    }
  }

  /** 消息解析 */
  @FunctionalInterface
  public interface Messages {
    String get(String code, Object... args);
  }

  /** 会话建立 */
  @FunctionalInterface
  public interface SessionLogin {
    SessionUser login(SysUserDTO user);
  }
}
