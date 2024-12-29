package com.github.link2fun.system.modular.usergroup.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.solon.annotation.Db;
import com.github.link2fun.system.modular.usergroup.model.SysUserGroup;
import com.github.link2fun.system.modular.usergroup.model.proxy.SysUserGroupProxy;
import com.github.link2fun.system.modular.usergroup.service.ISystemUserGroupService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.stream.Collectors;

/** 用户和群组关联表 服务实现类 */
@Slf4j
@Component
public class SystemUserGroupServiceImpl
  implements ISystemUserGroupService {

  @Inject
  private ISystemUserGroupService self;

  @Db
  private EasyEntityQuery entityQuery;

  /**
   * 给用户分配多个群组, 没有在分配群组内的会进行取消
   *
   * @param userId   用户ID
   * @param groupIds 群组ID List
   * @return 是否成功
   */
  @Override
  public boolean assignGroups(final Long userId, final List<Long> groupIds) {

    // 先查询用户已有的群组
    List<Long> userGroupIds = self.queryGroupIdsByUserId(userId);

    // 需要添加的群组
    List<Long> addGroupIds = groupIds.stream().filter(groupId -> !userGroupIds.contains(groupId))
      .collect(java.util.stream.Collectors.toList());

    // 需要移除的群组
    List<Long> removeGroupIds = userGroupIds.stream().filter(groupId -> !groupIds.contains(groupId))
      .collect(java.util.stream.Collectors.toList());

    // 添加群组
    if (CollectionUtil.isNotEmpty(addGroupIds)) {
      // stream 生成 mapping, 然后批量写入
      final List<SysUserGroup> userGroupList = addGroupIds.stream().map(groupId -> {
        SysUserGroup sysUserGroup = new SysUserGroup();
        sysUserGroup.setUserId(userId);
        sysUserGroup.setGroupId(groupId);
        return sysUserGroup;
      }).collect(Collectors.toList());
//      self.saveBatch(userGroupList);
      entityQuery.insertable(userGroupList).executeRows();
    }

    if (CollectionUtil.isNotEmpty(removeGroupIds)) {
      // 删除群组
      // stream 生成 mapping, 然后批量删除
      removeGroupIds.forEach(groupId -> {
//        lambdaUpdate()
//          .eq(SysUserGroup::getUserId, userId)
//          .eq(SysUserGroup::getGroupId, groupId)
//          .remove()
        entityQuery.deletable(SysUserGroup.class)
          .allowDeleteStatement(true)
          .where(userGroup -> {
            userGroup.userId().eq(userId);
            userGroup.groupId().eq(groupId);
          }).executeRows();
      });
    }

    return true;
  }

  /**
   * 向群组中添加多个用户
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void addUsers(final Long groupId, final List<Long> userIds) {
    // 查询群组已有的用户
    List<Long> groupUserIds = self.queryUserIdsByGroupId(groupId);

    // 需要添加的用户
    List<Long> addGroupUserIds = userIds.stream().filter(userId -> !groupUserIds.contains(userId))
      .collect(java.util.stream.Collectors.toList());

    // 如果没有需要添加的用户, 则直接返回
    if (CollectionUtil.isEmpty(addGroupUserIds)) {
      return;
    }

    // stream 生成 mapping, 然后批量写入
    final List<SysUserGroup> userGroupList = addGroupUserIds.stream().map(userId -> {
      SysUserGroup sysUserGroup = new SysUserGroup();
      sysUserGroup.setUserId(userId);
      sysUserGroup.setGroupId(groupId);
      return sysUserGroup;
    }).collect(Collectors.toList());
//    self.saveBatch(userGroupList);
    entityQuery.insertable(userGroupList).executeRows();
  }

  /**
   * 从群组中移除多个用户
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void removeUsers(final Long groupId, final List<Long> userIds) {

//    lambdaUpdate()
//      .eq(SysUserGroup::getGroupId, groupId)
//      .in(SysUserGroup::getUserId, userIds)
//      .remove();
    entityQuery.deletable(SysUserGroup.class)
      .allowDeleteStatement(true)
      .where(userGroup -> {
        userGroup.groupId().eq(groupId);
        userGroup.userId().in(userIds);
      }).executeRows();

  }

  /**
   * 根据用户ID查询群组ID列表
   *
   * @param userId 用户ID
   * @return 群组ID列表
   */
  @Override
  public List<Long> queryGroupIdsByUserId(final Long userId) {
//    return lambdaQuery()
//      .eq(SysUserGroup::getUserId, userId)
//      .list()
//      .stream()
//      .map(SysUserGroup::getGroupId)
//      .collect(Collectors.toList());
    return entityQuery.queryable(SysUserGroup.class)
      .where(userGroup -> userGroup.userId().eq(userId))
      .select(SysUserGroupProxy::groupId)
      .toList();
  }

  /**
   * 根据群组ID查询用户ID列表
   *
   * @param groupId 群组ID
   * @return 用户ID列表
   */
  @Override
  public List<Long> queryUserIdsByGroupId(final Long groupId) {
//    return lambdaQuery()
//      .eq(SysUserGroup::getGroupId, groupId)
//      .list()
//      .stream().map(SysUserGroup::getUserId)
//      .collect(Collectors.toList());
    return entityQuery.queryable(SysUserGroup.class)
      .where(userGroup -> userGroup.groupId().eq(groupId))
      .select(SysUserGroupProxy::userId)
      .toList();
  }
}
