package com.github.link2fun.web.controller.system;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.context.buildinfo.BuildInfoContext;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;


/**
 * 首页
 *
 * @author ruoyi
 */
@Controller
public class SysIndexController {

  /**
   * 访问首页，提示语
   */
  @Mapping("/")
  public String index() {

    return StrUtil.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。", Solon.cfg().appName(), BuildInfoContext.getVersion());
  }
}
