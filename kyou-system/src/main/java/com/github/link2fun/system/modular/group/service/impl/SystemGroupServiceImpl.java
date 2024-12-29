package com.github.link2fun.system.modular.group.service.impl;

import cn.hutool.core.util.StrUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.system.modular.group.model.SysGroup;
import com.github.link2fun.system.modular.group.service.ISystemGroupService;

import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

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
   * 检测群组名称是否唯一
   *
   * @param groupName 群组名称
   * @param groupId   群组ID
   * @return 是否唯一
   */
  @Override
  public boolean checkGroupNameUnique(final String groupName, final Long groupId) {

    return !entityQuery.queryable(SysGroup.class)
      .where(group -> group.groupName().eq(groupName))
      .where(group -> group.groupId().ne(groupId))
      .any();
  }

  @Override
  public SysGroup getById(Long groupId) {
    return entityQuery.queryable(SysGroup.class)
      .whereById(groupId).singleOrNull();
  }
}
