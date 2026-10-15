package com.github.link2fun.support.utils.http;

import org.noear.solon.core.handle.Context;

import java.io.IOException;

/**
 * 通用http工具封装
 *
 * @author ruoyi
 */
public class HttpHelper {

  public static String getBodyString() throws IOException {

    return Context.current().body();

  }
}
