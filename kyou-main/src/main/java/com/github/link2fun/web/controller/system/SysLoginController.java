package com.github.link2fun.web.controller.system;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.framework.web.service.SysLoginService;
import com.github.link2fun.framework.web.service.SysPermissionService;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.core.domain.model.LoginBody;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;
import java.util.Set;


/**
 * 登录验证
 *
 * @author ruoyi
 */
@Controller
public class SysLoginController {
  @Inject
  private SysLoginService loginService;

  @Inject
  private ISystemMenuService menuService;

  @Inject
  private SysPermissionService permissionService;

  /**
   * 登录方法
   *
   * @param loginBody 登录信息
   * @return 结果
   */
  @Mapping(value = "/login", method = MethodType.POST)
  public AjaxResult login(@Validated @Body LoginBody loginBody) {
    AjaxResult ajax = AjaxResult.success();
    // 生成令牌
    String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
      loginBody.getUuid());
    ajax.put(Constants.TOKEN, token);
    return ajax;
  }

  @Mapping(value = "/logout", method = MethodType.POST)
  public AjaxResult logout() {
    StpUtil.logout();
    return AjaxResult.success();
  }

  /**
   * 获取用户信息
   *
   * @return 用户信息
   */
  @SaCheckLogin
  @Mapping(value = "getInfo", method = MethodType.GET)
  public AjaxResult getInfo() {
    final ActionContext context = ActionContext.current();
    final SysUserDTO userDTO = context.getSessionUser().getUser();
    final Long userId = context.getUserId();
    // 角色集合
    Set<String> roles = permissionService.getRolePermission(userId);
    // 权限集合
    List<String> permissions = permissionService.getMenuPermission(userDTO);
    AjaxResult ajax = AjaxResult.success();
    ajax.put("user", userDTO);
    ajax.put("roles", roles);
    ajax.put("permissions", permissions);
    return ajax;
  }

  /**
   * 获取路由信息
   *
   * @return 路由信息
   */
  @SaCheckLogin
  @Mapping(value = "getRouters", method = MethodType.GET)
  public AjaxResult getRouters() {
    Long userId = SecurityUtils.getUserId();
    List<SysMenu> menus = menuService.selectMenuTreeByUserId(userId);
    return AjaxResult.success(menuService.buildMenus(menus));
  }
}
