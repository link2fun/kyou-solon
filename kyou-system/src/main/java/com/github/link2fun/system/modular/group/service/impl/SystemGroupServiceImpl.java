package com.github.link2fun.system.modular.group.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.system.modular.group.model.SysGroup;
import com.github.link2fun.system.modular.group.service.ISystemGroupService;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;
import java.util.Objects;

/** 系统用户组 Service Impl */
@Slf4j
@Component
public class SystemGroupServiceImpl implements ISystemGroupService {

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 根据搜索条件分页查询系统群组信息。
   */
  @Override
  public Page<SysGroup> pageSearchGroup(Page<SysGroup> page, SysGroup searchReq) {

    EasyPageResult<SysGroup> pageResult = entityQuery.queryable(SysGroup.class)
      .where(group -> group.groupName().like(StrUtil.isNotBlank(searchReq.getGroupName()), searchReq.getGroupName()))
      .where(group -> group.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()))
      .toPageResult(page.getPageNum(), page.getPageSize());
    return Page.of(pageResult);
  }

  /**
   * 校验群组名称唯一性, 不唯一时抛出业务异常
   *
   * @param action 操作描述(如 "新增"/"修改"), 用于拼接错误文案
   * @param group  群组信息
   */
  private void checkGroupNameUnique(final String action, final SysGroup group) {
    final SysGroup temp = entityQuery.queryable(SysGroup.class)
      .where(_group -> _group.groupName().eq(group.getGroupName()))
      .singleOrNull();
    if (Objects.nonNull(temp) && !Objects.equals(temp.getGroupId(), group.getGroupId())) {
      throw new ServiceException(action + "群组'" + group.getGroupName() + "'失败，群组名称已存在");
    }
  }

  /**
   * 新增群组(内部已完成名称唯一性检查)
   */
  @Transaction
  @Override
  public long insertGroup(final SysGroup group) {
    checkGroupNameUnique("新增", group);
    return entityQuery.insertable(group).executeRows();
  }

  /**
   * 修改群组(内部已完成名称唯一性检查)
   */
  @Transaction
  @Override
  public long updateGroup(final SysGroup group) {
    checkGroupNameUnique("修改", group);
    return entityQuery.updatable(group)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS).executeRows();
  }

  /**
   * 批量删除群组
   */
  @Transaction
  @Override
  public long deleteGroupByIds(final List<Long> groupIds) {
    return entityQuery.deletable(SysGroup.class)
      .allowDeleteStatement(true)
      .where(group -> group.groupId().in(groupIds)).executeRows();
  }

  /**
   * 查询所有群组
   */
  @Override
  public List<SysGroup> listAll() {
    return entityQuery.queryable(SysGroup.class).toList();
  }

  @Override
  public SysGroup getById(Long groupId) {
    return entityQuery.queryable(SysGroup.class)
      .whereById(groupId).singleOrNull();
  }
}
