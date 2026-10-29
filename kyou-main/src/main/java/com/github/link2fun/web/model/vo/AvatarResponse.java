package com.github.link2fun.web.model.vo;

import com.github.link2fun.support.core.domain.Resp;
import lombok.Getter;
import lombok.Setter;

/**
 * 头像上传响应：新头像的访问地址
 */
@Getter
@Setter
public class AvatarResponse extends Resp<Void> {
  /** 头像地址 */
  private String imgUrl;
}
