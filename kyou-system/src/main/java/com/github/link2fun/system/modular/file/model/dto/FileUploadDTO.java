package com.github.link2fun.system.modular.file.model.dto;

import lombok.Data;

/**
 * 单个文件的上传结果
 */
@Data
public class FileUploadDTO {

  /** 可访问地址 */
  private String url;

  /** 存储文件名(含日期目录) */
  private String fileName;

  /** 不含目录的文件名 */
  private String newFileName;

  /** 客户端原始文件名 */
  private String originalFilename;
}
