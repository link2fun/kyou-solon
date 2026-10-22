package com.github.link2fun.framework.auth;

import java.util.Collection;

/**
 * 授权变更后的会话缓存失效编排器: 调用方声明「什么变了」, 失效图由实现裁定。
 *
 * <p>须在库写成功后调用; 事务内推迟到提交后执行, 无事务立即执行;
 * 失败只记 error 不上抛; 空集合入参不产生动作。
 */
public interface AuthCacheInvalidator {

  /** 用户资料 / 角色授权 / 部门变更: 失效会话缓存, 下次请求从库重建 */
  void onUsersChanged(Collection<Long> userIds);

  /** 停用 / 改密 / 删除: 强退全部在线会话 */
  void onUsersKicked(Collection<Long> userIds);

  /** 角色名称 / 标识 / 状态 / 数据范围变更: 失效新旧 roleKey 与全部持有者; roleIds 为空(删除场景)只按旧标识失效 */
  void onRolesChanged(Collection<Long> roleIds, Collection<String> obsoleteRoleKeys);

  /** 菜单名称 / 权限码 / 状态变更: 失效引用这些菜单的角色 */
  void onMenusChanged(Collection<Long> menuIds);
}
