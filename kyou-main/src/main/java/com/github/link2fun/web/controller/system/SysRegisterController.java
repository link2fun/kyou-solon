package com.github.link2fun.web.controller.system;

import com.github.link2fun.framework.service.SysRegisterService;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.model.RegisterBody;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.tool.SystemConfigContext;
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
public class SysRegisterController extends BaseController {
  @Inject
  private SysRegisterService registerService;


  @Mapping(value = "/register", method = MethodType.POST)
  public AjaxResult register(@Body RegisterBody user) {
    if (!SystemConfigContext.accountRegisterEnabled()) {
      return error("当前系统没有开启注册功能！");
    }
    String msg = registerService.register(user);
    return StringUtils.isEmpty(msg) ? success() : error(msg);
  }
}
