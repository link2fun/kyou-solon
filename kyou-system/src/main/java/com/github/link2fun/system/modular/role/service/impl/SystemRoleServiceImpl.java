package com.github.link2fun.system.modular.role.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.framework.auth.AuthCacheInvalidator;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.easyquery.DataScope;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.role.model.req.SysRoleAddReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeDataScopeReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleChangeStatusReq;
import com.github.link2fun.system.modular.role.model.req.SysRoleModifyReq;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.system.modular.roledept.service.ISystemRoleDeptService;
import com.github.link2fun.system.modular.rolemenu.service.ISystemRoleMenuService;
import com.github.link2fun.system.modular.userrole.service.ISystemUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class SystemRoleServiceImpl implements ISystemRoleService {

  @Inject
  private ISystemRoleService self;

  @Inject
  private ISystemUserRoleService userRoleService;

  @Inject
  private ISystemRoleMenuService roleMenuService;

  @Inject
  private ISystemRoleDeptService roleDeptService;

  @Inject
  private AggregateDeleter aggregateDeleter;

  @Inject
  private AuthCacheInvalidator authCacheInvalidator;

  @Db
  private EasyEntityQuery entityQuery;

  /** 按条件分页查询角色, 受数据范围约束 */
  @Override
  public <T> Page<T> selectRoleList(ActionContext context, Page<SysRole> pageRequest, final SysRole searchReq, Class<T> resultClass) {

    final DataScope dataScope = DataScope.of(entityQuery, context);

    EntityQueryable<SysRoleProxy, SysRole> queryable = entityQuery.queryable(SysRole.class)
      .where((role) -> {
        role.delFlag().eq(UserConstants.NORMAL);
        role.roleId().eq(IdUtils.isIdValid(searchReq.getRoleId()), searchReq.getRoleId()); // 角色ID检索
        role.roleName().like(StrUtil.isNotBlank(searchReq.getRoleName()), searchReq.getRoleName()); // 角色名称检索
        role.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()); // 角色状态检索
        role.roleKey().like(StrUtil.isNotBlank(searchReq.getRoleKey()), searchReq.getRoleKey()); // 角色权限检索
        role.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()),
          searchReq.getParams().getBeginTime()); // 开始时间检索
        role.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime()); // 结束时间检索
      })
      .where(roleOuter -> {
        if (dataScope.filters()) {
          // 角色列表按「角色下有可见用户」判定, 故数据范围作用在关联用户/部门的存在性子查询上
          roleOuter.userRoleList().any(userRole ->
            dataScope.applyTo(userRole, userRole.user().dept().deptId(), userRole.user().userId())); // 数据范围过滤
        }
      });


    if (resultClass == SysRole.class) {
      // 传入的就是 SysRole 类型, 不查询关联信息
      //noinspection unchecked
      return (Page<T>) Page.of(pageRequest, queryable.toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize()));
    }


    EasyPageResult<T> pageResult = queryable
      .selectAutoInclude(resultClass)
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize());

    return Page.of(pageRequest, pageResult);
  }


  /**
   * 根据用户ID查询角色列表
   *
   * @param userId 用户ID
   * @return 角色列表
   */
  @Override
  public List<RoleDTO> selectRolesByUserId(final Long userId) {
    final List<RoleDTO> roleList = entityQuery.queryable(SysRole.class)
      .selectAutoInclude(RoleDTO.class)
      .toList();

    final List<Long> userRoleIdList = userRoleService.findRoleIdsByUserId(userId);
    roleList.forEach(r -> {
      if (userRoleIdList.contains(r.getRoleId())) {
        r.setFlag(true);
      }
    });

    return roleList;
  }


  /**
   * 查询用户持有的启用角色标识集合(停用/软删角色不参与授权)
   *
   * @param userId 用户ID
   * @return 启用角色的 roleKey 集合
   */
  @Override
  public List<String> listEnabledRoleKeysByUserId(final Long userId) {
    return entityQuery.queryable(SysRole.class)
      .where(role -> {
        role.delFlag().eq(UserConstants.NORMAL);
        role.status().eq(UserConstants.NORMAL);
        role.userRoleList().any(userRole -> userRole.userId().eq(userId));
      })
      .selectColumn(SysRoleProxy::roleKey)
      .toList();
  }

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  @Override
  public List<SysRole> selectRoleAll(ActionContext context) {
    return selectRoleList(context, Page.ofAll(), new SysRole(), SysRole.class).getRecords();
  }


  /**
   * 通过角色ID查询角色
   *
   * @param roleId      角色ID
   * @param resultClass 返回的结果类型
   * @return 角色对象信息
   */
  @Override
  public <T> T selectRoleById(final Long roleId, Class<T> resultClass) {
    EntityQueryable<SysRoleProxy, SysRole> queryable = entityQuery.queryable(SysRole.class).whereById(roleId);
    if (resultClass == SysRole.class) {
      // 传入的就是 SysRole 类型, 不查询关联信息
      //noinspection unchecked
      return (T) queryable.singleNotNull();
    }
    return queryable
      .selectAutoInclude(resultClass)
      .singleOrNull();
  }

  /**
   * 校验角色是否允许操作
   *
   * @param roleId 角色ID
   */
  private void checkRoleAllowed(final Long roleId) {
    if (Objects.nonNull(roleId) && SysRole.isSuperAdminRole(roleId)) {
      throw new ServiceException(StrUtil.format("不允许操作超级管理员角色"));
    }
  }

  /**
   * 校验角色名称/权限字符在系统中是否唯一, 不唯一时抛出业务异常
   *
   * @param action 操作描述(如 "新增"/"修改"), 用于拼接错误文案
   * @param roleId 当前角色id(编辑时排除自身), 新增时为 null
   */
  private void checkRoleFieldUnique(final String action, final String roleName, final String roleKey, final Long roleId) {
    if (StrUtil.contains(roleKey, StrUtil.COMMA)) {
      throw new ServiceException(action + "角色'" + roleName + "'失败，角色权限不能包含逗号");
    }
    UniqueChecker.checkOccupied(entityQuery.queryable(SysRole.class)
        .where(_role -> {
          _role.roleName().eq(roleName);
          _role.delFlag().eq(UserConstants.NORMAL);
          _role.roleId().ne(IdUtils.isIdValid(roleId), roleId);
        }).any(),
      () -> action + "角色'" + roleName + "'失败，角色名称已存在");
    UniqueChecker.checkOccupied(entityQuery.queryable(SysRole.class)
        .where(_role -> {
          _role.roleKey().eq(roleKey);
          _role.delFlag().eq(UserConstants.NORMAL);
          _role.roleId().ne(IdUtils.isIdValid(roleId), roleId);
        }).any(),
      () -> action + "角色'" + roleName + "'失败，角色权限已存在");
  }

  /**
   * 校验角色是否有数据权限
   *
   * @param roleId 角色id
   */
  @Override
  public void checkRoleDataScope(final Long roleId) {
    if (!ActionContext.current().isSuperAdmin()) {
      SysRole role = new SysRole();
      role.setRoleId(roleId);
      List<SysRole> roles = selectRoleList(ActionContext.current(), Page.ofLimit(1), role, SysRole.class).getRecords();
      if (CollectionUtil.isEmpty(roles)) {
        throw new ServiceException("没有权限访问角色数据！");
      }
    }
  }

  /**
   * 通过角色ID查询角色使用数量
   *
   * @param roleId 角色ID
   * @return 结果
   */
  @Override
  public long countUserRoleByRoleId(final Long roleId) {
    return userRoleService.countByRoleId(roleId);
  }

  /**
   * 新增保存角色信息
   *
   * @param role 角色信息
   * @return 结果
   */
  @Transaction
  @Override
  public long insertRole(final SysRoleAddReq role) {
    checkRoleFieldUnique("新增", role.getRoleName(), role.getRoleKey(), null);

    SysRole entity = role.toEntity();
    long row = entityQuery.insertable(entity).executeRows(true);
    final List<Long> menuIds = role.getMenuIds();

    roleMenuService.reassignMenus(entity.getRoleId(), menuIds);

    return row;
  }

  /**
   * 修改保存角色信息
   *
   * @param modifyReq 角色信息
   * @return 结果
   */
  @Transaction
  @Override
  public long updateRole(final SysRoleModifyReq modifyReq) {
    Long roleId = modifyReq.getRoleId();
    checkRoleAllowed(roleId);
    checkRoleDataScope(roleId);
    checkRoleFieldUnique("修改", modifyReq.getRoleName(), modifyReq.getRoleKey(), roleId);

    SysRole role = entityQuery.queryable(SysRole.class)
      .whereById(roleId)
      .singleNotNull();
    final String oldRoleKey = role.getRoleKey();

    BeanUtil.copyProperties(modifyReq, role);

    long row = entityQuery.updatable(role).executeRows();

    roleMenuService.reassignMenus(roleId, modifyReq.getMenuIds());

    authCacheInvalidator.onRolesChanged(List.of(roleId), List.of(oldRoleKey));
    return row;
  }

  /**
   * 修改角色状态
   *
   * @param changeStatusReq 角色信息
   * @return 结果
   */
  @Transaction
  @Override
  public long changeRoleStatus(final SysRoleChangeStatusReq changeStatusReq) {
    checkRoleAllowed(changeStatusReq.getRoleId());
    checkRoleDataScope(changeStatusReq.getRoleId());
    final long rows = entityQuery.updatable(SysRole.class)
      .whereById(changeStatusReq.getRoleId())
      .setColumns(r -> r.status().set(changeStatusReq.getStatus()))
      .executeRows();
    authCacheInvalidator.onRolesChanged(List.of(changeStatusReq.getRoleId()), List.of());
    return rows;
  }

  /**
   * 修改数据权限信息
   *
   * @param changeDataScopeReq 角色信息
   * @return 结果
   */
  @Transaction
  @Override
  public long authDataScope(final SysRoleChangeDataScopeReq changeDataScopeReq) {
    checkRoleAllowed(changeDataScopeReq.getRoleId());
    checkRoleDataScope(changeDataScopeReq.getRoleId());

    long executeRows = entityQuery.updatable(SysRole.class)
      .whereById(changeDataScopeReq.getRoleId())
      .setColumns(role -> {
        role.dataScope().set(changeDataScopeReq.getDataScope());
        role.remark().set(changeDataScopeReq.getRemark());
      })
      .executeRows();

    roleDeptService.reassignDepts(changeDataScopeReq.getRoleId(), changeDataScopeReq.getDeptIds());

    authCacheInvalidator.onRolesChanged(List.of(changeDataScopeReq.getRoleId()), List.of());
    return executeRows;
  }


  /**
   * 批量删除角色信息
   *
   * @param roleIds 需要删除的角色ID
   * @return 结果
   */
  @Transaction
  @Override
  public long deleteRoleByIds(final List<Long> roleIds) {
    final List<String> roleKeys = entityQuery.queryable(SysRole.class)
      .whereByIds(roleIds)
      .selectColumn(SysRoleProxy::roleKey)
      .toList();

    final long rows = aggregateDeleter.delete(AggregateDeletePlan.of(roleIds)
      .guardPerId(id -> {
        checkRoleAllowed(id);
        checkRoleDataScope(id);
      })
      .guardPerId(this::assertRoleUnused)
      // 删除角色与菜单关联
      .unlink(roleMenuService::removeByRoleIds)
      // 删除角色与部门关联
      .unlink(roleDeptService::removeByRoleIds)
      .delete(ids -> entityQuery.deletable(SysRole.class)
        .allowDeleteStatement(true)
        .whereByIds(ids)
        .executeRows()));

    authCacheInvalidator.onRolesChanged(List.of(), roleKeys);
    return rows;
  }

  /** 角色已分配用户时阻止删除 */
  private void assertRoleUnused(final Long roleId) {
    if (self.countUserRoleByRoleId(roleId) > 0) {
      SysRole role = entityQuery.queryable(SysRole.class)
        .whereById(roleId)
        .singleNotNull();
      throw new ServiceException(String.format("%s 已分配,不能删除", role.getRoleName()));
    }
  }

  /** 取消单个用户的角色授权 */
  @Transaction
  @Override
  public void unassignUser(final Long roleId, final Long userId) {
    userRoleService.unassignUser(roleId, userId);
    if (Objects.nonNull(userId)) {
      authCacheInvalidator.onUsersChanged(List.of(userId));
    }
  }

  /** 批量取消授权用户角色 */
  @Transaction
  @Override
  public void unassignUsers(final Long roleId, final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    userRoleService.unassignUsers(roleId, userIds);
    authCacheInvalidator.onUsersChanged(userIds);
  }

  /** 批量为角色授权用户 */
  @Transaction
  @Override
  public void assignUsers(final Long roleId, final List<Long> userIds) {
    checkRoleDataScope(roleId);
    userRoleService.assignUsers(roleId, userIds);
    authCacheInvalidator.onUsersChanged(userIds);
  }

  /**
   * 根据角色关键字查询角色信息
   *
   * @param roleKey 角色关键字
   * @return 返回角色信息
   */
  @Override
  public SysRole selectRoleByRoleKey(String roleKey) {
    return entityQuery.queryable(SysRole.class)
      .where((_role) -> _role.roleKey().eq(roleKey))
      .singleOrNull();
  }

  /** 根据角色ID集合查询角色 */
  @Override
  public List<SysRole> listByIds(List<Long> roleIdList) {
    return entityQuery.queryable(SysRole.class).whereByIds(roleIdList).toList();
  }

}
