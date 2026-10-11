package com.github.link2fun.system.modular.menu.util;

import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.core.tree.TreeBuilder;
import com.github.link2fun.support.utils.StringUtils;

import java.util.List;

public class MenuUtils {

  /**
   * 获取路由名称
   *
   * @param menu 菜单信息
   * @return 路由名称
   */
  public static String getRouteName(SysMenu menu) {
    String routerName = StringUtils.capitalize(menu.getPath());
    // 非外链并且是一级目录（类型为目录）
    if (isMenuFrame(menu)) {
      routerName = StringUtils.EMPTY;
    }
    return routerName;
  }

  /**
   * 获取组件信息
   *
   * @param menu 菜单信息
   * @return 组件信息
   */
  public static String getComponent(SysMenu menu) {
    String component = UserConstants.LAYOUT;
    if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
      component = menu.getComponent();
    } else if (StringUtils.isEmpty(menu.getComponent()) && menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
      component = UserConstants.INNER_LINK;
    } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
      component = UserConstants.PARENT_VIEW;
    }
    return component;
  }

  /**
   * 是否为菜单内部跳转
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public static boolean isMenuFrame(SysMenu menu) {
    return menu.getParentId().intValue() == 0 && UserConstants.TYPE_MENU.equals(menu.getMenuType())
      && menu.getIsFrame().equals(UserConstants.NO_FRAME);
  }

  /**
   * 是否为内链组件
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public static boolean isInnerLink(SysMenu menu) {
    return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.ishttp(menu.getPath());
  }

  /**
   * 是否为parent_view组件
   *
   * @param menu 菜单信息
   * @return 结果
   */
  public static boolean isParentView(SysMenu menu) {
    return menu.getParentId().intValue() != 0 && UserConstants.TYPE_DIR.equals(menu.getMenuType());
  }

  /**
   * 构建路由树
   *
   * @param list     菜单列表
   * @param parentId 顶级节点的父ID
   * @return 路由树
   */
  public static List<SysMenu> getChildPerms(List<SysMenu> list, Long parentId) {
    return TreeBuilder.buildByRootParent(list, parentId,
      SysMenu::getMenuId, SysMenu::getParentId, SysMenu::setChildren);
  }

  /**
   * 内链域名特殊字符替换
   *
   * @return 替换后的内链域名
   */
  public static String innerLinkReplaceEach(String path) {
    return StringUtils.replaceEach(path, new String[]{Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"},
      new String[]{"", "", "", "/", "/"});
  }

  /**
   * 获取路由地址
   *
   * @param menu 菜单信息
   * @return 路由地址
   */
  public static String getRouterPath(SysMenu menu) {
    String routerPath = menu.getPath();
    // 内链打开外网方式
    if (menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
      routerPath = innerLinkReplaceEach(routerPath);
    }
    // 非外链并且是一级目录（类型为目录）
    if (0 == menu.getParentId().intValue() && UserConstants.TYPE_DIR.equals(menu.getMenuType())
      && UserConstants.NO_FRAME.equals(menu.getIsFrame())) {
      routerPath = "/" + menu.getPath();
    }
    // 非外链并且是一级目录（类型为菜单）
    else if (isMenuFrame(menu)) {
      routerPath = "/";
    }
    return routerPath;
  }
}
