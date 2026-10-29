package com.github.link2fun.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.github.link2fun.framework.auth.LoginGuard;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysMenuDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.LoginBody;
import com.github.link2fun.system.domain.vo.RouterVo;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.web.model.vo.LoginInfoResponse;
import com.github.link2fun.web.model.vo.LoginResponse;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;


/**
 * 登录验证
 *
 * @author ruoyi
 */
@Controller
public class SysLoginController {
  @Inject
  private LoginGuard loginGuard;

  @Inject
  private ISystemMenuService menuService;

  /**
   * 登录方法
   *
   * @param loginBody 登录信息
   * @return 结果
   */
  @Mapping(value = "/login", method = MethodType.POST)
  public LoginResponse login(@Validated @Body LoginBody loginBody) {
    LoginResponse response = new LoginResponse();
    // 生成令牌
    String token = loginGuard.login(loginBody);
    response.setToken(token);
    return response;
  }

  /** 注销方法 */
  @Mapping(value = "/logout", method = MethodType.POST)
  public Resp<Void> logout() {
    loginGuard.logout();
    return Resp.success();
  }

  /**
   * 获取用户信息
   *
   * @return 用户信息
   */
  @SaCheckLogin
  @Mapping(value = "getInfo", method = MethodType.GET)
  public LoginInfoResponse getInfo() {
    final ActionContext context = ActionContext.current();
    final SysUserDTO userDTO = context.getCurrentUserNotNull().getUser();
    final Long userId = context.getUserId();
    List<String> roles = SaSessionBizTool.getRoleList(userId);
    List<String> permissions = SaSessionBizTool.getPermissionList(userId);
    LoginInfoResponse response = new LoginInfoResponse();
    response.setUser(userDTO);
    response.setRoles(roles);
    response.setPermissions(permissions);
    return response;
  }

  /**
   * 获取路由信息
   *
   * @return 路由信息
   */
  @SaCheckLogin
  @Mapping(value = "getRouters", method = MethodType.GET)
  public Resp<List<RouterVo>> getRouters() {
    Long userId = ActionContext.current().getUserId();
    List<SysMenuDTO> menus = menuService.selectMenuTreeByUserId(userId);
    return Resp.successData(menuService.buildMenus(menus));
  }
}
