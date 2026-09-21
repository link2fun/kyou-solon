package com.github.link2fun.support.context.action;

import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.exception.ServiceException;
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
   * @return 当前已登录用户信息
   * @throws ServiceException 未登录时抛出, code 为 401
   */
  public SessionUser getCurrentUserNotNull() {
    if (Objects.isNull(sessionUser)) {
      throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
    }

    return sessionUser;
  }

  /** 判断当前用户是否是超管 */
  public boolean isSuperAdmin() {
    return Objects.nonNull(sessionUser) && Objects.nonNull(sessionUser.getUser()) && sessionUser.getUser().isSuperAdmin();
  }

  /** 获取当前用户账号名, 未登录时为空 */
  public String getUsername() {
    return Optional.ofNullable(sessionUser).map(SessionUser::getUsername).orElse(null);
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
    if (!SaSessionBizTool.isLogin()) {
      return new ActionContext();
    }

    final Long loginId = SaSessionBizTool.currentLoginId();
    final SessionUser sessionUser = SaSessionBizTool.currentUser();

    return ActionContext.builder()
      .withUserId(loginId)
      .withDeptId(sessionUser.getDeptId())
      .withSessionUser(sessionUser)
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
