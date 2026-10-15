package com.github.link2fun.system.modular.menu.util;

import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.core.tree.TreeBuilder;

import java.util.List;

public class MenuUtils {

  /** 内链域名中的特殊字符, 与 {@link #INNER_LINK_REPLACEMENT} 一一对应 */
  private static final String[] INNER_LINK_SEARCH = {Constants.HTTP, Constants.HTTPS, Constants.WWW, ".", ":"};

  /** 内链特殊字符的替换值, 与 {@link #INNER_LINK_SEARCH} 一一对应 */
  private static final String[] INNER_LINK_REPLACEMENT = {"", "", "", "/", "/"};

  /**
   * 获取路由名称
   *
   * @param menu 菜单信息
   * @return 路由名称
   */
  public static String getRouteName(SysMenu menu) {
    String routerName = NamingCase.toPascalCase(menu.getPath());
    // 非外链并且是一级目录（类型为目录）
    if (isMenuFrame(menu)) {
      routerName = StrUtil.EMPTY;
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
    if (StrUtil.isNotBlank(menu.getComponent()) && !isMenuFrame(menu)) {
      component = menu.getComponent();
    } else if (StrUtil.isBlank(menu.getComponent()) && menu.getParentId().intValue() != 0 && isInnerLink(menu)) {
      component = UserConstants.INNER_LINK;
    } else if (StrUtil.isBlank(menu.getComponent()) && isParentView(menu)) {
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
    return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StrUtil.startWithAny(menu.getPath(), Constants.HTTP, Constants.HTTPS);
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
    String result = path;
    for (int i = 0; i < INNER_LINK_SEARCH.length; i++) {
      result = StrUtil.replace(result, INNER_LINK_SEARCH[i], INNER_LINK_REPLACEMENT[i]);
    }
    return result;
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
