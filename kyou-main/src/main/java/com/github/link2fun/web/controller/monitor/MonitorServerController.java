package com.github.link2fun.web.controller.monitor;


import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.framework.web.domain.Server;
import com.github.link2fun.support.core.domain.AjaxResult;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;

/**
 * 服务器监控
 *
 * @author ruoyi
 */
@Controller
@Mapping("/monitor/server")
public class MonitorServerController {



  @SaCheckPermission(value = {"monitor:server:list"})
  @Mapping(method = MethodType.GET)
  public AjaxResult serverInfo() throws Exception {
    Server server = new Server();
    server.copyTo();
    return AjaxResult.success(server);
  }
}
