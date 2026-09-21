package com.github.link2fun.support.context.action.tool;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.session.SaSessionCustomUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
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
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;
import org.noear.solon.core.handle.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 会话适配器: 登录态、会话用户读写、登录/注销、在线会话枚举的唯一落点,
 * 业务侧一律经 {@link com.github.link2fun.support.context.action.ActionContext} 取当前用户。
 */
@Slf4j
public class SaSessionBizTool {


  /** 登录环境字段: 存在 token 会话里, 按会话逐条展示 */
  private static final String IPADDR = "ipaddr";
  private static final String LOGIN_LOCATION = "loginLocation";
  private static final String BROWSER = "browser";
  private static final String OS = "os";


  /** 会话相关的业务数据加载器, 延迟取用避免容器就绪前的空引用 */
  private static ActionContextService contextService() {
    return Solon.context().getBean(ActionContextService.class);
  }


  /**
   * 判断当前线程是否处于已登录状态。
   *
   * <p>无请求上下文时（定时任务、单元测试）Sa-Token 取登录态抛 {@link SaTokenContextException}，
   * 这里视同未登录；其他异常不吞，照常向上抛。
   */
  public static boolean isLogin() {
    try {
      return StpUtil.isLogin();
    } catch (SaTokenContextException e) {
      log.debug("当前线程没有 Sa-Token 上下文, 视同未登录: {}", e.getMessage());
      return false;
    }
  }


  /** 当前登录用户ID, 未登录时由 Sa-Token 抛异常 */
  public static Long currentLoginId() {
    return StpUtil.getLoginIdAsLong();
  }


  /** 当前登录类型 */
  public static String currentLoginType() {
    return StpUtil.getLoginType();
  }


  /**
   * 读取当前会话的登录用户。
   *
   * <p>会话里没有登录用户时按登录ID从库中重建一份返回, 不回写会话。
   */
  public static SessionUser currentUser() {
    final Long loginId = currentLoginId();

    final SessionUser cached = StpUtil.getSession().getModel(Constants.SESSION_USER, SessionUser.class);
    if (cached != null) {
      return attachToken(cached);
    }

    final SessionUser sessionUser = new SessionUser();
    sessionUser.setUserId(loginId);

    final SysUserDTO sysUserDTO = userByUserId(loginId);
    sessionUser.setDeptId(sysUserDTO.getDeptId());
    sessionUser.setPermissions(getPermissionList(loginId, currentLoginType()));
    sessionUser.setUser(sysUserDTO);

    return attachToken(sessionUser);
  }


  /**
   * 登录并建立会话。
   *
   * @param sysUser 登录用户
   * @return 带 token 的登录用户
   */
  public static SessionUser login(final SysUserDTO sysUser) {
    StpUtil.login(sysUser.getUserId());

    final SessionUser sessionUser = new SessionUser();
    sessionUser.setUserId(sysUser.getUserId());
    sessionUser.setDeptId(sysUser.getDeptId());
    sessionUser.setUser(sysUser);
    attachToken(sessionUser);
    setUserAgent(sessionUser);
    sessionUser.setPermissions(getPermissionList(sysUser.getUserId(), currentLoginType()));

    setCurrentUser(sessionUser);
    return sessionUser;
  }


  /** 注销当前会话 */
  public static void logout() {
    StpUtil.logout();
  }


  /**
   * 强退指定 token 的会话。
   *
   * @param tokenValue 会话编号
   */
  public static void kickout(final String tokenValue) {
    StpUtil.kickoutByTokenValue(tokenValue);
  }


