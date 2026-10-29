package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import lombok.Getter;
import lombok.Setter;

/**
 * 多文件上传响应：逐个列出落盘文件的访问地址与原始文件名，各项按逗号分隔
 */
@Getter
@Setter
public class FileUploadsResponse extends Resp<Void> {
  /** 访问地址 */
  private String urls;

  /** 文件名 */
  private String fileNames;

  /** 落盘文件名 */
  private String newFileNames;

  /** 上传时的原始文件名 */
  private String originalFilenames;
}
