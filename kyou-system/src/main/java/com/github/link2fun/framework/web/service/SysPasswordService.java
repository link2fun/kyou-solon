package com.github.link2fun.framework.web.service;

import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.support.constant.CacheConstants;
import com.github.link2fun.support.context.cache.service.RedisCache;
import com.github.link2fun.support.exception.user.UserPasswordRetryLimitExceedException;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Component
public class SysPasswordService {


  @Inject
  private RedisCache redisCache;

  @Inject(value = "${user.password.maxRetryCount}", required = false)
  private int maxRetryCount = 3;

  @Inject(value = "${user.password.lockTime}", required = false)
  private int lockTime = 30;

  @Inject
  private PasswordEncoder passwordEncoder;

  /**
   * 登录账户密码错误次数缓存键名
   *
   * @param username 用户名
   * @return 缓存键key
   */
  private String getPwdErrCntCacheKey(String username) {
    return CacheConstants.PWD_ERR_CNT_KEY + username;
  }

  public void removePwdErrCnt(String loginName) {
    if (redisCache.hasKey(getPwdErrCntCacheKey(loginName))) {
      redisCache.del(getPwdErrCntCacheKey(loginName));
    }
  }

  public boolean matches(String username, String password, String encodedPassword) {
    // 获取当前的重试次数
    Integer retryCount = redisCache.getOrDefault(getPwdErrCntCacheKey(username),0);
    if (retryCount >= maxRetryCount) {
      throw new UserPasswordRetryLimitExceedException(maxRetryCount, lockTime);
    }

    boolean pwdMatched = passwordEncoder.matches(password, encodedPassword);
    if (!pwdMatched) {
      // 记录重试次数
      redisCache.set(getPwdErrCntCacheKey(username), retryCount+1, Duration.of(lockTime, ChronoUnit.MINUTES));
    } else {
      // 清除登录缓存
      removePwdErrCnt(username);
    }
    return pwdMatched;
  }
}
