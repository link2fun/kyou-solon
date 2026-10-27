package com.github.link2fun.framework.filter;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.SaTokenException;
import com.easy.query.core.exception.EasyQuerySQLCommandException;
import com.easy.query.core.exception.EasyQuerySQLStatementException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.core.domain.R;
import com.github.link2fun.support.exception.DemoModeException;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.exception.base.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.exception.StatusException;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;
import org.noear.solon.core.util.DataThrowable;
import org.noear.solon.validation.ValidatorException;

import java.util.Objects;

/**
 * 统一异常处理<br/>
 *
 * @link <a href="https://solon.noear.org/article/503">统一异常处理</a>
 */
@Slf4j
@Component
public class ResponseFilter implements Filter {
  /**
   * 过滤
   *
   * @param ctx   上下文
   * @param chain 链
   */
  @Override
  public void doFilter(final Context ctx, final FilterChain chain) throws Throwable {
    try {
      chain.doFilter(ctx);

      if (!ctx.getHandled()) {
        log.error("404 NOT FOUND: {}", ctx.path());
        ctx.render(R.fail(404, "资源不存在"));
      } else {
        // 已经被solon处理了, 但是有些特殊情况, 需要在这里处理
        // 判断这里的result 是否是 DataThrowable
        if (ctx.result instanceof final DataThrowable dataThrowable) {
          // 如果是 DataThrowable, 且 dataThrowable 中的 data 为空, 则需要手动填充 response body
          final Object data = dataThrowable.data();
          if (Objects.isNull(data)) {
            ctx.render(R.fail(HttpStatus.ERROR, dataThrowable.getMessage()));
          }
        }
      }
    } catch (NotLoginException e) {
      log.warn("未登录访问: {}", ctx.path(), e);
      ctx.render(R.fail(HttpStatus.UNAUTHORIZED, e.getMessage()));
    } catch (NotPermissionException e) {
      log.warn("权限不足: {}", ctx.path(), e);
      ctx.render(R.fail(HttpStatus.FORBIDDEN, e.getMessage()));
    } catch (SaTokenException e) {
      log.warn("认证异常: {}", ctx.path(), e);
      ctx.render(R.fail(HttpStatus.FORBIDDEN, e.getMessage()));
    } catch (ValidatorException e) {
      log.warn("参数校验失败: {}", e.getMessage(), e);
      ctx.render(R.fail(e.getCode(), e.getMessage()));
    } catch (BaseException e) {
      // 业务预期内的失败（密码错误、账号不存在、文件类型不支持等）：
      // 只记一行 warn，不打堆栈，避免正常的登录失败被当成系统故障刷 ERROR 日志、触发告警
      log.warn("业务异常: {}", e.getMessage());
      ctx.render(R.fail(HttpStatus.ERROR, e.getMessage()));
    } catch (DataThrowable e) {
      log.error("DataThrowable: {}", e.getMessage(), e);
      ctx.render(R.fail(HttpStatus.ERROR, e.getMessage()));
    } catch (ServiceException e) {
      log.warn("业务异常: code={}, msg={}, detail={}", e.getCode(), e.getMessage(), e.getDetailMessage(), e);
      ctx.render(R.fail(e.getCode() != null ? e.getCode() : HttpStatus.ERROR, e.getMessage()));
    } catch (DemoModeException e) {
      log.warn("演示模式拦截: {}", ctx.path(), e);
      ctx.render(R.fail(HttpStatus.ERROR, "演示模式，不允许操作"));
    } catch (EasyQuerySQLCommandException exp) {
      Throwable cause = exp.getCause();
      if (cause instanceof EasyQuerySQLStatementException) {
        log.error("SQL语句异常: {}{}{} {}", System.lineSeparator(), ((EasyQuerySQLStatementException) cause).getSQL(), System.lineSeparator(), cause.getMessage(), exp);
      } else {
        log.error("异常信息:{}", exp.getMessage(), exp);
      }
      ctx.render(R.fail(HttpStatus.ERROR, "SQL语句异常"));
    } catch (StatusException e) {
      // Solon 把参数绑定失败包成 StatusException(400), 真正原因在 cause 里
      final Throwable cause = e.getCause();
      if (cause instanceof JsonMappingException jsonError) {
        // 请求体与接口契约不符(未知字段、类型不匹配): 调用方的问题, 不是系统故障
        log.warn("请求体解析失败: {} {}", ctx.path(), jsonError.getOriginalMessage());
        ctx.render(R.fail(HttpStatus.BAD_REQUEST, "请求参数不正确: " + jsonError.getOriginalMessage()));
      } else {
        log.warn("参数绑定失败: {} {}", ctx.path(), e.getMessage());
        ctx.render(R.fail(e.getCode(), "请求参数不正确"));
      }
    } catch (JsonMappingException e) {
      // 请求体与接口契约不符(未知字段、类型不匹配): 这是调用方的问题, 不是系统故障
      log.warn("请求体解析失败: {} {}", ctx.path(), e.getOriginalMessage());
      ctx.render(R.fail(HttpStatus.BAD_REQUEST, "请求参数不正确: " + e.getOriginalMessage()));
    } catch (Throwable e) {
      log.error("系统异常", e);
      ctx.render(R.fail(500, "系统异常"));
    }
  }
}
