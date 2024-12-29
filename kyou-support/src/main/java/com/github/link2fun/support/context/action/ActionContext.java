package com.github.link2fun.support.context.action;

import cn.dev33.satoken.stp.StpUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import lombok.*;

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



  public static ActionContext current() {
    if (!StpUtil.isLogin()) {
      // 没有登录, 给一个空的
      return new ActionContext();
    }
    long loginIdAsLong = StpUtil.getLoginIdAsLong();

    // 登录了, 生成一个正常的
    final SessionUser _currentUser = Optional.ofNullable(StpUtil.getSession().getModel(Constants.SESSION_USER, SessionUser.class))
      .orElseGet(()->{
        SessionUser _sessionUser = new SessionUser();

        SaSessionBizTool.setUserAgent(_sessionUser);
//        _sessionUser.setIpaddr();
//        _sessionUser.setLoginLocation();
//        _sessionUser.setBrowser();
//        _sessionUser.setOs();

        _sessionUser.setUserId(loginIdAsLong);

        SysUserDTO sysUserDTO = SaSessionBizTool.userByUserId(loginIdAsLong);
        List<String> permissionList = SaSessionBizTool.getPermissionList(loginIdAsLong, StpUtil.getLoginType());
        _sessionUser.setDeptId(sysUserDTO.getDeptId());
//        _sessionUser.setTokenInfo();
//        _sessionUser.setTokenSession();
        _sessionUser.setPermissions(permissionList);
        _sessionUser.setUser(sysUserDTO);


        return _sessionUser;
      })
      .loadTokenInfoAndTokenSession();
    // TODO _currentUser 为空时的处理


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
