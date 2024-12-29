package com.github.link2fun.support.config;


import lombok.Data;

/**
 * 读取项目相关配置
 *
 * @author link2fun
 */
@Data
public class KyouProperties {

  /** 上传路径 */
  private String profile;

  /** 获取地址开关 */
  private Boolean addressEnabled = false;

  /** 验证码类型 */
  private String captchaType;


  /** 获取导入上传路径 */
  public String getImportPath() {
    return getProfile() + "/import";
  }


  /** 获取头像上传路径 */
  public String getAvatarPath() {
    return getProfile() + "/avatar";
  }


  /** 获取下载路径 */
  public String getDownloadPath() {
    return getProfile() + "/download/";
  }


  /** 获取上传路径 */
  public String getUploadPath() {
    return getProfile() + "/upload";
  }
}
