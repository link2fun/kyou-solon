package com.github.link2fun.config.satoken;

import cn.dev33.satoken.stp.StpInterface;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import com.github.link2fun.system.modular.role.service.ISystemRoleService;
import com.github.link2fun.support.context.action.tool.SaSessionBizTool;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;

@Slf4j
@Component
public class StpInterfaceImpl implements StpInterface {

  @Inject
  private ISystemMenuService menuService;

  @Inject
  private ISystemRoleService roleService;

  /**
   * 返回指定账号id所拥有的权限码集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的权限码集合
   * @link <a href="https://sa-token.cc/doc.html#/fun/jur-cache">参考：将权限数据放在缓存里</a>
   */
  @Override
  public List<String> getPermissionList(final Object loginId, final String loginType) {

    return SaSessionBizTool.getPermissionList(loginId, loginType);
  }

  /**
   * 返回指定账号id所拥有的角色标识集合
   *
   * @param loginId   账号id
   * @param loginType 账号类型
   * @return 该账号id具有的角色标识集合
   * @link <a href="https://sa-token.cc/doc.html#/fun/jur-cache">参考：将权限数据放在缓存里</a>
   */
  @Override
  public List<String> getRoleList(final Object loginId, final String loginType) {

    return SaSessionBizTool.getRoleList(loginId, loginType);
  }
}