  /**
   * 枚举在线会话的登录用户。
   *
   * <p>找不到登录ID的悬挂 token 直接注销掉, 不再返回。
   */
  public static List<SessionUser> listOnlineSessionUsers() {
    final List<String> tokenCacheKeyList = StpUtil.searchTokenValue("", 0, Integer.MAX_VALUE, false);

    final List<SessionUser> sessionUserList = new ArrayList<>(tokenCacheKeyList.size());
    for (final String cacheKey : tokenCacheKeyList) {
      // passkey:login:token:b61dd4b1-b822-4f4f-b9a2-05ff6c7fa777
      // 取最后一个冒号后面的值
      final String tokenValue = StrUtil.subAfter(cacheKey, ":", true);

      final Object loginId = StpUtil.getLoginIdByToken(tokenValue);
      // tokenValue 找不到对应的 loginId , 应该是个异常的 token， 直接注销掉
      if (loginId == null) {
        log.error("tokenValue: {} not found loginId, logout it", tokenValue);
        StpUtil.logoutByTokenValue(tokenValue);
        continue;
      }

      final SaSession session = StpUtil.getSessionByLoginId(loginId);
      final SessionUser sessionUser = session.getModel(Constants.SESSION_USER, SessionUser.class);
      // 会话里没有登录用户, 无法展示为在线用户
      if (sessionUser == null) {
        log.warn("tokenValue: {} 的会话没有登录用户, 跳过", tokenValue);
        continue;
      }
      sessionUserList.add(toOnlineView(sessionUser, tokenValue));
    }
    return sessionUserList;
  }


  /** 会话用户写回会话 */
  public static void setCurrentUser(SessionUser currentUser) {
    StpUtil.getSession().set(Constants.SESSION_USER, currentUser);
  }


  /**
   * 返回指定账号id所拥有的权限码集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的权限码集合
   */
  public static List<String> getPermissionList(final Object loginId, final String loginType) {
    if (SysUser.isSuperAdmin(Convert.toLong(loginId))) {
      return List.of("*:*:*");
    }

    final List<String> roleKeyList = getRoleList(loginId, loginType);


    return roleKeyList.stream()
      .distinct()
      .flatMap(roleKey -> {
        final SaSession roleSession = SaSessionCustomUtil.getSessionById("roleKey:" + roleKey);
        List<String> list = roleSession.get("permissionList", () -> contextService().menuPermissionListByRoleKey(roleKey));
        return list.stream();
      }).distinct().collect(Collectors.toList());

  }


  /**
   * 返回指定账号id所拥有的角色标识集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的角色标识集合
   */
  public static List<String> getRoleList(final Object loginId, final String loginType) {

    SaSession session = StpUtil.getSessionByLoginId(loginId);
    return session.get("roleList",
      () -> contextService().roleListByUserId(Convert.toLong(loginId)).
        stream()
        .map(RoleDTO::getRoleKey)
        .collect(Collectors.toList())
    );
  }


  /**
   * 设置用户代理信息, 同时写进 token 会话供在线列表按会话逐条展示
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

    final SaSession tokenSession = StpUtil.getTokenSession();
    tokenSession.set(IPADDR, sessionUser.getIpaddr());
    tokenSession.set(LOGIN_LOCATION, sessionUser.getLoginLocation());
    tokenSession.set(BROWSER, sessionUser.getBrowser());
    tokenSession.set(OS, sessionUser.getOs());
  }


  /**
   * 根据用户主键获取用户信息
   *
   * @param userId 用户/登录用户主键
   */
  public static SysUserDTO userByUserId(Long userId) {
    return contextService().userByUserId(userId);
  }


  /** 把当前 token 及其登录时间填进会话用户 */
  private static SessionUser attachToken(final SessionUser sessionUser) {
    sessionUser.setToken(StpUtil.getTokenValue());
    sessionUser.setLoginTime(StpUtil.getTokenSession().getCreateTime());
    return sessionUser;
  }


  /** 用 token 会话里的登录环境补齐会话用户的展示字段, 不污染会话里缓存的那份 */
  private static SessionUser toOnlineView(final SessionUser sessionUser, final String tokenValue) {
    final SaSession tokenSession = StpUtil.getTokenSessionByToken(tokenValue);

    final SessionUser view = BeanUtil.copyProperties(sessionUser, SessionUser.class);
    view.setToken(tokenValue);
    view.setLoginTime(tokenSession.getCreateTime());
    view.setIpaddr(tokenSession.getString(IPADDR));
    view.setLoginLocation(tokenSession.getString(LOGIN_LOCATION));
    view.setBrowser(tokenSession.getString(BROWSER));
    view.setOs(tokenSession.getString(OS));
    return view;
  }
}
