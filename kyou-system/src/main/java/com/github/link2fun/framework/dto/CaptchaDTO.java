package com.github.link2fun.framework.dto;

import lombok.Data;

/** 验证码数据传输对象 */
@Data
public class CaptchaDTO {

  /** 验证码开启状态 */
  private Boolean captchaEnabled;
  /** 验证码的 uuid, 用于验证码比对 */
  private String uuid;
  /** 验证码的图片 base64 */
  private String img;
}
