package com.github.link2fun.support.exception.file;

import lombok.Getter;

import java.io.Serial;
import java.util.Arrays;

/**
 * 文件上传 误异常类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
@Getter
public class InvalidExtensionException extends FileUploadException {
  @Serial
  private static final long serialVersionUID = 1L;

  private final String[] allowedExtension;
  private final String extension;
  private final String filename;

  public InvalidExtensionException(String[] allowedExtension, String extension, String filename) {
    super("文件[" + filename + "]后缀[" + extension + "]不正确，请上传" + Arrays.toString(allowedExtension) + "格式");
    this.allowedExtension = allowedExtension;
    this.extension = extension;
    this.filename = filename;
  }

  public static class InvalidImageExtensionException extends InvalidExtensionException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidImageExtensionException(String[] allowedExtension, String extension, String filename) {
      super(allowedExtension, extension, filename);
    }
  }

  public static class InvalidFlashExtensionException extends InvalidExtensionException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidFlashExtensionException(String[] allowedExtension, String extension, String filename) {
      super(allowedExtension, extension, filename);
    }
  }

  public static class InvalidMediaExtensionException extends InvalidExtensionException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidMediaExtensionException(String[] allowedExtension, String extension, String filename) {
      super(allowedExtension, extension, filename);
    }
  }

  public static class InvalidVideoExtensionException extends InvalidExtensionException {
    @Serial
    private static final long serialVersionUID = 1L;

    public InvalidVideoExtensionException(String[] allowedExtension, String extension, String filename) {
      super(allowedExtension, extension, filename);
    }
  }
}
