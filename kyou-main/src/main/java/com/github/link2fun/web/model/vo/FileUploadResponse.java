package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import lombok.Getter;
import lombok.Setter;

/**
 * 单文件上传响应：落盘文件的访问地址与原始文件名
 */
@Getter
@Setter
public class FileUploadResponse extends Resp<Void> {
  /** 访问地址 */
  private String url;

  /** 文件名 */
  private String fileName;

  /** 落盘文件名 */
  private String newFileName;

  /** 上传时的原始文件名 */
  private String originalFilename;
}
