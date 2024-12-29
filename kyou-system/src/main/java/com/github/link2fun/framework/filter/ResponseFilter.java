package com.github.link2fun.framework.filter;

import cn.dev33.satoken.exception.SaTokenException;
import com.easy.query.core.exception.EasyQuerySQLCommandException;
import com.easy.query.core.exception.EasyQuerySQLStatementException;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.core.domain.R;
import com.github.link2fun.support.exception.DemoModeException;
import com.github.link2fun.support.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
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
    } catch (SaTokenException e) {
      ctx.render(R.fail(HttpStatus.FORBIDDEN, "你没有权限"));
    } catch (ValidatorException e) {
      ctx.render(R.fail(e.getCode(), e.getMessage()));
    } catch (DataThrowable | ServiceException | DemoModeException e) {
      ctx.render(R.fail(HttpStatus.ERROR, e.getMessage()));
    } catch (EasyQuerySQLCommandException exp) {
      Throwable cause = exp.getCause();
      if (cause instanceof EasyQuerySQLStatementException) {
        log.error("SQL语句异常: {}{}{} {}", System.lineSeparator(), ((EasyQuerySQLStatementException) cause).getSQL(), System.lineSeparator(), cause.getMessage(), exp);
      } else {
        log.error("异常信息:{}", exp.getMessage(), exp);
      }
      ctx.render(R.fail(HttpStatus.ERROR, "SQL语句异常"));
    } catch (Throwable e) {
      log.error("系统异常", e);
      ctx.render(R.fail(500, "系统异常"));
    }
  }
}
