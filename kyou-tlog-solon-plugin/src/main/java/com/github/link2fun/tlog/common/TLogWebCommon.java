package com.github.link2fun.tlog.common;

import com.yomahub.tlog.constant.TLogConstants;
import com.yomahub.tlog.context.TLogContext;
import com.yomahub.tlog.core.rpc.TLogLabelBean;
import com.yomahub.tlog.core.rpc.TLogRPCHandler;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.core.handle.Context;


/**
 * TLog web这块的逻辑封装类
 *
 * @author Bryan.Zhang
 * @since 1.1.5
 */
@Slf4j
public class TLogWebCommon extends TLogRPCHandler {

  private static volatile TLogWebCommon tLogWebCommon;

  public static TLogWebCommon loadInstance() {
    if (tLogWebCommon == null) {
      synchronized (TLogWebCommon.class) {
        if (tLogWebCommon == null) {
          tLogWebCommon = new TLogWebCommon();
        }
      }
    }
    return tLogWebCommon;
  }

  /** 从TLogContext中获取 TLogLabelBean */
  public TLogLabelBean getLabelBeanFromLogContext() {
    String traceId = TLogContext.getTraceId();
    String spanId = TLogContext.getSpanId();
    String preIvkApp = TLogContext.getPreIvkApp();
    String preIvkHost = TLogContext.getPreIvkHost();
    String preIp = TLogContext.getPreIp();
    return new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId);
  }

  public void preHandle(TLogLabelBean labelBean){
    processProviderSide(labelBean);
  }

  public void preHandle(Context context) {
    String traceId;
    String spanId;
    String preIvkApp;
    String preIvkHost;
    String preIp;
    if (context !=null && context.request() != null) {
      traceId = context.header(TLogConstants.TLOG_TRACE_KEY);
      spanId = context.header(TLogConstants.TLOG_SPANID_KEY);
      preIvkApp = context.header(TLogConstants.PRE_IVK_APP_KEY);
      preIvkHost = context.header(TLogConstants.PRE_IVK_APP_HOST);
      preIp = context.header(TLogConstants.PRE_IP_KEY);
    } else {
      // 非WEB场景, 前面应该没有源头. 比如定时任务
      traceId = null;
      spanId = null;
      preIvkApp = null;
      preIvkHost = null;
      preIp = null;
    }

    TLogLabelBean labelBean = new TLogLabelBean(preIvkApp, preIvkHost, preIp, traceId, spanId);

    processProviderSide(labelBean);
  }

  public void afterCompletion() {
    cleanThreadLocal();
  }
}
