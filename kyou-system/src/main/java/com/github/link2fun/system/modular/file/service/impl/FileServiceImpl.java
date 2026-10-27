package com.github.link2fun.system.modular.file.service.impl;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.config.ServerConfig;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.file.FileUploadUtils;
import com.github.link2fun.support.utils.file.FileUtils;
import com.github.link2fun.system.modular.file.model.vo.FileUploadVO;
import com.github.link2fun.system.modular.file.service.IFileService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.UploadedFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 通用文件上传下载实现; 下载名还原与本地资源路径解析都收在这里
 */
@Component
public class FileServiceImpl implements IFileService {

  /** 拼接访问地址用 */
  @Inject
  private ServerConfig serverConfig;

  /** 上传/下载/资源目录配置 */
  @Inject
  private KyouProperties kyouProperties;

  /**
   * 下载临时文件
   *
   * @param fileName        存储的文件名
   * @param delete          消费完是否删除文件
   * @param contentConsumer 文件内容消费器
   */
  @Override
  public void downloadFile(final String fileName, final boolean delete,
                           final Consumer<InputStream> contentConsumer) throws IOException {
    if (!FileUtils.checkAllowDownload(fileName)) {
      throw new ServiceException(StrUtil.format("文件名称({})非法，不允许下载。 ", fileName), HttpStatus.BAD_REQUEST);
    }
    // 导出文件以 "<uuid>_<原名>.xlsx" 落盘, 下载名去掉 uuid 前缀并加时间戳避免重名
    final String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
    final String filePath = kyouProperties.getDownloadPath() + fileName;

    FileUtils.setAttachmentResponseHeader(realFileName);
    consumeFile(filePath, contentConsumer);
    if (delete) {
      FileUtils.deleteFile(filePath);
    }
  }

  /**
   * 上传单个文件
   *
   * @param file 上传的文件
   * @return 上传结果(访问地址、存储名、原文件名)
   */
  @Override
  public FileUploadVO uploadFile(final UploadedFile file) throws IOException {
    final String fileName = FileUploadUtils.upload(kyouProperties.getUploadPath(), file);
    return toResult(file, fileName);
  }

  /**
   * 批量上传文件
   *
   * @param files 上传的文件列表
   * @return 每个文件的上传结果
   */
  @Override
  public List<FileUploadVO> uploadFiles(final List<UploadedFile> files) throws IOException {
    final List<FileUploadVO> results = new ArrayList<>(files.size());
    for (final UploadedFile file : files) {
      results.add(uploadFile(file));
    }
    return results;
  }

  /**
   * 下载本地资源(如 /profile 下的头像)
   *
   * @param resource        资源地址
   * @param contentConsumer 文件内容消费器
   */
  @Override
  public void downloadResource(final String resource,
                               final Consumer<InputStream> contentConsumer) throws IOException {
    if (!FileUtils.checkAllowDownload(resource)) {
      throw new ServiceException(StrUtil.format("资源文件({})非法，不允许下载。 ", resource), HttpStatus.BAD_REQUEST);
    }
    // /profile 前缀映射到本地 profile 目录, 与 ImageUtils 的解析口径一致
    final String downloadPath = kyouProperties.getProfile()
      + StrUtil.subAfter(resource, Constants.RESOURCE_PREFIX, false);

    FileUtils.setAttachmentResponseHeader(StrUtil.subAfter(downloadPath, "/", true));
    consumeFile(downloadPath, contentConsumer);
  }

  /**
   * 打开文件交给消费器, 由本方法负责关闭
   *
   * <p>流不交给调用方自己关: 消费器可能提前返回或抛异常, 关闭职责留在开流的一侧才不会泄漏。
   */
  private void consumeFile(final String filePath, final Consumer<InputStream> contentConsumer) throws IOException {
    final Path path = Path.of(filePath);
    if (!Files.exists(path)) {
      throw new ServiceException(StrUtil.format("文件({})不存在", filePath), HttpStatus.NOT_FOUND);
    }
    try (InputStream in = new FileInputStream(path.toFile())) {
      contentConsumer.accept(in);
    }
  }

  /** 组装上传结果 */
  private FileUploadVO toResult(final UploadedFile file, final String fileName) {
    final FileUploadVO result = new FileUploadVO();
    result.setUrl(serverConfig.getUrl() + fileName);
    result.setFileName(fileName);
    result.setNewFileName(FileUtils.getName(fileName));
    result.setOriginalFilename(file.getName());
    return result;
  }
}
