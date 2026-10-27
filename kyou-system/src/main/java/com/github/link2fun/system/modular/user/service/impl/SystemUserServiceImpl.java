package com.github.link2fun.system.modular.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.basic.api.select.Query;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.core.expression.lambda.SQLActionExpression1;
import com.easy.query.core.proxy.columns.types.SQLStringTypeColumn;
import com.easy.query.core.proxy.columns.types.impl.SQLStringTypeColumnImpl;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.framework.auth.AuthCacheInvalidator;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.easyquery.DataScope;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.PasswordUtils;
import com.github.link2fun.support.utils.bean.BeanValidators;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.dto.proxy.AllocatedUserDTOProxy;
import com.github.link2fun.system.modular.user.model.req.*;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.system.modular.usergroup.service.ISystemUserGroupService;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import com.github.link2fun.system.tool.SystemConfigContext;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;
import org.noear.solon.data.tran.TranPolicy;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Slf4j
@Component
public class SystemUserServiceImpl implements ISystemUserService {

  @Inject
  private ISystemUserService self;

  @Inject
  private ISystemRoleService roleService;

  @Inject
  private ISystemPostService postService;

  @Inject
  private ISystemUserRoleService userRoleService;

  @Inject
  private ISystemUserPostService userPostService;

  @Inject
  private ISystemUserGroupService userGroupService;

  @Inject
  private AggregateDeleter aggregateDeleter;

  @Inject
  private AuthCacheInvalidator authCacheInvalidator;

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 根据条件分页查询用户列表
   *
   * @param context     操作上下文, 含有当前用户信息
   * @param pageRequest 分页信息
   * @param searchReq   用户查询条件
   * @return 用户信息集合信息
   */
  @Override
  public <T> Page<T> selectUserList(final ActionContext context, final Page<T> pageRequest, final SysUserSearchReq searchReq, final Class<T> resultClass) {

    final DataScope dataScope = DataScope.of(entityQuery, context);

    Query<T> query = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .where(user -> {
        user.delFlag().eq(UserConstants.NORMAL);
        user.userId().eq(IdUtils.isIdValid(searchReq.getUserId()), searchReq.getUserId());
        user.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        user.nickName().like(StrUtil.isNotBlank(searchReq.getNickName()), searchReq.getNickName());
        user.phonenumber().like(StrUtil.isNotBlank(searchReq.getPhonenumber()), searchReq.getPhonenumber());
        user.email().like(StrUtil.isNotBlank(searchReq.getEmail()), searchReq.getEmail());
        user.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus());
        user.or(() -> {
          user.deptId().eq(IdUtils.isIdValid(searchReq.getDeptId()), searchReq.getDeptId());
          user.deptId().in(IdUtils.isIdValid(searchReq.getDeptId()), entityQuery.queryable(SysDept.class)
            .where(_dept -> _dept.expression().sql("find_in_set({0},ancestors)", p -> p.value(searchReq.getDeptId())))
            .selectColumn(SysDeptProxy::deptId)
          );
        });
        user.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        user.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());
        dataScope.applyTo(user, user.dept().deptId(), user.userId());
      })
      .include(SysUserProxy::dept)

      .selectAutoInclude(resultClass);
    EasyPageResult<T> pageResult = query
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize());

    return Page.of(pageRequest, pageResult);
  }

  /**
   * 根据条件分页查询已分配用户角色列表
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param searchReq 已分配角色用户的查询条件
   * @return 用户信息集合信息
   */
  @Override
  public Page<AllocatedUserDTO> selectAllocatedList(final ActionContext context, final Page<AllocatedUserDTO> pageRequest, final SysUserSearchReq searchReq) {
    final DataScope dataScope = DataScope.of(entityQuery, context);

    EasyPageResult<AllocatedUserDTO> pageResult = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .where(user -> {
        user.delFlag().eq(UserConstants.NORMAL);
        user.roles().any(role -> role.roleId().eq(searchReq.getRoleId()));
        user.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        user.phonenumber().like(StrUtil.isNotBlank(searchReq.getPhonenumber()), searchReq.getPhonenumber());
        dataScope.applyTo(user, user.dept().deptId(), user.userId());
      })
      .select(user -> new AllocatedUserDTOProxy()
        .userId().set(user.userId())
        .deptId().set(user.deptId())
        .deptName().set(user.dept().deptName())
        .userName().set(user.userName())
        .nickName().set(user.nickName())
        .email().set(user.email())
        .phonenumber().set(user.phonenumber())
        .status().set(user.status())
        .createTime().set(user.createTime()))
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize());

    return Page.of(pageRequest, pageResult);

