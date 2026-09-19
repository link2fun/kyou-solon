package com.github.link2fun.support.context.action;

import cn.dev33.satoken.exception.SaTokenContextException;
import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder(setterPrefix = "with")
@Setter
@Slf4j
public class ActionContext implements Serializable {

  /** 当前用户ID */
  private Long userId;

  /** 当前部门ID */
  private Long deptId;

  private SessionUser sessionUser;


  /**
   * 获取当前已登录用户信息。
   *
   * @return 返回当前已登录用户信息。
   * @throws IllegalStateException 如果SessionUser尚未初始化，则抛出此异常。
   */
  public SessionUser getCurrentUserNotNull() {
    if (Objects.isNull(sessionUser)) {
      throw new IllegalStateException("SessionUser 尚未初始化");
    }

    return sessionUser;
  }

  /** 判断是否是管理员 */
  public boolean isAdmin() {
    return Objects.nonNull(sessionUser) && Objects.nonNull(sessionUser.getUser()) && sessionUser.getUser().isAdmin();
  }

  /** 判断当前用户是否存在 */
  public boolean currentUserExists() {
    return Objects.nonNull(sessionUser);
  }



  /**
   * 获取当前操作上下文。
   *
   * <p>未登录、或当前线程不在 Web 请求中（定时任务、单元测试）时返回一个未初始化的
   * 空上下文而不抛异常 —— EasyQuery 审计拦截器据此把操作人视同 system。
   *
   * @return 当前操作上下文, 未登录时各项均为空
   */
  public static ActionContext current() {
    if (!isLogin()) {
      return new ActionContext();
    }
    long loginIdAsLong = StpUtil.getLoginIdAsLong();

    final SessionUser _currentUser = Optional.ofNullable(StpUtil.getSession().getModel(Constants.SESSION_USER, SessionUser.class))
      .orElseGet(()->{
        SessionUser _sessionUser = new SessionUser();

        SaSessionBizTool.setUserAgent(_sessionUser);

        _sessionUser.setUserId(loginIdAsLong);

        SysUserDTO sysUserDTO = SaSessionBizTool.userByUserId(loginIdAsLong);
        List<String> permissionList = SaSessionBizTool.getPermissionList(loginIdAsLong, StpUtil.getLoginType());
        _sessionUser.setDeptId(sysUserDTO.getDeptId());
        _sessionUser.setPermissions(permissionList);
        _sessionUser.setUser(sysUserDTO);


        return _sessionUser;
      })
      .loadTokenInfoAndTokenSession();

    if (Objects.nonNull(_currentUser)) {
      return ActionContext.builder()
        .withUserId(loginIdAsLong)
        .withDeptId(_currentUser.getDeptId())
        .withSessionUser(_currentUser)
        .build();
    }
    return ActionContext.builder()
      .withUserId(loginIdAsLong)
      .build();




  }


  /**
   * 判断当前线程是否处于已登录状态。
   *
   * <p>无请求上下文时（定时任务、单元测试）Sa-Token 取登录态抛 {@link SaTokenContextException}，
   * 这里视同未登录；其他异常不吞，照常向上抛。
   */
  private static boolean isLogin() {
    try {
      return StpUtil.isLogin();
    } catch (SaTokenContextException e) {
      log.debug("当前线程没有 Sa-Token 上下文, 视同未登录: {}", e.getMessage());
      return false;
    }
  }


  /**
   * 获取当前用户所拥有的角色列表
   *
   * @return 返回一个包含角色信息的DTO列表，如果当前用户不存在，则返回空列表
   */
  public List<RoleDTO> getRoles() {
    return Optional.ofNullable(getSessionUser())
      .map(SessionUser::getUser).map(SysUserDTO::getRoles)
      .orElse(Collections.emptyList());
  }
}
