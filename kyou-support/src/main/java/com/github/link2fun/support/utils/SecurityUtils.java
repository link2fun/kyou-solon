package com.github.link2fun.support.utils;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaStrategy;
import com.github.link2fun.framework.password.PasswordEncoder;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.HttpStatus;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.uuid.IdUtils;
import org.noear.solon.Solon;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 安全服务工具类
 *
 * @author ruoyi
 */
@SuppressWarnings("unused")
public class SecurityUtils {

  /**
   * 用户ID
   **/
  public static Long getUserId() {
    try {
      return currentUser().getUserId();
    } catch (Exception e) {
      throw new ServiceException("获取用户ID异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取部门ID
   **/
  public static Long getDeptId() {
    try {
      return currentUser().getDeptId();
    } catch (Exception e) {
      throw new ServiceException("获取部门ID异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取用户账户
   **/
  public static String getUsername() {
    try {
      return currentUser().getUsername();
    } catch (Exception e) {
      throw new ServiceException("获取用户账户异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取用户
   **/
  public static SessionUser currentUser() {
    try {
      return StpUtil.getSession().getModel(Constants.SESSION_USER, SessionUser.class);
    } catch (Exception e) {
      throw new ServiceException("获取用户信息异常", HttpStatus.UNAUTHORIZED);
    }
  }

  /**
   * 获取Authentication
   */
  public static SaTokenInfo getAuthentication() {
    return StpUtil.getTokenInfo();
  }

  /**
   * 生成BCryptPasswordEncoder密码
   *
   * @param password 密码
   * @return 加密字符串
   */
  public static String encryptPassword(String password) {
    PasswordEncoder passwordEncoder = Solon.context().getBean(PasswordEncoder.class);
    return passwordEncoder.encode(password);
  }

  /**
   * 判断密码是否相同
   *
   * @param rawPassword     真实密码
   * @param encodedPassword 加密后字符
   * @return 结果
   */
  public static boolean matchesPassword(String rawPassword, String encodedPassword) {
    PasswordEncoder passwordEncoder = Solon.context().getBean(PasswordEncoder.class);
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  /**
   * 是否为管理员
   *
   * @param userId 用户ID
   * @return 结果
   */
  public static boolean isAdmin(Long userId) {
    return IdUtils.isIdValid(userId) && 1L == userId;
  }

  /**
   * 验证用户是否具备某权限
   *
   * @param permission 权限字符串
   * @return 用户是否具备某权限
   */
  public static boolean hasPermission(String permission) {
    return hasPermission(currentUser().getPermissions(), permission);
  }

  /**
   * 判断是否包含权限
   *
   * @param authorities 权限列表
   * @param permission  权限字符串
   * @return 用户是否具备某权限
   */
  public static boolean hasPermission(Collection<String> authorities, String permission) {


    return authorities.stream().filter(StringUtils::hasText)
      .anyMatch(x -> Constants.ALL_PERMISSION.equals(x) || SaStrategy.instance.hasElement.apply(List.of(x), permission));
  }

  /**
   * 验证用户是否拥有某个角色
   *
   * @param role 角色标识
   * @return 用户是否具备某角色
   */
  public static boolean hasRole(String role) {
    List<RoleDTO> roleList = currentUser().getUser().getRoles();
    Collection<String> roles = roleList.stream().map(RoleDTO::getRoleKey).collect(Collectors.toSet());
    return hasRole(roles, role);
  }

  /**
   * 判断是否包含角色
   *
   * @param roles 角色列表
   * @param role  角色
   * @return 用户是否具备某角色权限
   */
  public static boolean hasRole(Collection<String> roles, String role) {
    return roles.stream().filter(StringUtils::hasText)
      .anyMatch(x -> Constants.SUPER_ADMIN.equals(x) || SaStrategy.instance.hasElement.apply(List.of(x), role));
  }

}
