package com.github.link2fun.framework.config;


import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.solon.dao.SaTokenDaoOfRedissonJackson;
import cn.dev33.satoken.solon.integration.SaTokenInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.framework.password.impl.BCryptPasswordEncoder;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.AjaxResult;
import org.noear.solon.annotation.Bean;
import org.noear.solon.annotation.Configuration;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Handler;
import org.noear.solon.core.route.RouterInterceptor;
import org.noear.solon.core.route.RouterInterceptorChain;
import org.redisson.api.RedissonClient;

/**
 * Sa-Token权限认证配置
 *
 * @link <a href="https://solon.noear.org/article/110">sa-token-solon-plugin [国产]</a>
 */
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
    return new SaTokenDaoOfRedissonJackson(redissonClient);
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
      .setError(e -> {
        System.out.println("---------- sa全局异常 ");
        if (e instanceof NotPermissionException) {
          return AjaxResult.error(HttpStatus.FORBIDDEN, e.getMessage());
        } else if (e instanceof NotLoginException) {
          return AjaxResult.error(HttpStatus.UNAUTHORIZED, e.getMessage());
        }

        return AjaxResult.error(e.getMessage());
      })

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


  /** 再弄一个初始化操作上下文, 放在 SaTokenInterceptor 之后 */
  public RouterInterceptor actionContextInitInterceptor() {
    final RouterInterceptor routerInterceptor = new RouterInterceptor() {
      /**
       * 执行拦截
       *
       */
      @Override
      public void doIntercept(final Context ctx, final Handler mainHandler, final RouterInterceptorChain chain) throws Throwable {

        if (StpUtil.isLogin()) {
          // 如果是已经登录的状态
          ActionContext actionContext = new ActionContext();
          final long userId = StpUtil.getLoginIdAsLong();
          actionContext.setUserId(userId);
        }else{
          ActionContext actionContext = new ActionContext();
          actionContext.setUserId(0L);
        }


        chain.doIntercept(ctx, mainHandler);
      }
    };
    return routerInterceptor;
  }


}
