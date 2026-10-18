package com.github.link2fun.framework.filter;

import com.github.link2fun.support.constant.Constants;
import org.noear.solon.annotation.Component;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.Filter;
import org.noear.solon.core.handle.FilterChain;

/**
 * 本地静态资源(/profile/**)响应头加固<br/>
 *
 * <p>对 KyouApp 经 StaticMappings 映射的 /profile/ 上传目录补两个响应头:
 * X-Content-Type-Options: nosniff 阻止浏览器 MIME 嗅探, Content-Disposition: attachment
 * 强制下载语义, 防止上传内容被当页面/脚本在同源下执行。
 * 图片等子资源(&lt;img&gt;)加载不受 attachment 影响。
 */
@Component
public class StaticResourceSecurityFilter implements Filter {
  /**
   * 过滤
   *
   * @param ctx   上下文
   * @param chain 链
   */
  @Override
  public void doFilter(final Context ctx, final FilterChain chain) throws Throwable {
    if (ctx.path().startsWith(Constants.RESOURCE_PREFIX + "/")) {
      ctx.headerSet("X-Content-Type-Options", "nosniff");
      ctx.headerSet("Content-Disposition", "attachment");
    }
    chain.doFilter(ctx);
  }
}
