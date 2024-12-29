package com.github.link2fun.context.buildinfo;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import com.google.common.base.Preconditions;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * 这是一个用于获取构建信息的上下文类。
 * <p>
 * 该类会从JAR包或文件系统中加载构建信息，并提供相关的获取方法。
 * 通过静态代码块在类加载时自动加载构建信息。
 * 提供了获取版本、构建时间、修订版本等信息的方法。
 */
@Slf4j
public class BuildInfoContext {

  @Getter
  private static Props buildInfo = new Props();

  static {
    try {
      // 获取当前类的代码源位置
      URL location = BuildInfoContext.class.getProtectionDomain().getCodeSource().getLocation();
      // 获取协议类型（如 "file" 或 "jar"）
      String protocol = location.getProtocol();
      // 获取路径信息
      String path = location.getPath();

      // 如果协议是 "jar"，表示程序是从JAR包中运行的
      if (StrUtil.equals(protocol, "jar")) {
        // 提取JAR文件的路径
        String jarFilePath = StrUtil.subBefore(path, "!", false);
        JarFile jarFile = null;
        InputStream inputStream = null;
        try {
          // 创建JAR文件对象
          jarFile = new JarFile(new File(new URI(jarFilePath)));
          // 获取JAR文件中指定的条目（build.properties文件）
          ZipEntry entry = jarFile.getEntry("BOOT-INF/classes/build.properties");
          // 确保条目存在
          Preconditions.checkArgument(entry != null, "build.properties not found in jar file");
          // 获取条目的输入流
          inputStream = jarFile.getInputStream(entry);
          // 加载属性文件
          buildInfo.load(inputStream);
        } finally {
          // 关闭输入流和JAR文件，释放资源
          IoUtil.closeIfPosible(inputStream);
          IoUtil.closeIfPosible(jarFile);
        }
      } else {
        // 如果不是从JAR包中运行，则从文件系统中加载
        File buildPropFile = new File(new File(path).getParentFile(),
            "generated/build-metadata/build.properties");
        // 检查文件是否存在
        if (FileUtil.exist(buildPropFile)) {
          // 使用try-with-resources语句自动关闭文件输入流
          try (FileInputStream fileInputStream = new FileInputStream(buildPropFile)) {
            // 加载属性文件
            buildInfo.load(fileInputStream);
          }
        }
      }
    } catch (Exception e) {
      // 捕获异常并记录错误信息
      log.error("Error loading build properties", e);
    }
  }

  /** 获取版本信息 */
  public static String getVersion() {
    return buildInfo.getStr("version");
  }

  /** 获取构建时间 */
  public static LocalDateTime getBuildTime() {
    return LocalDateTimeUtil.of(buildInfo.getLong("timestamp"));
  }

  /** 获取构建时间 str */
  public static String getBuildTimeStr() {
    return LocalDateTimeUtil.formatNormal(getBuildTime());
  }

  /** 获取GIT/SVN版本 */
  public static String getRevision() {
    return buildInfo.getStr("revision");
  }

  /** 用于输出的字符串 */
  public static String buildInfoPrintStr() {
    return getVersion() + " - " + getRevision() + " (" + getBuildTimeStr() + ")";
  }

}
