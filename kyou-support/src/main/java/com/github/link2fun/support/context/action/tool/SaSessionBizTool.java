package com.github.link2fun.support.context.action.tool;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.context.action.service.ActionContextService;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.utils.ip.AddressUtils;
import com.github.link2fun.support.utils.ip.IpUtils;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.Context;

import java.util.List;
import java.util.stream.Collectors;

/**
 * SaSession业务工具类
 */
public class SaSessionBizTool {


  private static ActionContextService contextService;

  static {

    Solon.context().getBeanAsync(ActionContextService.class, bw -> contextService = bw);
  }


  public static SessionUser getCurrentUser(Boolean autoCreate) {
    if (StpUtil.isLogin()) {
      final SessionUser sessionUser = StpUtil.getSession().getModel(Constants.SESSION_USER, SessionUser.class);
      sessionUser.setTokenSession(StpUtil.getTokenSession());
      sessionUser.setTokenInfo(StpUtil.getTokenInfo());
      return sessionUser;
    }
    if (autoCreate) {
      final SessionUser sessionUser = new SessionUser();
      sessionUser.setTokenInfo(StpUtil.getTokenInfo());
      sessionUser.setTokenSession(StpUtil.getTokenSession());
      return sessionUser;
    }
    return null;

  }


  public static SessionUser getCurrentUser() {
    return getCurrentUser(true);
  }

  public static void setCurrentUser(SessionUser currentUser) {
    StpUtil.getSession().set(Constants.SESSION_USER, currentUser);
  }


  /**
   * 返回指定账号id所拥有的权限码集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的权限码集合
   * @link <a href="https://sa-token.cc/doc.html#/fun/jur-cache">参考：将权限数据放在缓存里</a>
   */
  public static List<String> getPermissionList(final Object loginId, final String loginType) {
    if (SysUser.isAdmin(Convert.toLong(loginId))) {
      return List.of("*:*:*");
    }

    final List<String> roleKeyList = getRoleList(loginId, loginType);


    return roleKeyList.stream()
      .distinct()
      .flatMap(roleKey -> {
        final SaSession roleSession = SaSessionCustomUtil.getSessionById("roleKey:" + roleKey);
        List<String> list = roleSession.get("permissionList", () -> contextService.menuPermissionListByRoleKey(roleKey));
        return list.stream();
      }).distinct().collect(Collectors.toList());

  }


  /** 返回指定账号id所拥有的角色集合 */
  public static List<RoleDTO> getRole(final Object loginId, final String loginType) {
    SaSession session = StpUtil.getSessionByLoginId(loginId);
    return session.get("role",
      () -> contextService.roleListByUserId(Convert.toLong(loginId))
        .stream().map(role -> BeanUtil.copyProperties(role, RoleDTO.class)).collect(Collectors.toList())
    );
  }


  /**
   * 返回指定账号id所拥有的角色标识集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的角色标识集合
   * @link <a href="https://sa-token.cc/doc.html#/fun/jur-cache">参考：将权限数据放在缓存里</a>
   */
  public static List<String> getRoleList(final Object loginId, final String loginType) {

    SaSession session = StpUtil.getSessionByLoginId(loginId);
    return session.get("roleList",
      () -> contextService.roleListByUserId(Convert.toLong(loginId)).
        stream()
        .map(RoleDTO::getRoleKey)
        .collect(Collectors.toList())
    );
  }


  /**
   * 设置用户代理信息
   *
   * @param sessionUser 登录信息
   */
  public static void setUserAgent(SessionUser sessionUser) {
    UserAgent userAgent = UserAgentUtil.parse(Context.current().userAgent());
    String ip = IpUtils.getIpAddr();
    sessionUser.setIpaddr(ip);
    sessionUser.setLoginLocation(AddressUtils.getRealAddressByIP(ip));
    sessionUser.setBrowser(userAgent.getBrowser().getName());
    sessionUser.setOs(userAgent.getOs().getName());
  }


  /**
   * 根据用户主键获取用户信息
   *
   * @param userId 用户/登录用户主键
   */
  public static SysUserDTO userByUserId(Long userId) {
    return contextService.userByUserId(userId);
  }
}
