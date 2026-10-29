package com.github.link2fun.framework.config;


import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoForRedisson;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.framework.password.impl.BCryptPasswordEncoder;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.core.domain.Resp;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.redisson.api.RedissonClient;

/**
 * Sa-Token权限认证配置
 *
 * @link <a href="https://solon.noear.org/article/110">sa-token-solon-plugin [国产]</a>
 */
@Slf4j
@Configuration
public class SaTokenConfig {

  /** 密码编码处理及比对 */
  @Bean(typed = true)
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }


  /**
   * redisson-solon-plugin 二次应用 结合 cn.dev33:sa-token-redisson-jackson2 插件，创建 sa-token dao
   *
   * @param redissonClient RedissonClient
   * @return SaTokenDao
   * @link <a href="https://solon.noear.org/article/533">redisson-solon-plugin 二次应用 结合 cn.dev33:sa-token-redisson-jackson2 插件，创建 sa-token dao</a>
   */
  @Bean
  public SaTokenDao saTokenDaoInit(@Inject RedissonClient redissonClient) {
    return new SaTokenDaoForRedisson(redissonClient);
  }

  /**
   * 认证失败映射: 无权限 403, 未登录 401, 其余认证异常 403, 非认证异常 500
   */
  static Resp<Void> authFailure(Throwable e) {
    if (e instanceof NotPermissionException) {
      return Resp.error(HttpStatus.FORBIDDEN, e.getMessage());
    } else if (e instanceof NotLoginException) {
      return Resp.error(HttpStatus.UNAUTHORIZED, e.getMessage());
    } else if (e instanceof SaTokenException) {
      log.warn("认证异常: {}", e.getMessage());
      return Resp.error(HttpStatus.FORBIDDEN, e.getMessage());
    }

    log.error("sa全局异常", e);
    return Resp.error(e.getMessage());
  }


  @Bean(index = -100)
  public SaTokenInterceptor saTokenInterceptor() {
    return new SaTokenInterceptor()
      // 指定 [拦截路由] 与 [放行路由]
      .addInclude("/**").addExclude("/favicon.ico")

      // 认证函数: 每次请求执行
      .setAuth(req -> {

        // System.out.println("---------- sa全局认证");
//        SaRouter.match("/**", StpUtil::checkLogin);

        // 根据路由划分模块，不同模块不同鉴权
//        SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
//        SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
      })

      // 异常处理函数：每次认证函数发生异常时执行此函数 //包括注解异常
      .setError(SaTokenConfig::authFailure)

      // 前置函数：在每次认证函数之前执行
      .setBeforeAuth(req -> {
        // ---------- 设置一些安全响应头 ----------
        SaHolder.getResponse()
          // 服务器名称
          .setServer("sa-server")
          // 是否可以在iframe显示视图： DENY=不可以 | SAMEORIGIN=同域下可以 | ALLOW-FROM uri=指定域名下可以
          .setHeader("X-Frame-Options", "SAMEORIGIN")
          // 是否启用浏览器默认XSS防护： 0=禁用 | 1=启用 | 1; mode=block 启用, 并在检查到XSS攻击时，停止渲染页面
          .setHeader("X-XSS-Protection", "1; mode=block")
          // 禁用浏览器内容嗅探
          .setHeader("X-Content-Type-Options", "nosniff");
      })
      ;
  }


}
