package com.github.link2fun.web.controller.system;

import com.github.link2fun.framework.auth.LoginGuard;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.model.RegisterBody;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;


/**
 * 注册验证
 *
 * @author ruoyi
 */
@Controller
public class SysRegisterController {
  @Inject
  private LoginGuard loginGuard;


  /** 注册 */
  @Mapping(value = "/register", method = MethodType.POST)
  public AjaxResult register(@Body RegisterBody user) {
    loginGuard.register(user);
    return AjaxResult.success();
  }
}