//    return getBaseMapper().selectAllocatedList(pageRequest, searchReq);
  }

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param searchReq 未分配角色用户的查询条件
   * @return 用户信息集合信息
   */
  @Override
  public Page<AllocatedUserDTO> selectUnallocatedList(final ActionContext context, final Page<AllocatedUserDTO> pageRequest, final SysUserSearchReq searchReq) {
    final DataScope dataScope = DataScope.of(entityQuery, context);
//    return getBaseMapper().selectUnallocatedList(pageRequest, searchReq);
    EasyPageResult<AllocatedUserDTO> pageResult = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .where(user -> {
        user.delFlag().eq(UserConstants.NORMAL);
        user.roles().none(role -> role.roleId().eq(IdUtils.isIdValid(searchReq.getRoleId()), searchReq.getRoleId()));
        user.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        user.phonenumber().like(StrUtil.isNotBlank(searchReq.getPhonenumber()), searchReq.getPhonenumber());
        dataScope.applyTo(user, user.dept().deptId(), user.userId());
      })
      .select(user -> new AllocatedUserDTOProxy()
        .userId().set(user.userId())
        .deptId().set(user.deptId())
        .deptName().set(user.dept().deptName())
        .userName().set(user.userName())
        .nickName().set(user.nickName())
        .email().set(user.email())
        .phonenumber().set(user.phonenumber())
        .status().set(user.status())
        .createTime().set(user.createTime()))
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize());
    return Page.of(pageRequest, pageResult);
  }

  /**
   * 通过用户名查询用户
   *
   * @param userName 用户名
   * @return 用户对象信息; 查无此人时返回 {@code null}
   */
  @Override
  public SysUserDTO selectUserByUserName(final String userName) {

    return self.getSysUserDTOQuery(user -> {
      user.userName().eq(userName);
      user.delFlag().eq(UserConstants.NORMAL);
    }).singleOrNull();
  }


  /** 构造已预加载部门与角色的用户 DTO 查询(角色仅取启用状态) */
  @Override
  public Query<SysUserDTO> getSysUserDTOQuery(SQLActionExpression1<SysUserProxy> whereExpression) {
    return entityQuery.queryable(SysUser.class)
      .where(whereExpression)
      .include(SysUserProxy::dept)
      .include(SysUserProxy::roles, roleQuery -> {
        // 停用角色不参与授权, 预加载时即过滤
        roleQuery.where(role -> role.status().eq(UserConstants.NORMAL));
      })
      .selectAutoInclude(SysUserDTO.class);
  }

  /**
   * 通过用户ID查询用户
   *
   * @param userId 用户ID
   * @return 用户对象信息
   */
  @Override
  public SysUserDTO selectUserById(final Long userId) {

//    return getBaseMapper().selectUserByUserId(userId);
    return self.getSysUserDTOQuery(user -> {
      user.userId().eq(userId);
      user.delFlag().eq(UserConstants.NORMAL);
    }).singleNotNull();
  }

  /**
   * 根据用户ID查询用户所属角色组
   *
   * @param userName 用户名
   * @return 结果
   */
  @Override
  public String selectUserRoleGroup(final String userName) {

//    final Long userId = lambdaQuery().select(SysUser::getUserId)
//      .eq(SysUser::getUserName, userName)
//      .oneOpt()
//      .map(SysUser::getUserId).orElse(-1L);
    final Long userId = requireUserIdByUserName(userName);

    List<Long> roleIdList = userRoleService.findRoleIdsByUserId(userId);

    if (CollectionUtil.isEmpty(roleIdList)) {
      return StrUtil.EMPTY;
    }

    return roleService.listByIds(roleIdList).stream().map(SysRole::getRoleName).collect(Collectors.joining(","));
  }

  /**
   * 根据用户ID查询用户所属岗位组
   *
   * @param userName 用户名
   * @return 结果
   */
  @Override
  public String selectUserPostGroup(final String userName) {
    final Long userId = requireUserIdByUserName(userName);
    final List<Long> postIdList = userPostService.findPostIdsByUserId(userId);
    if (CollectionUtil.isEmpty(postIdList)) {
      return StrUtil.EMPTY;
    }
    return postService.listByIds(postIdList).stream().map(SysPost::getPostName).collect(Collectors.joining(","));
  }

  /** 按用户名取用户ID, 查无此人或已软删时抛业务异常 */
  private Long requireUserIdByUserName(final String userName) {
    final Long userId = self.findUserIdByUserName(userName);
    if (ObjectUtil.isNull(userId)) {
      throw new ServiceException("用户不存在");
    }
    return userId;
  }


  /** 探测某列值的占用行, 空值不参与判重, 已过滤软删行 */
  private List<Long> findOccupantIds(final SQLStringTypeColumn<SysUserProxy> column, final String columnValue) {
    if (StrUtil.isBlank(columnValue)) {
      // 空字符串默认算不重复, 不参与判重
      return List.of();
    }

    return entityQuery.queryable(SysUser.class)
      .where(user -> {
        new SQLStringTypeColumnImpl<>(user.getEntitySQLContext(), user.getTable(), column.getValue()).eq(columnValue);
        user.delFlag().eq(UserConstants.NORMAL);
      })
      .selectColumn(SysUserProxy::userId)
      .toList();
  }


  /**
   * 校验用户是否允许操作
   *
   * @param userId 用户信息
   */
  private void checkUserAllowed(final Long userId) {
    if (ObjectUtil.isNotNull(userId) && SysUser.isSuperAdmin(userId)) {
      throw new ServiceException("不允许操作超级管理员用户");
    }
  }

  /**
   * 校验用户是否有数据权限
   * <p>仅用于查询类接口(getInfo)的越权防护; 编辑类操作的前置检查已在各执行方法内部完成。
   * 取目标用户的部门后按数据范围点查, 不再借道用户列表查询</p>
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userId  用户id
   */
  @Override
  public void checkUserDataScope(final ActionContext context, final Long userId) {
    if (context.isSuperAdmin()) {
      return;
    }
    final Long targetDeptId = entityQuery.queryable(SysUser.class)
      .where(user -> {
        user.delFlag().eq(UserConstants.NORMAL);
        user.userId().eq(userId);
      })
      .selectColumn(SysUserProxy::deptId)
      .firstOrNull();
    if (!DataScope.of(entityQuery, context).containsUser(userId, targetDeptId)) {
      throw new ServiceException("没有权限访问用户数据！");
    }
  }


  /** 判断登录账号是否未被占用(空值视为可用), 供注册等外部流程探测 */
  @Override
  public boolean isUserNameAvailable(final String userName) {
    return UniqueChecker.available(findOccupantIds(SysUserProxy.TABLE.userName(), userName), null);
  }

  private void checkUserFieldUnique(final String action, final String userName, final String phonenumber,
                                    final String email, final Long userId) {
    final String prefix = action + "用户'" + userName + "'失败，";
    UniqueChecker.checkOrThrow(findOccupantIds(SysUserProxy.TABLE.userName(), userName),
      userId, prefix + "登录账号已存在");
    UniqueChecker.checkOrThrow(findOccupantIds(SysUserProxy.TABLE.phonenumber(), phonenumber),
      userId, prefix + "手机号码已存在");
    UniqueChecker.checkOrThrow(findOccupantIds(SysUserProxy.TABLE.email(), email),
      userId, prefix + "邮箱账号已存在");
  }

  /**
   * 新增用户信息
   *
   * @param addReq 新增用户请求
   * @return 结果
   */
  @Transaction
  @Override
  public Long insertUser(final SysUserReq.AddReq addReq) {
    checkUserFieldUnique("新增", addReq.getUserName(), addReq.getPhonenumber(), addReq.getEmail(), null);

    SysUser user = addReq.transferTo(SysUser.class);

    user.setPassword(PasswordUtils.encryptPassword(addReq.getPassword())); //
    user.setStatus(UserConstants.NORMAL); // 状态为正常
    user.setDelFlag(UserConstants.NORMAL); // 删除状态为未删除
    user.setLoginIp("");
    user.setLoginDate(null);


//    getBaseMapper().insert(user);
    entityQuery.insertable(user)
      .executeRows(true);
    Long userId = user.getUserId();

    userPostService.reassignPosts(userId, addReq.getPostIds());
    userRoleService.reassignRoles(userId, addReq.getRoleIds());
    return userId;
  }

  /**
   * 注册用户信息
   *
   * @param user 用户信息
   * @return 结果
   */
  @Override
  public boolean registerUser(final SysUser user) {
//    return getBaseMapper().insert(user) > 0;
    return entityQuery.insertable(user)
      .executeRows() > 0;
  }

  /**
   * 修改用户信息
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param updateReq 用户信息
   * @return 结果
   */
  @Transaction
  @Override
  public long updateUser(final ActionContext context, final SysUserReq.UpdateReq updateReq) {
    Long userId = updateReq.getUserId();
    checkUserAllowed(userId);
    checkUserDataScope(context, userId);
    checkUserFieldUnique("修改", updateReq.getUserName(), updateReq.getPhonenumber(), updateReq.getEmail(), userId);

    // 整行编辑提交, 状态与部门为必填, 缺失视为非法请求
    if (StrUtil.isBlank(updateReq.getStatus())) {
      throw new ServiceException("修改用户'" + updateReq.getUserName() + "'失败，状态不能为空");
    }
    if (ObjectUtil.isNull(updateReq.getDeptId())) {
      throw new ServiceException("修改用户'" + updateReq.getUserName() + "'失败，部门不能为空");
    }

//    final SysUser sysUser = getBaseMapper().selectById(userId);
    final SysUser sysUser = entityQuery.queryable(SysUser.class)
      .whereById(userId)
      .singleNotNull();


    // null 表示本次未提交角色/岗位信息, 跳过重分配; 空集合表示显式清空
    if (ObjectUtil.isNotNull(updateReq.getRoleIds())) {
      userRoleService.reassignRoles(userId, updateReq.getRoleIds());
    }

    if (ObjectUtil.isNotNull(updateReq.getPostIds())) {
      userPostService.reassignPosts(userId, updateReq.getPostIds());
    }

    BeanUtil.copyProperties(updateReq, sysUser);

//    return getBaseMapper().updateById(sysUser);
    final long rows = entityQuery.updatable(sysUser)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
    authCacheInvalidator.onUsersChanged(List.of(userId));
    return rows;
  }

  /**
   * 用户授权角色
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userId  用户ID
   * @param roleIds 角色组
   */
  @Transaction(policy = TranPolicy.required)
  @Override
  public void insertUserAuth(final ActionContext context, final Long userId, final List<Long> roleIds) {
    checkUserDataScope(context, userId);
    userRoleService.reassignRoles(userId, roleIds);
    authCacheInvalidator.onUsersChanged(List.of(userId));
  }

  /**
   * 修改用户状态
   *
   * @param context         操作上下文, 含有当前用户信息
   * @param changeStatusReq 修改用户状态请求
   * @return 结果
   */
  @Transaction
  @Override
  public long updateUserStatus(final ActionContext context, final SysUserChangeStatusReq changeStatusReq) {
    final Long userId = changeStatusReq.getUserId();
    final String status = changeStatusReq.getStatus();
    checkUserAllowed(userId);
    checkUserDataScope(context, userId);
    final long rows = entityQuery.updatable(SysUser.class)
      .where(user -> user.userId().eq(userId))
      .setColumns(user -> user.status().set(status))
      .executeRows();
    if (UserConstants.USER_DISABLE.equals(status)) {
      authCacheInvalidator.onUsersKicked(List.of(userId));
    } else {
      authCacheInvalidator.onUsersChanged(List.of(userId));
    }
    return rows;
  }

  /**
   * 修改用户基本信息(个人中心)
   *
   * @param user 用户信息
   * @return 结果
   */
  @Transaction
  @Override
  public long updateUserProfile(final SysUser user) {
    checkUserFieldUnique("修改", user.getUserName(), user.getPhonenumber(), user.getEmail(), user.getUserId());
//    return getBaseMapper().updateById(user);
    return entityQuery.updatable(user)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
  }

  /**
   * 修改本人资料(个人中心), 成功后失效本人会话缓存
   */
  @Transaction
  @Override
  public long updateMyProfile(final Long userId, final SysUserProfileModifyReq modifyReq) {
    final SysUser user = getByIdNotNull(userId);
    user.setNickName(modifyReq.getNickName());
    user.setEmail(modifyReq.getEmail());
    user.setPhonenumber(modifyReq.getPhonenumber());
    user.setSex(modifyReq.getSex());
    final long rows = updateUserProfile(user);
    authCacheInvalidator.onUsersChanged(List.of(userId));
    return rows;
  }

  /**
   * 修改用户头像
   *
   * @param userName 用户名
   * @param avatar   头像地址
   * @return 结果
   */
  @Override
  public boolean updateUserAvatar(final String userName, final String avatar) {

    return entityQuery.updatable(SysUser.class)
      .setColumns(user -> user.avatar().set(avatar))
      .where(user -> user.userName().eq(userName))
      .executeRows() > 0;
  }

  /**
   * 修改本人头像, 成功后失效本人会话缓存
   */
  @Override
  public boolean updateMyAvatar(final Long userId, final String userName, final String avatar) {
    final boolean updated = updateUserAvatar(userName, avatar);
    authCacheInvalidator.onUsersChanged(List.of(userId));
    return updated;
  }

  /**
   * 重置用户密码
   *
   * @param context     操作上下文, 含有当前用户信息
   * @param resetPwdReq 重置密码请求
   * @return 结果
   */
  @Transaction
  @Override
  public long resetPwd(final ActionContext context, final SysUserResetPwdReq resetPwdReq) {
    final Long userId = resetPwdReq.getUserId();
    checkUserAllowed(userId);
    checkUserDataScope(context, userId);
    final long rows = entityQuery.updatable(SysUser.class)
      .where(userProxy -> userProxy.userId().eq(userId))
      .setColumns(userProxy -> userProxy.password().set(PasswordUtils.encryptPassword(resetPwdReq.getPassword())))
      // TODO 用户管理应该加上乐观锁
      .executeRows();
    authCacheInvalidator.onUsersKicked(List.of(userId));
    return rows;
  }

  /**
   * 重置用户密码
   *
   * @param userName 用户名
   * @param password 密码
   * @return 结果
   */
  @Override
  public boolean resetUserPwd(final String userName, final String password) {
    final boolean updated = entityQuery.updatable(SysUser.class)
      .setColumns(user -> user.password().set(password))
      .where(user -> user.userName().eq(userName))
      .executeRows() > 0;
    // 查无有效用户(如已软删)时无从强退会话
    final Long userId = self.findUserIdByUserName(userName);
    if (ObjectUtil.isNotNull(userId)) {
      authCacheInvalidator.onUsersKicked(List.of(userId));
    }
    return updated;
  }

  /**
   * 批量删除用户信息
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userIds 需要删除的用户ID
   * @return 结果
   */
  @Transaction
  @Override
  public boolean deleteUserByIds(final ActionContext context, final List<Long> userIds) {

    final long rows = aggregateDeleter.delete(AggregateDeletePlan.of(userIds)
      .guard(ids -> {
        if (CollectionUtil.contains(ids, context.getUserId())) {
          throw new ServiceException("当前用户不能删除");
        }
      })
      .guardPerId(userId -> {
        checkUserAllowed(userId);
        checkUserDataScope(context, userId);
      })
      // 删除用户与角色关联
      .unlink(userRoleService::removeByUserIds)
      // 删除用户与岗位关联
      .unlink(userPostService::removeByUserIds)
      // 删除用户与群组关联
      .unlink(userGroupService::removeByUserIds)
      // 软删: 置删除标记
      .delete(ids -> entityQuery.updatable(SysUser.class)
        .setColumns(user -> user.delFlag().set(UserConstants.DELETED))
        .where(user -> user.userId().in(ids))
        .executeRows()));
    authCacheInvalidator.onUsersKicked(userIds);
    return rows > 0;
  }

  /**
   * 导入用户数据
   *
   * @param context         操作上下文, 含有当前用户信息
   * @param userDTOList     用户数据列表
   * @param isUpdateSupport 是否更新支持，如果已存在，则进行更新数据
   * @param operName        操作用户
   * @return 结果
   */
  @Override
  public String importUser(final ActionContext context, final List<SysUserDTO> userDTOList, final Boolean isUpdateSupport, final String operName) {
    if (ObjectUtil.isNull(userDTOList) || userDTOList.isEmpty()) {
      throw new ServiceException("导入用户数据不能为空！");
    }

    List<SysUser> userList = userDTOList.stream().map(userDTO -> BeanUtil.copyProperties(userDTO, SysUser.class)).toList();

    int successNum = 0;
    int failureNum = 0;
    StringBuilder successMsg = new StringBuilder();
    StringBuilder failureMsg = new StringBuilder();
    String password = SystemConfigContext.userInitPassword();
    for (SysUser user : userList) {
      try {
        // 验证是否存在这个用户
        SysUserDTO u = self.selectUserByUserName(user.getUserName());
        if (ObjectUtil.isNull(u)) {
          BeanValidators.validateWithException(user);
          user.setPassword(PasswordUtils.encryptPassword(password));
          user.setCreateBy(operName);
//          getBaseMapper().insert(user);
          entityQuery.insertable(user).executeRows();
          successNum++;
          successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 导入成功");
        } else if (isUpdateSupport) {
          BeanValidators.validateWithException(user);
          checkUserAllowed(u.getUserId());
          checkUserDataScope(context, u.getUserId());
          user.setUserId(u.getUserId());
          user.setUpdateBy(operName);
//          getBaseMapper().updateById(user);
          entityQuery.updatable(user).executeRows();
          // 导入会改部门/状态等会话载荷字段, 在线用户须重建
          authCacheInvalidator.onUsersChanged(List.of(u.getUserId()));

          successNum++;
          successMsg.append("<br/>").append(successNum).append("、账号 ").append(user.getUserName()).append(" 更新成功");
        } else {
          failureNum++;
          failureMsg.append("<br/>").append(failureNum).append("、账号 ").append(user.getUserName()).append(" 已存在");
        }
      } catch (Exception e) {
        failureNum++;
        String msg = "<br/>" + failureNum + "、账号 " + user.getUserName() + " 导入失败：";
        failureMsg.append(msg).append(e.getMessage());
        log.error(msg, e);
      }
    }
    if (failureNum > 0) {
      failureMsg.insert(0, "很抱歉，导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
      throw new ServiceException(failureMsg.toString());
    } else {
      successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
    }
    return successMsg.toString();

  }

  /**
   * 查询部门是否存在用户
   *
   * @param deptId 部门ID
   * @return 结果
   */
  @Override
  public boolean checkDeptExistUser(final Long deptId) {
//    return self.exists(
//      query().eq(SysUser::getDeptId, deptId)
//        .eq(SysUser::getStatus, UserConstants.NORMAL)
//        .limit(1));
//    return lambdaQuery().eq(SysUser::getDeptId, deptId)
//      .eq(SysUser::getStatus, UserConstants.NORMAL)
//      .exists();
    return entityQuery.queryable(SysUser.class)
      .where(user -> {
        user.deptId().eq(deptId);
        user.status().eq(UserConstants.NORMAL);
      })
      .any();
  }

  /**
   * 根据用户名称查询用户ID
   *
   * @param userName 用户名称
   * @return 用户ID; 查无此人或已软删时返回 {@code null}, 同名多行视为数据错误直接抛出
   */
  @Override
  public Long findUserIdByUserName(final String userName) {
//    return lambdaQuery().select(SysUser::getUserId)
//      .eq(SysUser::getUserName, userName)
//      .oneOpt().map(SysUser::getUserId)
//      .orElse(-1L);
    return entityQuery.queryable(SysUser.class)
      .where(user -> {
        user.userName().eq(userName);
        user.delFlag().eq(UserConstants.NORMAL);
      })
      .select(SysUserProxy::userId)
      .singleOptional()
      .orElse(null);
  }

  /** 根据用户ID查询用户 */
  @Override
  public SysUser getByIdNotNull(long userId) {
    return entityQuery.queryable(SysUser.class).whereById(userId).singleNotNull();
  }
}
