package com.github.link2fun.tlog.filter;

import com.github.link2fun.tlog.common.TLogWebCommon;
import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;


/**
 * TLog Filter
 * 用于填充日志链路信息
 *
 * @author link2fun
 */
@Component(index = -999)
public class TLogFilter implements Filter {


  /**
   * TLog Filter
   * 用于填充日志链路信息
   *
   * @param ctx   上下文
   * @param chain 过滤器链
   * @throws Throwable 异常
   */
  @Override
  public void doFilter(final Context ctx, final FilterChain chain) throws Throwable {

    try {
      TLogWebCommon.loadInstance().preHandle(ctx);
      ctx.headerAdd(TLogConstants.TLOG_TRACE_KEY, TLogContext.getTraceId());
      chain.doFilter(ctx);
    } finally {
      TLogWebCommon.loadInstance().afterCompletion();
    }

  }
}
