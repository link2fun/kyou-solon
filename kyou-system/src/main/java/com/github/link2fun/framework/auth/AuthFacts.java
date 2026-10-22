package com.github.link2fun.framework.auth;

import java.util.Collection;
import java.util.List;

/**
 * 授权事实读端口: 谁持有这些角色、哪些角色引用了这些菜单。
 */
public interface AuthFacts {

  /** 查询角色当前的 roleKey */
  List<String> roleKeysByRoleIds(Collection<Long> roleIds);

  /** 查询持有这些角色的用户 ID */
  List<Long> holderUserIdsByRoleIds(Collection<Long> roleIds);

  /** 查询引用了这些菜单的角色标识 */
  List<String> roleKeysReferencingMenus(Collection<Long> menuIds);
}
