package com.github.link2fun.support.utils.http;

import org.noear.solon.core.handle.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * 通用http工具封装
 *
 * @author ruoyi
 */
public class HttpHelper {
  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHelper.class);

  public static String getBodyString() throws IOException {

    return Context.current().body();

  }
}
