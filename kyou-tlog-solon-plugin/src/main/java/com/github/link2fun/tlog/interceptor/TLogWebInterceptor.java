package com.github.link2fun.tlog.interceptor;

import com.github.link2fun.tlog.common.TLogWebCommon;
import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.handle.Context;

/**
 * solon @Mapping 的拦截器
 * 暂时不能使用， 因为一个注解只能添加一个拦截器
 *
 * @author link2fun
 */
public class TLogWebInterceptor implements Interceptor {


  /**
   * 拦截
   *
   * @param inv 调用者
   */
  @Override
  public Object doIntercept(final Invocation inv) throws Throwable {

    try {
      TLogWebCommon.loadInstance().preHandle(Context.current());
      //把traceId放入response的header，为了方便有些人有这样的需求，从前端拿整条链路的traceId
      Context.current().headerAdd(TLogConstants.TLOG_TRACE_KEY, TLogContext.getTraceId());

      return inv.invoke();

    } finally {
      TLogWebCommon.loadInstance().afterCompletion();
    }

  }
}
