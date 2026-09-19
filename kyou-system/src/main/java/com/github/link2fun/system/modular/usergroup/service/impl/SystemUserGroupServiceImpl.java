package com.github.link2fun.system.modular.usergroup.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.system.modular.usergroup.service.ISystemUserGroupService;
import com.github.link2fun.support.easyquery.MappingSync;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

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
   * 向群组中添加多个用户, 已有关系保持不变
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void addUsers(final Long groupId, final List<Long> userIds) {
    mappingSync.add(groupUserMapping, groupId, userIds);
  }

  /**
   * 从群组中移除多个用户
   *
   * @param groupId 群组ID
   * @param userIds 用户ID List
   */
  @Override
  public void removeUsers(final Long groupId, final List<Long> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return;
    }
    groupUserMapping.unlink(groupId, userIds);
  }

  /**
   * 根据用户ID查询群组ID列表
   *
   * @param userId 用户ID
   * @return 群组ID列表
   */
  @Override
  public List<Long> queryGroupIdsByUserId(final Long userId) {
    return userGroupMapping.findTargetIds(userId);
  }

  /**
   * 根据群组ID查询用户ID列表
   *
   * @param groupId 群组ID
   * @return 用户ID列表
   */
  @Override
  public List<Long> queryUserIdsByGroupId(final Long groupId) {
    return groupUserMapping.findTargetIds(groupId);
  }
}
