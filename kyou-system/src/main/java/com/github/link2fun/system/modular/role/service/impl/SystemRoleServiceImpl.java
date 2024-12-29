package com.github.link2fun.system.modular.role.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.api.proxy.entity.select.EntityQueryable;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.annotation.DataScope;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.dto.RoleDTO;
import com.github.link2fun.support.core.domain.entity.SysDept;
import com.github.link2fun.support.core.domain.entity.SysRole;
import com.github.link2fun.support.core.domain.entity.SysUser;
import com.github.link2fun.support.core.domain.entity.SysUserRole;
import com.github.link2fun.support.core.domain.entity.proxy.SysRoleProxy;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
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
import org.apache.poi.ss.formula.functions.T;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

  @Db
  private EasyEntityQuery entityQuery;

  @DataScope(deptAlias = SysDept.TABLE_ALIAS)
  @Override
  public <T> Page<T> selectRoleList(ActionContext context, Page<SysRole> page, final SysRole searchReq, Class<T> resultClass) {

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
        if (StrUtil.isNotBlank(searchReq.getParams().getDataScope())) {
          roleOuter.expression().exists(() -> {
            //
            return entityQuery.queryable(SysRole.class)
              .leftJoin(SysUserRole.class, (role, userRole) -> role.roleId().eq(userRole.roleId()))
              .leftJoin(SysUser.class, (role, userRole, user) -> userRole.userId().eq(user.userId()))
              .asAlias(SysUser.TABLE_ALIAS)
              .leftJoin(SysDept.class, (role, userRole, user, dept) -> user.deptId().eq(dept.deptId()))
              .asAlias(SysDept.TABLE_ALIAS)
              .where((role, userRole, user, dept) -> {
                role.expression().sql(searchReq.getParams().getDataScope()); // 数据范围过滤
                role.roleId().eq(roleOuter.roleId());
              });
          });
        }
      });


    if (resultClass == SysRole.class) {
      // 传入的就是 SysRole 类型, 不查询关联信息
      //noinspection unchecked
      return (Page<T>) Page.of(queryable.toPageResult(page.getPageNum(), page.getPageSize()));
    }


    EasyPageResult<T> pageResult = queryable
      .selectAutoInclude(resultClass)
      .toPageResult(page.getPageNum(), page.getPageSize());

    return Page.of(pageResult);
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

    final List<Long> userRoleIdList = userRoleService.findRoleIdListByUserId(userId);
    roleList.forEach(r -> {
      if (userRoleIdList.contains(r.getRoleId())) {
        r.setFlag(true);
      }
    });

    return roleList;
  }


  /**
   * 根据用户ID查询角色权限
   *
   * @param userId 用户ID
   * @return 权限列表
   */
  @Override
  public Set<String> selectRolePermissionByUserId(final Long userId) {
    List<String> roleKeyList = entityQuery.queryable(SysRole.class)
      .leftJoin(SysUserRole.class, (role, userRole) -> role.roleId().eq(userRole.roleId()))
      .where((role, userRole) -> userRole.userId().eq(userId))
      .selectColumn((role, userRole) -> role.roleKey())
      .distinct()
      .toList();

    return roleKeyList.stream()
      .flatMap(_roleKey -> StrUtil.split(_roleKey, StrUtil.COMMA).stream())
      .filter(StrUtil::isNotBlank)
      .collect(Collectors.toSet());
  }

  /**
   * 查询所有角色
   *
   * @return 角色列表
   */
  @Override
  public List<SysRole> selectRoleAll(ActionContext context) {
    return self.selectRoleList(context, Page.ofAll(), new SysRole(), SysRole.class).getRecords();
  }


  /**
   * 通过角色ID查询角色
   *
   * @param roleId      角色ID
   * @param resultClass 返回的结果类型
   * @return 角色对象信息
   */
  @Override
  public <T>  T selectRoleById(final Long roleId, Class<T> resultClass) {
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
   * 校验角色名称是否唯一
   *
   * @param roleName  角色名称
   * @param roleId 角色ID
   * @return 结果
   */
  @Override
  public boolean isRoleNameUnique(final String roleName, Long roleId) {

    final Long roleIdSameRoleName = entityQuery.queryable(SysRole.class)
      .where((_role) -> _role.roleName().eq(roleName))
      .select(SysRoleProxy::roleId)
      .singleOrNull();
    if (Objects.nonNull(roleIdSameRoleName) && !Objects.equals(roleIdSameRoleName, roleId)) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }

  /**
   * 校验角色权限是否唯一
   *
   * @param roleKey 角色信息
   * @return 结果
   */
  @Override
  public boolean isRoleKeyUnique(final String roleKey, Long roleId) {
    final Long roleIdSameRokeKey = entityQuery.queryable(SysRole.class)
      .where((_role) -> _role.roleKey().eq(roleKey))
      .select(SysRoleProxy::roleId)
      .singleOrNull();
    if (Objects.nonNull(roleIdSameRokeKey) && !Objects.equals(roleIdSameRokeKey, roleId)) {
      return UserConstants.NOT_UNIQUE;
    }
    return UserConstants.UNIQUE;
  }

  /**
   * 校验角色是否允许操作
   *
   * @param roleId 角色ID
   */
  @Override
  public void checkRoleAllowed(final Long roleId) {
    if (Objects.nonNull(roleId) && SysRole.isAdmin(roleId)) {
      throw new ServiceException(StrUtil.format("不允许操作超级管理员角色"));
    }
  }

  /**
   * 校验角色是否有数据权限
   *
   * @param roleId 角色id
   */
  @Override
  public void checkRoleDataScope(final Long roleId) {
    if (!SysUser.isAdmin(SecurityUtils.getUserId())) {
      SysRole role = new SysRole();
      role.setRoleId(roleId);
      List<SysRole> roles = self.selectRoleList(ActionContext.current(), Page.ofLimit(1), role,SysRole.class ).getRecords();
      if (StringUtils.isEmpty(roles)) {
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
  @Override
  public long insertRole(final SysRoleAddReq role) {

    SysRole entity = role.toEntity();
    long row = entityQuery.insertable(entity).executeRows(true);
    final List<Long> menuIds = role.getMenuIds();

    roleMenuService.updateMappings(entity.getRoleId(), menuIds);

    return row;
  }

  /**
   * 修改保存角色信息
   *
   * @param modifyReq 角色信息
   * @return 结果
   */
  @Override
  public long updateRole(final SysRoleModifyReq modifyReq) {
    Long roleId = modifyReq.getRoleId();
    SysRole role = entityQuery.queryable(SysRole.class)
      .whereById(roleId)
      .singleNotNull();

    BeanUtil.copyProperties(modifyReq, role);

    long row = entityQuery.updatable(role).executeRows();

    roleMenuService.updateMappings(roleId, modifyReq.getMenuIds());

    return row;
  }

  /**
   * 修改角色状态
   *
   * @param changeStatusReq 角色信息
   * @return 结果
   */
  @Override
  public long changeRoleStatus(final SysRoleChangeStatusReq changeStatusReq) {
    return entityQuery.updatable(SysRole.class)
      .whereById(changeStatusReq.getRoleId())
      .setColumns(role -> role.status().set(changeStatusReq.getStatus()))
      .executeRows();
  }

  /**
   * 修改数据权限信息
   *
   * @param changeDataScopeReq 角色信息
   * @return 结果
   */
  @Override
  public long authDataScope(final SysRoleChangeDataScopeReq changeDataScopeReq) {

    // 修改角色信息
    long executeRows = entityQuery.updatable(SysRole.class)
      .whereById(changeDataScopeReq.getRoleId())
      .setColumns(role -> {
        role.dataScope().set(changeDataScopeReq.getDataScope());
        role.remark().set(changeDataScopeReq.getRemark());
      })
      .executeRows();

    // 修改角色与部门关联关系
    roleDeptService.updateMappings(changeDataScopeReq.getRoleId(), changeDataScopeReq.getDeptIds());

    return executeRows;
  }


  /**
   * 批量删除角色信息
   *
   * @param roleIds 需要删除的角色ID
   * @return 结果
   */
  @Override
  public long deleteRoleByIds(final List<Long> roleIds) {
    for (Long roleId : roleIds) {
      checkRoleAllowed(roleId);
      checkRoleDataScope(roleId);

      if (self.countUserRoleByRoleId(roleId) > 0) {
        SysRole role = entityQuery.queryable(SysRole.class)
          .whereById(roleId)
          .singleNotNull();
        throw new ServiceException(String.format("%s 已分配,不能删除", role.getRoleName()));
      }
    }
    // 删除角色与菜单关联
    roleMenuService.removeByRoleIds(roleIds);
    // 删除角色与部门关联
    roleDeptService.removeByRoleIds(roleIds);
    return entityQuery.deletable(SysRole.class)
      .allowDeleteStatement(true)
      .whereByIds(roleIds)
      .executeRows();
  }

  /**
   * 取消授权用户角色
   *
   * @return 结果
   */
  @Override
  public boolean removeUserRoleMapping(Long userId, Long roleId) {
    return userRoleService.removeMapping(userId, roleId);
  }

  /**
   * 批量取消授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要取消授权的用户数据ID
   * @return 结果
   */
  @Override
  public boolean deleteAuthUsers(final Long roleId, final List<Long> userIds) {
    return userRoleService.deleteUserRoleInfos(roleId, userIds);
  }

  /**
   * 批量选择授权用户角色
   *
   * @param roleId  角色ID
   * @param userIds 需要删除的用户数据ID
   * @return 结果
   */
  @Override
  public Boolean insertAuthUsers(final Long roleId, final List<Long> userIds) {
    return userRoleService.batchUserRole(roleId, userIds);
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

  @Override
  public List<SysRole> listByIds(List<Long> roleIdList) {
    return entityQuery.queryable(SysRole.class).whereByIds(roleIdList).toList();
  }

}
