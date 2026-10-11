package com.github.link2fun.system.modular.usergroup.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.support.easyquery.MappingSync;
import com.github.link2fun.system.modular.usergroup.service.ISystemUserGroupService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.data.annotation.Transaction;

import java.util.List;

/** 用户和群组关联表 服务实现类 */
@Component
public class SystemUserGroupServiceImpl
  implements ISystemUserGroupService {

  @Inject
  private MappingSync mappingSync;

  @Inject
  private UserGroupMapping userGroupMapping;

  @Inject
  private GroupUserMapping groupUserMapping;

  /**
   * 重新分配用户的群组, 用户最终持有的群组与 groupIds 完全一致
   *
   * @param userId   用户ID
   * @param groupIds 群组ID集合, null 或空集合表示清空
   */
  @Override
  public void reassignGroups(final Long userId, final List<Long> groupIds) {
    mappingSync.sync(userGroupMapping, userId, groupIds);
  }

  /**
   * 批量为群组分配用户, 已有关系保持不变
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void assignUsers(final Long groupId, final List<Long> userIds) {
    mappingSync.add(groupUserMapping, groupId, userIds);
  }

  /**
   * 批量取消群组的用户授权
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void unassignUsers(final Long groupId, final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    groupUserMapping.unlink(groupId, userIds);
  }

  /**
   * 根据用户ID查询群组ID集合
   *
   * @param userId 用户ID
   * @return 群组ID集合
   */
  @Override
  public List<Long> findGroupIdsByUserId(final Long userId) {
    return userGroupMapping.findTargetIds(userId);
  }

  /**
   * 根据群组ID查询用户ID集合
   *
   * @param groupId 群组ID
   * @return 用户ID集合
   */
  @Override
  public List<Long> findUserIdsByGroupId(final Long groupId) {
    return groupUserMapping.findTargetIds(groupId);
  }

  /** 按用户 ID 批量解除用户-群组关联（用户消亡时静默解绑） */
  @Override
  @Transaction
  public void removeByUserIds(final List<Long> userIds) {
    userGroupMapping.unlinkAll(userIds);
  }

  /** 批量删除群组的用户关联信息（群组消亡时静默解绑） */
  @Override
  @Transaction
  public void removeByGroupIds(final List<Long> groupIds) {
    groupUserMapping.unlinkAll(groupIds);
  }
}
