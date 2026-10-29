package com.github.link2fun.system.modular.file.service;

import com.github.link2fun.system.modular.file.model.dto.FileUploadDTO;
import org.noear.solon.core.handle.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

/**
 * 通用文件上传下载
 */
public interface IFileService {

  /**
   * 下载临时文件; 校验文件名、按命名约定还原下载名
   *
   * <p>文件流由本方法打开并关闭, 调用方只决定怎么消费(写响应、算摘要、转存)。
   *
   * @param fileName        存储的文件名
   * @param delete          消费完是否删除文件
   * @param contentConsumer 文件内容消费器
   * @throws java.io.IOException 打开文件失败
   */
  void downloadFile(String fileName, boolean delete, Consumer<InputStream> contentConsumer) throws IOException;

  /**
   * 上传单个文件
   *
   * @param file 上传的文件
   * @return 上传结果(访问地址、存储名、原文件名)
   * @throws java.io.IOException 落盘失败
   */
  FileUploadDTO uploadFile(UploadedFile file) throws IOException;

  /**
   * 批量上传文件
   *
   * @param files 上传的文件列表
   * @return 每个文件的上传结果
   * @throws java.io.IOException 任一文件落盘失败
   */
  List<FileUploadDTO> uploadFiles(List<UploadedFile> files) throws IOException;

  /**
   * 下载本地资源(如 /profile 下的头像)
   *
   * @param resource        资源地址
   * @param contentConsumer 文件内容消费器
   * @throws java.io.IOException 打开文件失败
   */
  void downloadResource(String resource, Consumer<InputStream> contentConsumer) throws IOException;
}
