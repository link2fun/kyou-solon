package com.github.link2fun.support.utils.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.github.link2fun.support.config.KyouConfig;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.constant.Constants;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * 图片处理工具类
 *
 * @author link2fun
 */

@Slf4j
public class ImageUtils {

  /**
   * 从网络地址或本地地址读取字节流
   *
   * @param urlOrPath 网络连接或本地路径
   * @return 字节数据
   */
  public static byte[] readBytes(String urlOrPath) {
    if (urlOrPath.startsWith("http")) {
      try (HttpResponse response = HttpUtil.createGet(urlOrPath).execute()) {
        return response.bodyBytes();
      }
    }
    // 本机地址
    String localPath = KyouConfig.profile();
    String downloadPath = localPath + StrUtil.subAfter(urlOrPath, Constants.RESOURCE_PREFIX, false);
    InputStream in = FileUtil.getInputStream(downloadPath);
    return IoUtil.readBytes(in);

  }
}
