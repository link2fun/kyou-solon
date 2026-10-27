package com.github.link2fun.system.modular.group.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.aggregatedelete.AggregateDeletePlan;
import com.github.link2fun.support.aggregatedelete.AggregateDeleter;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.easyquery.UniqueChecker;
import com.github.link2fun.support.utils.uuid.IdUtils;
import com.github.link2fun.system.modular.group.model.SysGroup;
import com.github.link2fun.system.modular.group.model.req.SysGroupAddReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupModifyReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupSearchReq;
import com.github.link2fun.system.modular.group.service.ISystemGroupService;
import com.github.link2fun.system.modular.usergroup.service.ISystemUserGroupService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

/** 系统用户组 Service Impl */
@Slf4j
@Component
public class SystemGroupServiceImpl implements ISystemGroupService {

  @Inject
  private ISystemUserGroupService userGroupService;

  @Inject
  private AggregateDeleter aggregateDeleter;

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 根据搜索条件分页查询系统群组信息。
   */
  @Override
  public Page<SysGroup> pageSearchGroup(Page<SysGroup> pageRequest, SysGroupSearchReq searchReq) {

    EasyPageResult<SysGroup> pageResult = entityQuery.queryable(SysGroup.class)
      .where(group -> group.groupName().like(StrUtil.isNotBlank(searchReq.getGroupName()), searchReq.getGroupName()))
      .where(group -> group.status().eq(StrUtil.isNotBlank(searchReq.getStatus()), searchReq.getStatus()))
      .toPageResult(pageRequest.getPageNum(), pageRequest.getPageSize());
    return Page.of(pageRequest, pageResult);
  }

  /**
   * 校验群组名称唯一性, 不唯一时抛出业务异常
   *
   * @param action 操作描述(如 "新增"/"修改"), 用于拼接错误文案
   * @param group  群组信息
   */
  private void checkGroupNameUnique(final String action, final SysGroup group) {
    UniqueChecker.checkOccupied(entityQuery.queryable(SysGroup.class)
        .where(_group -> {
          _group.groupName().eq(group.getGroupName());
          _group.groupId().ne(IdUtils.isIdValid(group.getGroupId()), group.getGroupId());
        }).any(),
      () -> action + "群组'" + group.getGroupName() + "'失败，群组名称已存在");
  }

  /**
   * 新增群组(内部已完成名称唯一性检查)
   */
  @Transaction
  @Override
  public long insertGroup(final SysGroupAddReq addReq) {
    final SysGroup group = BeanUtil.toBean(addReq, SysGroup.class);
    checkGroupNameUnique("新增", group);
    return entityQuery.insertable(group).executeRows();
  }

  /**
   * 修改群组(内部已完成名称唯一性检查)
   */
  @Transaction
  @Override
  public long updateGroup(final SysGroupModifyReq modifyReq) {
    final SysGroup group = BeanUtil.toBean(modifyReq, SysGroup.class);
    checkGroupNameUnique("修改", group);
    return entityQuery.updatable(group)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS).executeRows();
  }

  /**
   * 批量删除群组（静默解绑: 顺带清除群组的用户关联）
   */
  @Transaction
  @Override
  public long deleteGroupByIds(final List<Long> groupIds) {
    return aggregateDeleter.delete(AggregateDeletePlan.of(groupIds)
      // 删除群组与用户关联
      .unlink(userGroupService::removeByGroupIds)
      .delete(ids -> entityQuery.deletable(SysGroup.class)
        .allowDeleteStatement(true)
        .where(group -> group.groupId().in(ids)).executeRows()));
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
