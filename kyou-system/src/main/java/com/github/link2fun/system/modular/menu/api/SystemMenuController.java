package com.github.link2fun.system.modular.menu.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.constant.UserConstants;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysMenu;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.system.modular.menu.service.ISystemMenuService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 菜单信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/menu")
public class SystemMenuController extends BaseController {
  @Inject
  private ISystemMenuService menuService;

  /**
   * 获取菜单列表
   */
  @SaCheckPermission("system:menu:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public AjaxResult list(SysMenu menu) {
    List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
    return success(menus);
  }

  /**
   * 根据菜单编号获取详细信息
   */
  @SaCheckPermission("system:menu:query")
  @Mapping(value = "/{menuId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long menuId) {
    return success(menuService.selectMenuById(menuId));
  }

  /**
   * 获取菜单下拉树列表
   */
  @Mapping(value = "/treeselect", method = MethodType.GET)
  public AjaxResult treeselect(SysMenu menu) {
    List<SysMenu> menus = menuService.selectMenuList(menu, getUserId());
    return success(menuService.buildMenuTreeSelect(menus));
  }

  /**
   * 加载对应角色菜单列表树
   */
  @Mapping(value = "/roleMenuTreeselect/{roleId}", method = MethodType.GET)
  public AjaxResult roleMenuTreeselect(@Path("roleId") Long roleId) {
    List<SysMenu> menus = menuService.selectMenuList(getUserId());
    AjaxResult ajax = AjaxResult.success();
    ajax.put("checkedKeys", menuService.selectMenuListByRoleId(roleId).stream().map(Convert::toStr).collect(Collectors.toList()));
    ajax.put("menus", menuService.buildMenuTreeSelect(menus));
    return ajax;
  }

  /**
   * 新增菜单
   */
  @SaCheckPermission("system:menu:add")
  @Log(title = "菜单管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysMenu menu) {
    if (!menuService.checkMenuNameUnique(menu)) {
      return error("新增菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
      return error("新增菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    }
    menu.setCreateBy(getUsername());
    return toAjax(menuService.insertMenu(menu));
  }

  /**
   * 修改菜单
   */
  @SaCheckPermission("system:menu:edit")
  @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysMenu menu) {
    if (!menuService.checkMenuNameUnique(menu)) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，菜单名称已存在");
    } else if (UserConstants.YES_FRAME.equals(menu.getIsFrame()) && !StringUtils.ishttp(menu.getPath())) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，地址必须以http(s)://开头");
    } else if (menu.getMenuId().equals(menu.getParentId())) {
      return error("修改菜单'" + menu.getMenuName() + "'失败，上级菜单不能选择自己");
    }
    menu.setUpdateBy(getUsername());
    return toAjax(menuService.updateMenu(menu));
  }

  /**
   * 删除菜单
   */
  @SaCheckPermission("system:menu:remove")
  @Log(title = "菜单管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{menuId}", method = MethodType.DELETE)
  public AjaxResult remove(@Path("menuId") Long menuId) {
    if (menuService.hasChildByMenuId(menuId)) {
      return warn("存在子菜单,不允许删除");
    }
    if (menuService.checkMenuExistRole(menuId)) {
      return warn("菜单已分配,不允许删除");
    }
    return toAjax(menuService.deleteMenuById(menuId));
  }
}