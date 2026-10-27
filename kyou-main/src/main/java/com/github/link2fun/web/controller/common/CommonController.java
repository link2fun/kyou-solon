package com.github.link2fun.web.controller.common;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.system.modular.file.model.vo.FileUploadVO;
import com.github.link2fun.system.modular.file.service.IFileService;
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
  public AjaxResult uploadFile(UploadedFile file) throws IOException {
    final FileUploadVO result = fileService.uploadFile(file);
    AjaxResult ajax = AjaxResult.success();
    ajax.put("url", result.getUrl());
    ajax.put("fileName", result.getFileName());
    ajax.put("newFileName", result.getNewFileName());
    ajax.put("originalFilename", result.getOriginalFilename());
    return ajax;
  }

  /**
   * 通用上传请求（多个）
   */
  @SaCheckLogin
  @SaCheckPermission("common:upload")
  @Mapping(value = "/uploads", method = MethodType.POST)
  public AjaxResult uploadFiles(List<UploadedFile> files) throws IOException {
    final List<FileUploadVO> results = fileService.uploadFiles(files);
    AjaxResult ajax = AjaxResult.success();
    ajax.put("urls", join(results, FileUploadVO::getUrl));
    ajax.put("fileNames", join(results, FileUploadVO::getFileName));
    ajax.put("newFileNames", join(results, FileUploadVO::getNewFileName));
    ajax.put("originalFilenames", join(results, FileUploadVO::getOriginalFilename));
    return ajax;
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
  private static String join(final List<FileUploadVO> results,
                             final Function<FileUploadVO, String> field) {
    return results.stream().map(field).collect(Collectors.joining(FILE_DELIMITER));
  }
}
