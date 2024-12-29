package com.github.link2fun.system.modular.user.service;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.easy.query.core.basic.api.select.Query;
import com.easy.query.core.expression.lambda.SQLExpression2;
import com.easy.query.core.proxy.columns.types.SQLStringTypeColumn;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;

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
   * @param page      分页适配器
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  <T> Page<T> selectUserList(final ActionContext context, Page<T> page, SysUser searchReq, Class<T> resultClass);

  /**
   * 根据条件分页查询已分配用户角色列表
   *
   * @param context
   * @param page      分页适配器
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  Page<AllocatedUserDTO> selectAllocatedList(final ActionContext context, Page<AllocatedUserDTO> page, SysUser searchReq);

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param context
   * @param page      分页适配器
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  Page<AllocatedUserDTO> selectUnallocatedList(final ActionContext context, Page<AllocatedUserDTO> page, SysUser searchReq);

  /**
   * 通过用户名查询用户
   *
   * @param userName 用户名
   * @return 用户对象信息
   */
  SysUserDTO selectUserByUserName(String userName);


  Query<SysUserDTO> getSysUserDTOQuery(SQLExpression2<SysUserProxy, SysDeptProxy> whereExpression);

  /** 根据用户 tokenInfo 查询登录用户信息 */
  SessionUser selectCurrentUserByTokenInfo(SaTokenInfo tokenInfo);

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


  /**
   * 判断某一列的值是否是唯一的
   *
   * @param column      列
   * @param columnValue 列的值
   * @param userId      用户id, 会排除当前用户
   * @return 结果
   */
  boolean isColumnValueUnique(SQLStringTypeColumn<SysUserProxy> column, String columnValue, final Long userId);

  /**
   * 校验用户是否允许操作
   *
   * @param userId 用户信息
   */
  void checkUserAllowed(Long userId);

  /**
   * 校验用户是否有数据权限
   *
   * @param context
   * @param userId  用户id
   */
  void checkUserDataScope(final ActionContext context, Long userId);

  /**
   * 新增用户信息
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
   * 修改用户信息
   *
   * @param user 用户信息
   * @return 结果
   */
  long updateUser(SysUserReq.UpdateReq user);

  /**
   * 用户授权角色
   *
   * @param userId  用户ID
   * @param roleIds 角色组
   */
  void insertUserAuth(Long userId, List<Long> roleIds);

  /**
   * 修改用户状态
   *
   * @param user 用户信息
   * @return 结果
   */
  long updateUserStatus(SysUser user);

  /**
   * 修改用户基本信息
   *
   * @param user 用户信息
   * @return 结果
   */
  long updateUserProfile(SysUser user);

  /**
   * 修改用户头像
   *
   * @param userName 用户名
   * @param avatar   头像地址
   * @return 结果
   */
  boolean updateUserAvatar(String userName, String avatar);

  /**
   * 重置用户密码
   *
   * @param user 用户信息
   * @return 结果
   */
  long resetPwd(SysUser user);

  /**
   * 重置用户密码
   *
   * @param userName 用户名
   * @param password 密码
   * @return 结果
   */
  boolean resetUserPwd(String userName, String password);

  /**
   * 通过用户ID删除用户
   *
   * @param userId 用户ID
   * @return 结果
   */
  boolean deleteUserById(Long userId);

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

  /**
   * 根据用户名称查询用户ID
   *
   * @param userName 用户名称
   * @return 用户ID
   */
  Long findUserIdByUserName(String userName);

  SysUser getById(long userId);
}
