package com.github.link2fun.web.controller.common;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.framework.config.ServerConfig;
import com.github.link2fun.support.config.KyouProperties;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.utils.file.FileUploadUtils;
import com.github.link2fun.support.utils.file.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.UploadedFile;
import org.smartboot.http.common.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用请求处理
 *
 * @author ruoyi
 */
@Slf4j
@Controller
@Mapping("/common")
public class CommonController {

  @Inject
  private ServerConfig serverConfig;
  @Inject
  private KyouProperties kyouProperties;

  private static final String FILE_DELIMITER = ",";

  /**
   * 通用下载请求
   *
   * @param fileName 文件名称
   * @param delete   是否删除
   */
  @SaCheckLogin
  @Mapping(value = "/download", method = MethodType.GET)
  public void fileDownload(String fileName, Boolean delete, Context context) {
    try {
      if (!FileUtils.checkAllowDownload(fileName)) {
        throw new Exception(StrUtil.format("文件名称({})非法，不允许下载。 ", fileName));
      }
      String realFileName = System.currentTimeMillis() + fileName.substring(fileName.indexOf("_") + 1);
      String filePath = kyouProperties.getDownloadPath() + fileName;

      context.contentType("application/octet-stream");
      FileUtils.setAttachmentResponseHeader(realFileName);
      FileUtils.writeBytes(filePath, context.outputStream());
      if (delete) {
        FileUtils.deleteFile(filePath);
      }
    } catch (Exception e) {
      log.error("下载文件失败", e);
    }
  }

  /**
   * 通用上传请求（单个）
   */
  @SaCheckLogin
  @Mapping(value = "/upload", method = MethodType.POST)
  public AjaxResult uploadFile(UploadedFile file) {
    try {
      // 上传文件路径
      String filePath = kyouProperties.getUploadPath();
      // 上传并返回新文件名称
      String fileName = FileUploadUtils.upload(filePath, file);
      String url = serverConfig.getUrl() + fileName;
      AjaxResult ajax = AjaxResult.success();
      ajax.put("url", url);
      ajax.put("fileName", fileName);
      ajax.put("newFileName", FileUtils.getName(fileName));
      ajax.put("originalFilename", file.getName());
      return ajax;
    } catch (Exception e) {
      return AjaxResult.error(e.getMessage());
    }
  }

  /**
   * 通用上传请求（多个）
   */
  @SaCheckLogin
  @Mapping(value = "/uploads", method = MethodType.POST)
  public AjaxResult uploadFiles(List<UploadedFile> files) {
    try {
      // 上传文件路径
      String filePath = kyouProperties.getUploadPath();
      List<String> urls = new ArrayList<>();
      List<String> fileNames = new ArrayList<>();
      List<String> newFileNames = new ArrayList<>();
      List<String> originalFilenames = new ArrayList<>();
      for (UploadedFile file : files) {
        // 上传并返回新文件名称
        String fileName = FileUploadUtils.upload(filePath, file);
        String url = serverConfig.getUrl() + fileName;
        urls.add(url);
        fileNames.add(fileName);
        newFileNames.add(FileUtils.getName(fileName));
        originalFilenames.add(file.getName());
      }
      AjaxResult ajax = AjaxResult.success();
      ajax.put("urls", StrUtil.join(FILE_DELIMITER, urls));
      ajax.put("fileNames", StrUtil.join(FILE_DELIMITER, fileNames));
      ajax.put("newFileNames", StrUtil.join(FILE_DELIMITER, newFileNames));
      ajax.put("originalFilenames", StrUtil.join(FILE_DELIMITER, originalFilenames));
      return ajax;
    } catch (Exception e) {
      return AjaxResult.error(e.getMessage());
    }
  }

  /**
   * 本地资源通用下载
   */
  @SaCheckLogin
  @Mapping(value = "/download/resource", method = MethodType.GET)
  public void resourceDownload(String resource, Context context) {
    try {
      if (!FileUtils.checkAllowDownload(resource)) {
        throw new Exception(StrUtil.format("资源文件({})非法，不允许下载。 ", resource));
      }
      // 本地资源路径
      String localPath = kyouProperties.getProfile();
      // 数据库资源地址
      String downloadPath = localPath + StringUtils.substringAfter(resource, Constants.RESOURCE_PREFIX);
      // 下载名称
      String downloadName = StringUtils.substringAfter(downloadPath, "/");
      context.contentType("application/octet-stream");
      FileUtils.setAttachmentResponseHeader(downloadName);
      FileUtils.writeBytes(downloadPath, context.outputStream());
    } catch (Exception e) {
      log.error("下载文件失败", e);
    }
  }
}
