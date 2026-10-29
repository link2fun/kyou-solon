package com.github.link2fun.web.controller.common;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.system.modular.file.model.dto.FileUploadDTO;
import com.github.link2fun.system.modular.file.service.IFileService;
import com.github.link2fun.web.model.vo.FileUploadResponse;
import com.github.link2fun.web.model.vo.FileUploadsResponse;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@Controller
@Mapping("/common")
public class CommonController {

  @Inject
  private IFileService fileService;

  /** 多文件上传结果的字段分隔符 */
  private static final String FILE_DELIMITER = ",";

  /**
   * 通用下载请求
   *
   * @param fileName 文件名称
   * @param delete   是否删除
   */
  @SaCheckLogin
  @Mapping(value = "/download", method = MethodType.GET)
  public void fileDownload(String fileName, Boolean delete, Context context) throws IOException {
    context.contentType("application/octet-stream");
    fileService.downloadFile(fileName, Boolean.TRUE.equals(delete), in -> copyTo(in, context));
  }

  /**
   * 通用上传请求（单个）
   */
  @SaCheckLogin
  @SaCheckPermission("common:upload")
  @Mapping(value = "/upload", method = MethodType.POST)
  public FileUploadResponse uploadFile(UploadedFile file) throws IOException {
    final FileUploadDTO result = fileService.uploadFile(file);
    FileUploadResponse response = new FileUploadResponse();
    response.setUrl(result.getUrl());
    response.setFileName(result.getFileName());
    response.setNewFileName(result.getNewFileName());
    response.setOriginalFilename(result.getOriginalFilename());
    return response;
  }

  /**
   * 通用上传请求（多个）
   */
  @SaCheckLogin
  @SaCheckPermission("common:upload")
  @Mapping(value = "/uploads", method = MethodType.POST)
  public FileUploadsResponse uploadFiles(List<UploadedFile> files) throws IOException {
    final List<FileUploadDTO> results = fileService.uploadFiles(files);
    FileUploadsResponse response = new FileUploadsResponse();
    response.setUrls(join(results, FileUploadDTO::getUrl));
    response.setFileNames(join(results, FileUploadDTO::getFileName));
    response.setNewFileNames(join(results, FileUploadDTO::getNewFileName));
    response.setOriginalFilenames(join(results, FileUploadDTO::getOriginalFilename));
    return response;
  }

  /**
   * 本地资源通用下载
   */
  @SaCheckLogin
  @Mapping(value = "/download/resource", method = MethodType.GET)
  public void resourceDownload(String resource, Context context) throws IOException {
    context.contentType("application/octet-stream");
    fileService.downloadResource(resource, in -> copyTo(in, context));
  }

  /**
   * 把文件内容写进响应
   *
   * <p>响应流由容器管理, 这里只写不关; 文件流由 service 关闭。
   */
  private static void copyTo(final InputStream in, final Context context) {
    try {
      in.transferTo(context.outputStream());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** 按字段拼接为逗号分隔的字符串 */
  private static String join(final List<FileUploadDTO> results,
                             final Function<FileUploadDTO, String> field) {
    return results.stream().map(field).collect(Collectors.joining(FILE_DELIMITER));
  }
}
