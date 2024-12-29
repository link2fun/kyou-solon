package com.github.link2fun.support.utils.file;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.config.KyouConfig;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.exception.file.FileNameLengthLimitExceededException;
import com.github.link2fun.support.exception.file.FileSizeLimitExceededException;
import com.github.link2fun.support.exception.file.InvalidExtensionException;
import com.github.link2fun.support.utils.DateUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.uuid.Seq;
import lombok.Getter;
import org.apache.commons.io.FilenameUtils;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.UploadedFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Objects;


/**
 * 文件上传工具类
 *
 * @author ruoyi
 */
public class FileUploadUtils {
  /**
   * 默认大小 50M
   */
  public static final long DEFAULT_MAX_SIZE = 50 * 1024 * 1024;

  /**
   * 默认的文件名最大长度 100
   */
  public static final int DEFAULT_FILE_NAME_LENGTH = 100;

  /**
   * 默认上传的地址
   */
  @Getter
  private static String defaultBaseDir;

  static {
    Solon.context().getBeanAsync(KyouProperties.class, (cfg) -> defaultBaseDir = cfg.getProfile());
  }


  /**
   * 以默认配置进行文件上传
   *
   * @param file 上传的文件
   * @return 文件名称
   */
  public static String upload(UploadedFile file) throws IOException {
    try {
      return upload(getDefaultBaseDir(), file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * 根据文件路径上传
   *
   * @param baseDir 相对应用的基目录
   * @param file    上传的文件
   * @return 文件名称
   */
  public static String upload(String baseDir, UploadedFile file) throws IOException {
    try {
      return upload(baseDir, file, MimeTypeUtils.DEFAULT_ALLOWED_EXTENSION);
    } catch (Exception e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  /**
   * 文件上传
   *
   * @param baseDir          相对应用的基目录
   * @param file             上传的文件
   * @param allowedExtension 上传文件类型
   * @return 返回上传成功的文件名
   * @throws FileSizeLimitExceededException       如果超出最大大小
   * @throws FileNameLengthLimitExceededException 文件名太长
   * @throws IOException                          比如读写文件出错时
   * @throws InvalidExtensionException            文件校验异常
   */
  public static String upload(String baseDir, UploadedFile file, String[] allowedExtension)
    throws FileSizeLimitExceededException, IOException, FileNameLengthLimitExceededException,
    InvalidExtensionException {
    int fileNameLength = StrUtil.length(file.getName());
    if (fileNameLength > FileUploadUtils.DEFAULT_FILE_NAME_LENGTH) {
      throw new FileNameLengthLimitExceededException(FileUploadUtils.DEFAULT_FILE_NAME_LENGTH);
    }

    assertAllowed(file, allowedExtension);

    String fileName = extractFilename(file);

    String absPath = getAbsoluteFile(baseDir, fileName).getAbsolutePath();
    file.transferTo(Paths.get(absPath).toFile());
    return getPathFileName(baseDir, fileName);
  }

  /**
   * 编码文件名
   */
  public static String extractFilename(UploadedFile file) {
    return StringUtils.format("{}/{}_{}.{}", DateUtils.datePath(),
      FilenameUtils.getBaseName(file.getName()), Seq.getId(Seq.uploadSeqType), getExtension(file));
  }

  public static File getAbsoluteFile(String uploadDir, String fileName) {
    File desc = new File(uploadDir + File.separator + fileName);

    if (!desc.exists()) {
      if (!desc.getParentFile().exists()) {
        FileUtil.mkdir(desc.getParentFile());
      }
    }
    return desc;
  }

  public static String getPathFileName(String uploadDir, String fileName) {
    int dirLastIndex = KyouConfig.profile().length() + 1;
    String currentDir = StringUtils.substring(uploadDir, dirLastIndex);
    return Constants.RESOURCE_PREFIX + "/" + currentDir + "/" + fileName;
  }

  /**
   * 文件大小校验
   *
   * @param file 上传的文件
   * @throws FileSizeLimitExceededException 如果超出最大大小
   * @throws InvalidExtensionException      非法的文件类型
   */
  public static void assertAllowed(UploadedFile file, String[] allowedExtension)
    throws FileSizeLimitExceededException, InvalidExtensionException {
    long size = file.getContentSize();
    if (size > DEFAULT_MAX_SIZE) {
      throw new FileSizeLimitExceededException(DEFAULT_MAX_SIZE / 1024 / 1024);
    }

    String fileName = file.getName();
    String extension = getExtension(file);
    if (allowedExtension != null && !isAllowedExtension(extension, allowedExtension)) {
      if (allowedExtension == MimeTypeUtils.IMAGE_EXTENSION) {
        throw new InvalidExtensionException.InvalidImageExtensionException(allowedExtension, extension,
          fileName);
      } else if (allowedExtension == MimeTypeUtils.FLASH_EXTENSION) {
        throw new InvalidExtensionException.InvalidFlashExtensionException(allowedExtension, extension,
          fileName);
      } else if (allowedExtension == MimeTypeUtils.MEDIA_EXTENSION) {
        throw new InvalidExtensionException.InvalidMediaExtensionException(allowedExtension, extension,
          fileName);
      } else if (allowedExtension == MimeTypeUtils.VIDEO_EXTENSION) {
        throw new InvalidExtensionException.InvalidVideoExtensionException(allowedExtension, extension,
          fileName);
      } else {
        throw new InvalidExtensionException(allowedExtension, extension, fileName);
      }
    }
  }

  /**
   * 判断MIME类型是否是允许的MIME类型
   *
   * @param extension        文件后缀
   * @param allowedExtension 允许的后缀
   * @return true：允许；false：不允许
   */
  public static boolean isAllowedExtension(String extension, String[] allowedExtension) {
    return StrUtil.equalsAnyIgnoreCase(extension, allowedExtension);
  }

  /**
   * 获取文件名的后缀
   *
   * @param file 表单文件
   * @return 后缀名
   */
  public static String getExtension(UploadedFile file) {
    String extension = FilenameUtils.getExtension(file.getName());
    if (StringUtils.isEmpty(extension)) {
      extension = MimeTypeUtils.getExtension(Objects.requireNonNull(file.getContentType()));
    }
    return extension;
  }
}
