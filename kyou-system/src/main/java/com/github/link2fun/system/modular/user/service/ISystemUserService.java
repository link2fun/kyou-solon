package com.github.link2fun.system.modular.user.service;

import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.req.*;

import java.util.List;

/**
 * 用户 业务层
 *
 * @author ruoyi
 */
public interface ISystemUserService {
  /**
   * 根据条件分页查询用户列表
   *
   * @param context
   * @param pageRequest 分页适配器
   * @param searchReq   用户查询条件
   * @return 用户信息集合信息
   */
  <T> Page<T> selectUserList(final ActionContext context, Page<T> pageRequest, SysUserSearchReq searchReq, Class<T> resultClass);

  /**
   * 根据条件分页查询已分配用户角色列表
   *
   * @param context
   * @param pageRequest 分页适配器
   * @param searchReq   已分配角色用户的查询条件
   * @return 用户信息集合信息
   */
  Page<AllocatedUserDTO> selectAllocatedList(final ActionContext context, Page<AllocatedUserDTO> pageRequest, SysUserSearchReq searchReq);

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param context
   * @param pageRequest 分页适配器
   * @param searchReq   未分配角色用户的查询条件
   * @return 用户信息集合信息
   */
  Page<AllocatedUserDTO> selectUnallocatedList(final ActionContext context, Page<AllocatedUserDTO> pageRequest, SysUserSearchReq searchReq);

  /**
   * 通过用户名查询用户
   *
   * @param userName 用户名
   * @return 用户对象信息; 查无此人时返回 {@code null}
   */
  SysUserDTO selectUserByUserName(String userName);


  /**
   * 通过用户ID查询用户
   *
   * @param userId 用户ID
   * @return 用户对象信息
   */
  SysUserDTO selectUserById(Long userId);

  /**
   * 根据用户ID查询用户所属角色组
   *
   * @param userName 用户名
   * @return 结果
   */
  String selectUserRoleGroup(String userName);

  /**
   * 根据用户ID查询用户所属岗位组
   *
   * @param userName 用户名
   * @return 结果
   */
  String selectUserPostGroup(String userName);

  /** 判断登录账号是否未被占用(空值视为可用), 供注册等外部流程探测 */
  boolean isUserNameAvailable(String userName);


  /**
   * 新增用户信息(内部已完成唯一性检查)
   *
   * @param user 用户信息
   * @return 结果
   */
  Long insertUser(SysUserReq.AddReq user);

  /**
   * 注册用户信息
   *
   * @param user 用户信息
   * @return 结果
   */
  boolean registerUser(SysUser user);

  /**
   * 修改用户信息(内部已完成操作权限/数据权限/唯一性检查)
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param user    用户信息
   * @return 结果
   */
  long updateUser(final ActionContext context, SysUserReq.UpdateReq user);

  /**
   * 用户授权角色(内部已完成数据权限检查)
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userId  用户ID
   * @param roleIds 角色组
   */
  void insertUserAuth(final ActionContext context, Long userId, List<Long> roleIds);

  /**
   * 修改用户状态(内部已完成操作权限/数据权限检查)
   *
   * @param context         操作上下文, 含有当前用户信息
   * @param changeStatusReq 用户ID与目标状态
   * @return 结果
   */
  long updateUserStatus(final ActionContext context, SysUserChangeStatusReq changeStatusReq);

  /**
   * 修改用户基本信息(内部已完成手机号/邮箱唯一性检查)
   *
   * @param user 用户信息
   * @return 结果
   */
  long updateUserProfile(SysUser user);

  /**
   * 修改本人资料(昵称/邮箱/手机号/性别), 成功后失效本人会话缓存
   *
   * @param userId    本人用户ID
   * @param modifyReq 个人资料修改请求
   * @return 结果
   */
  long updateMyProfile(Long userId, SysUserProfileModifyReq modifyReq);

  /**
   * 修改本人头像, 成功后失效本人会话缓存
   *
   * @param userId   本人用户ID
   * @param userName 用户名
   * @param avatar   头像地址
   * @return 结果
   */
  boolean updateMyAvatar(Long userId, String userName, String avatar);

  /**
   * 重置用户密码(内部已完成操作权限/数据权限检查与加密)
   *
   * @param context     操作上下文, 含有当前用户信息
   * @param resetPwdReq 用户ID与新密码(明文)
   * @return 结果
   */
  long resetPwd(final ActionContext context, SysUserResetPwdReq resetPwdReq);

  /**
   * 重置用户密码
   *
   * @param userName 用户名
   * @param password 密码
   * @return 结果
   */
  boolean resetUserPwd(String userName, String password);

  /**
   * 批量删除用户信息
   *
   * @param context
   * @param userIds 需要删除的用户ID
   * @return 结果
   */
  boolean deleteUserByIds(final ActionContext context, List<Long> userIds);

  /**
   * 导入用户数据
   *
   * @param context
   * @param userList        用户数据列表
   * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
   * @param operName        操作用户
   * @return 结果
   */
  String importUser(final ActionContext context, List<SysUserDTO> userList, Boolean isUpdateSupport, String operName);

  /**
   * 查询部门是否存在用户
   *
   * @param deptId 部门ID
   * @return 结果
   */
  boolean checkDeptExistUser(Long deptId);

  /** 根据用户ID查询用户 */
  SysUser getByIdNotNull(long userId);
}
