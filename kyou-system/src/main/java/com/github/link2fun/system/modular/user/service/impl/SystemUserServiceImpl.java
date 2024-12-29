package com.github.link2fun.system.modular.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.base.LongProxy;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.basic.api.select.Query;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.core.expression.lambda.SQLExpression2;
import com.easy.query.core.proxy.columns.types.SQLStringTypeColumn;
import com.easy.query.core.proxy.columns.types.impl.SQLStringTypeColumnImpl;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.annotation.DataScope;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.dto.SysUserDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.proxy.SysDeptProxy;
import com.github.link2fun.support.core.domain.entity.proxy.SysUserProxy;
import com.github.link2fun.support.core.domain.model.SessionUser;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.bean.BeanValidators;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.support.core.domain.entity.SysPost;
import com.github.link2fun.system.modular.post.service.ISystemPostService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.user.model.dto.AllocatedUserDTO;
import com.github.link2fun.system.modular.user.model.dto.proxy.AllocatedUserDTOProxy;
import com.github.link2fun.system.modular.user.model.req.SysUserReq;
import com.github.link2fun.system.modular.user.service.ISystemUserService;
import com.github.link2fun.system.modular.userpost.service.ISystemUserPostService;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import com.github.link2fun.system.tool.SystemConfigContext;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.handle.Result;
import org.noear.solon.core.util.DataThrowable;
import org.noear.solon.data.annotation.Tran;
import org.noear.solon.data.tran.TranPolicy;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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


  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 根据条件分页查询用户列表
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param page      分页信息
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  @Override
  @DataScope(deptAlias = SysDept.TABLE_ALIAS, userAlias = SysUser.TABLE_ALIAS)
  public <T> Page<T> selectUserList(final ActionContext context, final Page<T> page, final SysUser searchReq, final Class<T> resultClass) {

    Query<T> query = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId()))
      .where((user, dept) -> {
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
        if (StrUtil.isNotBlank(searchReq.getParams().getDataScope())) {
          user.expression().sql(searchReq.getParams().getDataScope());
        }
      })
      .include(SysUserProxy::dept)

      .selectAutoInclude(resultClass);
    EasyPageResult<T> pageResult = query
      .toPageResult(page.getPageNum(), page.getPageSize(), page.getTotal());

    return Page.of(pageResult);
  }

  /**
   * 根据条件分页查询已分配用户角色列表
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  @Override
  @DataScope(deptAlias = SysDept.TABLE_ALIAS, userAlias = SysUser.TABLE_ALIAS)
  public Page<AllocatedUserDTO> selectAllocatedList(final ActionContext context, final Page<AllocatedUserDTO> page, final SysUser searchReq) {
    EasyPageResult<AllocatedUserDTO> pageResult = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId())).asAlias(SysDept.TABLE_ALIAS)
      .leftJoin(SysUserRole.class, (user, dept, userRole) -> user.userId().eq(userRole.userId()))
      .leftJoin(SysRole.class, (user, dept, userRole, role) -> userRole.roleId().eq(role.roleId()))

      .where((user, dept, userRole, role) -> {
        user.delFlag().eq(UserConstants.NORMAL);
        role.roleId().eq(searchReq.getRoleId());
        user.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        user.phonenumber().like(StrUtil.isNotBlank(searchReq.getPhonenumber()), searchReq.getPhonenumber());
        if (StrUtil.isNotBlank(searchReq.getParams().getDataScope())) {
          user.expression().sql(searchReq.getParams().getDataScope());
        }
      })
      // FIXME 这里的分页查询有问题， count(distinct *) 会导致分页查询出现问题
      .select((user, dept, userRole, role) -> new AllocatedUserDTOProxy()
        .userId().set(user.userId())
        .deptId().set(user.deptId())
        .deptName().set(dept.deptName())
        .userName().set(user.userName())
        .nickName().set(user.nickName())
        .email().set(user.email())
        .phonenumber().set(user.phonenumber())
        .status().set(user.status())
        .createTime().set(user.createTime()))
      .distinct()
      .toPageResult(page.getPageNum(), page.getPageSize());

    return Page.of(pageResult);

//    return getBaseMapper().selectAllocatedList(page, searchReq);
  }

  /**
   * 根据条件分页查询未分配用户角色列表
   *
   * @param context   操作上下文, 含有当前用户信息
   * @param searchReq 用户信息
   * @return 用户信息集合信息
   */
  @Override
  @DataScope(deptAlias = SysDept.TABLE_ALIAS, userAlias = SysUser.TABLE_ALIAS)
  public Page<AllocatedUserDTO> selectUnallocatedList(final ActionContext context, final Page<AllocatedUserDTO> page, final SysUser searchReq) {
//    return getBaseMapper().selectUnallocatedList(page, searchReq);
    //select distinct user.user_id     as userId,
    //                      user.dept_id     as deptId,
    //                      user.user_name   as userName,
    //                      user.nick_name   as nickName,
    //                      user.email,
    //                      user.phonenumber,
    //                      user.status,
    //                      user.create_time as createTime
    //      from sys_user user
    //               left join sys_dept dept on user.dept_id = dept.dept_id
    //               left join sys_user_role user_dept on user.user_id = user_dept.user_id
    //               left join sys_role role on role.role_id = user_dept.role_id
    //      where user.del_flag = '0'
    //        and (role.role_id != #{searchReq.roleId} or role.role_id IS NULL)
    //        and user.user_id not in (select user.user_id
    //                                 from sys_user u
    //                                          inner join sys_user_role ur
    //                                                     on user.user_id = user_dept.user_id and user_dept.role_id = #{searchReq.roleId})
    //      <if test="searchReq.userName != null and searchReq.userName != ''">
    //          AND user.user_name like concat('%', #{searchReq.userName}, '%')
    //      </if>
    //      <if test="searchReq.phonenumber != null and searchReq.phonenumber != ''">
    //          AND user.phonenumber like concat('%', #{searchReq.phonenumber}, '%')
    //      </if>
    //      <!-- 数据范围过滤 -->
    //      ${searchReq.params.dataScope}
    EasyPageResult<AllocatedUserDTO> pageResult = entityQuery.queryable(SysUser.class).asAlias(SysUser.TABLE_ALIAS)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId())).asAlias(SysDept.TABLE_ALIAS)
      .leftJoin(SysUserRole.class, (user, dept, userRole) -> user.userId().eq(userRole.userId()))
      .leftJoin(SysRole.class, (user, dept, userRole, role) -> userRole.roleId().eq(role.roleId()))
      .where((user, dept, userRole, role) -> {
        user.delFlag().eq(UserConstants.NORMAL);
        role.or(() -> {
          role.roleId().ne(searchReq.getRoleId());
          role.roleId().isNull();
        });
        user.userId().notIn(IdUtils.isIdValid(searchReq.getRoleId()),
          entityQuery.queryable(SysUser.class)
            .innerJoin(SysUserRole.class, (u, ur) -> {
              u.userId().eq(ur.userId());
              ur.roleId().eq(searchReq.getRoleId());
            })
            .select((u, ur) -> new LongProxy(u.userId())));

        user.userName().like(StrUtil.isNotBlank(searchReq.getUserName()), searchReq.getUserName());
        user.phonenumber().like(StrUtil.isNotBlank(searchReq.getPhonenumber()), searchReq.getPhonenumber());
        if (StrUtil.isNotBlank(searchReq.getParams().getDataScope())) {
          user.expression().sql(searchReq.getParams().getDataScope());
        }
      })

      .select((user, dept, userRole, role) -> new AllocatedUserDTOProxy()
        .userId().set(user.userId())
        .deptId().set(user.deptId())
        .deptName().set(dept.deptName())
        .userName().set(user.userName())
        .nickName().set(user.nickName())
        .email().set(user.email())
        .phonenumber().set(user.phonenumber())
        .status().set(user.status())
        .createTime().set(user.createTime()))
      .distinct()
      .toPageResult(page.getPageNum(), page.getPageSize());
    return Page.of(pageResult);
  }

  /**
   * 通过用户名查询用户
   *
   * @param userName 用户名
   * @return 用户对象信息
   */
  @Override
  public SysUserDTO selectUserByUserName(final String userName) {

    return self.getSysUserDTOQuery((user, dept) -> {
      user.userName().eq(userName);
      user.delFlag().eq(UserConstants.NORMAL);
    }).singleNotNull(() -> new DataThrowable("用户信息不存在").data(Result.failure("用户信息不存在")));
  }


  @Override
  public Query<SysUserDTO> getSysUserDTOQuery(SQLExpression2<SysUserProxy, SysDeptProxy> whereExpression) {
    return entityQuery.queryable(SysUser.class)
      .leftJoin(SysDept.class, (user, dept) -> user.deptId().eq(dept.deptId()))
      .where(whereExpression)
      .include(SysUserProxy::dept)
      .includes(SysUserProxy::roles)
      .selectAutoInclude(SysUserDTO.class);
  }

  /**
   * 根据用户 tokenInfo 查询登录用户信息
   *
   * @param tokenInfo token信息
   */
  @Override
  public SessionUser selectCurrentUserByTokenInfo(final SaTokenInfo tokenInfo) {
    final Object loginId = tokenInfo.getLoginId();
    final Long userId = Convert.toLong(loginId);
    final SessionUser currentUser = SaSessionBizTool.getCurrentUser();
    final SysUser user = entityQuery.queryable(SysUser.class).whereById(userId).singleNotNull();
    currentUser.setUserId(user.getUserId());
    currentUser.setDeptId(user.getDeptId());
    currentUser.setTokenInfo(tokenInfo);
//    currentUser.setLoginTime();
//    currentUser.setExpireTime();
//    currentUser.setIpaddr();
//    currentUser.setLoginLocation();
//    currentUser.setBrowser();
//    currentUser.setOs();

    final SysUserDTO userDTO = BeanUtil.copyProperties(user, SysUserDTO.class);

    final List<RoleDTO> roleList = SaSessionBizTool.getRole(loginId, tokenInfo.getLoginType());
    userDTO.setRoles(roleList);

    final List<String> permissionList = SaSessionBizTool.getPermissionList(loginId, tokenInfo.getLoginType());

    currentUser.setPermissions(permissionList);
    currentUser.setUser(userDTO);

    SaSessionBizTool.setCurrentUser(currentUser);

    return currentUser;
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
    return self.getSysUserDTOQuery((user, dept) -> {
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
    Long userId = Optional.ofNullable(entityQuery.queryable(SysUser.class)
        .where(user -> user.userName().eq(userName))
        .select(user -> new LongProxy(user.userId()))
        .singleOrNull())
      .orElse(-1L);

    List<Long> roleIdList = userRoleService.findRoleIdListByUserId(userId);

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
    final Long userId = self.findUserIdByUserName(userName);
    final List<Long> postIdList = userPostService.findPostIdListByUserId(userId);
    if (CollectionUtil.isEmpty(postIdList)) {
      return StrUtil.EMPTY;
    }
    return postService.listByIds(postIdList).stream().map(SysPost::getPostName).collect(Collectors.joining(","));
  }



  @Override
  public boolean isColumnValueUnique(SQLStringTypeColumn<SysUserProxy> column, final String columnValue, final Long userId) {
    if (StrUtil.isBlank(columnValue)) {
      // 当前要进行判断的值 是 空字符串, 默认情况下, 判重不进行空字符串的判重, 空字符串默认算是不重复的
      return true;
    }


    final List<Long> userIdList = entityQuery.queryable(SysUser.class)
      .where(user -> {
        // 采用动态拼接
        new SQLStringTypeColumnImpl<>(user.getEntitySQLContext(), user.getTable(), column.getValue()).eq(columnValue);
        user.delFlag().eq(UserConstants.NORMAL);
      })
      .selectColumn(SysUserProxy::userId)
      .toList();

    if (CollectionUtil.size(userIdList) > 1) {
      // 当有两个用户的手机号一致的时候, 肯定算是重复了
      return false;
    }


    // 如果只有一个用户, 判断是否是当前用户, 当包含时,说明 数据跟当前用户(传入的用户)重复, 即自己跟自己一样
    return CollectionUtil.contains(userIdList, userId);
  }


  /**
   * 校验用户是否允许操作
   *
   * @param userId 用户信息
   */
  @Override
  public void checkUserAllowed(final Long userId) {
    if (StringUtils.isNotNull(userId) && SysUser.isAdmin(userId)) {
      throw new ServiceException("不允许操作超级管理员用户");
    }
  }

  /**
   * 校验用户是否有数据权限
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userId  用户id
   */
  @Override
  public void checkUserDataScope(final ActionContext context, final Long userId) {
    if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
      SysUser user = new SysUser();
      user.setUserId(userId);
      Page<SysUserDTO> users = self.selectUserList(context, Page.ofLimit(1L), user, SysUserDTO.class);
      if (CollectionUtil.isEmpty(users.getRecords())) {
        throw new ServiceException("没有权限访问用户数据！");
      }
    }
  }

  /**
   * 新增用户信息
   *
   * @param addReq 用户信息
   * @return 结果
   */
  @Tran
  @Override
  public Long insertUser(final SysUserReq.AddReq addReq) {
    Preconditions.checkArgument(self.isColumnValueUnique(SysUserProxy.TABLE.userName(),addReq.getUserName(), null), "新增用户'" + addReq.getUserName() + "'失败，登录账号已存在");
    Preconditions.checkArgument(self.isColumnValueUnique(SysUserProxy.TABLE.phonenumber(), addReq.getPhonenumber(), null), "新增用户'" + addReq.getUserName() + "'失败，手机号码已存在");
    Preconditions.checkArgument(self.isColumnValueUnique(SysUserProxy.TABLE.email(), addReq.getEmail(), null), "新增用户'" + addReq.getUserName() + "'失败，邮箱账号已存在");

    SysUser user = addReq.transferTo(SysUser.class);

    user.setPassword(SecurityUtils.encryptPassword(addReq.getPassword())); //
    user.setStatus(UserConstants.NORMAL); // 状态为正常
    user.setDelFlag(UserConstants.NORMAL); // 删除状态为未删除
    user.setLoginIp("");
    user.setLoginDate(null);
    user.setCreateTime(LocalDateTimeUtil.of(new Date()));
    user.setUpdateBy(user.getCreateBy());
    user.setUpdateTime(LocalDateTimeUtil.of(new Date()));


//    getBaseMapper().insert(user);
    entityQuery.insertable(user)
      .executeRows(true);
    Long userId = user.getUserId();

    // 保存用户岗位关联
    userPostService.updateMappingsByUserId(userId, addReq.getPostIds());
    // 保存用户与角色关联
    userRoleService.updateMappingsByUserId(userId, addReq.getRoleIds());
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
   * @param updateReq 用户信息
   * @return 结果
   */
  @Override
  public long updateUser(final SysUserReq.UpdateReq updateReq) {
    Long userId = updateReq.getUserId();


//    final SysUser sysUser = getBaseMapper().selectById(userId);
    final SysUser sysUser = entityQuery.queryable(SysUser.class)
      .whereById(userId)
      .singleNotNull();


    // 更新用户与角色关联
    userRoleService.updateMappingsByUserId(userId, updateReq.getRoleIds());

    // 更新用户与岗位关联
    userPostService.updateMappingsByUserId(userId, updateReq.getPostIds());

    // sysUser 从 user 复制属性
    BeanUtil.copyProperties(updateReq, sysUser);

//    return getBaseMapper().updateById(sysUser);
    return entityQuery.updatable(sysUser)
      .setSQLStrategy(SQLExecuteStrategyEnum.ALL_COLUMNS)
      .executeRows();
  }

  /**
   * 用户授权角色
   *
   * @param userId  用户ID
   * @param roleIds 角色组
   */
  @Tran(policy = TranPolicy.required)
  @Override
  public void insertUserAuth(final Long userId, final List<Long> roleIds) {
    // 新增用户与角色关联
    userRoleService.updateMappingsByUserId(userId, roleIds);
  }

  /**
   * 修改用户状态
   *
   * @param user 用户信息
   * @return 结果
   */
  @Override
  public long updateUserStatus(final SysUser user) {
//    return getBaseMapper().updateById(user);
    return entityQuery.updatable(user)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
  }

  /**
   * 修改用户基本信息
   *
   * @param user 用户信息
   * @return 结果
   */
  @Override
  public long updateUserProfile(final SysUser user) {
//    return getBaseMapper().updateById(user);
    return entityQuery.updatable(user)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
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
   * 重置用户密码
   *
   * @param user 用户信息
   * @return 结果
   */
  @Override
  public long resetPwd(final SysUser user) {
    return entityQuery.updatable(SysUser.class)
      .where(userProxy -> userProxy.userId().eq(user.getUserId()))
      .setColumns(userProxy -> userProxy.password().set(user.getPassword()))
      // TODO 用户管理应该加上乐观锁
      .executeRows();
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
    return entityQuery.updatable(SysUser.class)
      .setColumns(user -> user.password().set(password))
      .where(user -> user.userName().eq(userName))
      .executeRows() > 0;
  }

  /**
   * 通过用户ID删除用户
   *
   * @param userId 用户ID
   * @return 结果
   */
  @Override
  public boolean deleteUserById(final Long userId) {
    // 删除用户与角色关联
    userRoleService.deleteUserRoleByUserId(userId);
    // 删除用户与岗位表
    userPostService.deleteUserPostByUserId(userId);

    return entityQuery.updatable(SysUser.class)
      .setColumns(user -> user.delFlag().set(UserConstants.DELETED))
      .where(user -> user.userId().eq(userId))
      .executeRows() > 0;

  }

  /**
   * 批量删除用户信息
   *
   * @param context 操作上下文, 含有当前用户信息
   * @param userIds 需要删除的用户ID
   * @return 结果
   */
  @Override
  public boolean deleteUserByIds(final ActionContext context, final List<Long> userIds) {

    for (final Long userId : userIds) {
      self.checkUserAllowed(userId);
      self.checkUserDataScope(context, userId);
    }

    // 删除用户与角色关联
    userRoleService.deleteUserRole(userIds);
    // 删除用户与岗位关联
    userPostService.deleteUserPost(userIds);

    return entityQuery.updatable(SysUser.class)
      .setColumns(user -> user.delFlag().set(UserConstants.DELETED))
      .where(user -> user.userId().in(userIds))
      .executeRows() > 0;
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
    if (StringUtils.isNull(userDTOList) || userDTOList.isEmpty()) {
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
        if (StringUtils.isNull(u)) {
          BeanValidators.validateWithException(user);
          user.setPassword(SecurityUtils.encryptPassword(password));
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
   * @return 用户ID
   */
  @Override
  public Long findUserIdByUserName(final String userName) {
//    return lambdaQuery().select(SysUser::getUserId)
//      .eq(SysUser::getUserName, userName)
//      .oneOpt().map(SysUser::getUserId)
//      .orElse(-1L);
    return entityQuery.queryable(SysUser.class)
      .where(user -> user.userName().eq(userName))
      .select(SysUserProxy::userId)
      .singleOptional()
      .orElse(-1L);
  }

  @Override
  public SysUser getById(long userId) {
    return entityQuery.queryable(SysUser.class).whereById(userId).singleNotNull();
  }
}
