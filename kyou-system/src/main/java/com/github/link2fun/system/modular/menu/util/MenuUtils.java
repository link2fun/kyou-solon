package com.github.link2fun.system.modular.menu.util;

import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MenuUtils {
  /**
   * 递归列表
   *
   * @param list 分类表
   * @param t    子节点
   */
  public static void recursionFn(List<SysMenu> list, SysMenu t) {
    // 得到子节点列表
    List<SysMenu> childList = getChildList(list, t);
    t.setChildren(childList);
    for (SysMenu tChild : childList) {
      if (hasChild(list, tChild)) {
        recursionFn(list, tChild);
      }
    }
  }

  /**
   * 得到子节点列表
   */
  public static List<SysMenu> getChildList(List<SysMenu> list, SysMenu t) {
    List<SysMenu> tlist = new ArrayList<>();
    for (final SysMenu n : list) {
      if (n.getParentId().longValue() == t.getMenuId().longValue()) {
        tlist.add(n);
      }
    }
    return tlist;
  }

  /**
   * 判断是否有子节点
   */
  public static boolean hasChild(List<SysMenu> list, SysMenu t) {
    return !getChildList(list, t).isEmpty();
  }

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
   * 根据父节点的ID获取所有子节点
   *
   * @param list     分类表
   * @param parentId 传入的父节点ID
   * @return String
   */
  public static List<SysMenu> getChildPerms(List<SysMenu> list, int parentId) {
    List<SysMenu> returnList = new ArrayList<>();
    for (SysMenu t : list) {
      // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
      if (t.getParentId() == parentId) {
        recursionFn(list, t);
        returnList.add(t);
      }
    }
    return returnList;
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
