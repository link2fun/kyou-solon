package com.github.link2fun.web.controller.common;

import cn.hutool.core.lang.Dict;
import com.github.link2fun.framework.dto.CaptchaDTO;
import com.github.link2fun.framework.service.CaptchaService;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.core.handle.Result;

/**
 * 验证码操作处理
 *
 * @author link2fun
 */
@Controller
public class CaptchaController {

  @Inject
  private CaptchaService captchaService;

  /** 生成验证码 */
  @Mapping(value = "/captchaImage", method = MethodType.GET)
  public Dict getCode() {

    final CaptchaDTO captchaDTO = captchaService.getCaptcha();
    final Dict dict = Dict.create().parseBean(captchaDTO);
    // 拼上 code 信息
    dict.parseBean(Result.succeed());

    return dict;
  }
}
