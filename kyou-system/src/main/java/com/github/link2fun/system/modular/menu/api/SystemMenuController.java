package com.github.link2fun.system.modular.menu.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.convert.Convert;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.TreeSelect;
import com.github.link2fun.support.core.domain.dto.SysMenuDTO;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.system.modular.menu.model.req.SysMenuAddReq;
import com.github.link2fun.system.modular.menu.model.req.SysMenuModifyReq;
import com.github.link2fun.system.modular.menu.model.req.SysMenuSearchReq;
import com.github.link2fun.system.modular.menu.model.vo.MenuTreeSelectResponse;
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
public class SystemMenuController {
  @Inject
  private ISystemMenuService menuService;

  /**
   * 获取菜单列表
   */
  @SaCheckPermission("system:menu:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public Resp<List<SysMenuDTO>> list(SysMenuSearchReq searchReq) {
    List<SysMenuDTO> menus = menuService.selectMenuList(searchReq, ActionContext.current().getUserId(), SysMenuDTO.class);
    return Resp.successData(menus);
  }

  /**
   * 根据菜单编号获取详细信息
   */
  @SaCheckPermission("system:menu:query")
  @Mapping(value = "/{menuId}", method = MethodType.GET)
  public Resp<SysMenuDTO> getInfo(@Path Long menuId) {
    return Resp.successData(menuService.selectMenuById(menuId, SysMenuDTO.class));
  }

  /**
   * 获取菜单下拉树列表
   */
  @Mapping(value = "/treeselect", method = MethodType.GET)
  public Resp<List<TreeSelect>> treeselect(SysMenuSearchReq searchReq) {
    List<SysMenuDTO> menus = menuService.selectMenuList(searchReq, ActionContext.current().getUserId(), SysMenuDTO.class);
    return Resp.successData(menuService.buildMenuTreeSelect(menus));
  }

  /**
   * 加载对应角色菜单列表树
   */
  @Mapping(value = "/roleMenuTreeselect/{roleId}", method = MethodType.GET)
  public MenuTreeSelectResponse roleMenuTreeselect(@Path("roleId") Long roleId) {
    List<SysMenuDTO> menus = menuService.selectMenuList(ActionContext.current().getUserId(), SysMenuDTO.class);
    MenuTreeSelectResponse response = new MenuTreeSelectResponse();
    response.setCheckedKeys(menuService.selectMenuListByRoleId(roleId).stream().map(Convert::toStr).collect(Collectors.toList()));
    response.setMenus(menuService.buildMenuTreeSelect(menus));
    return response;
  }

  /**
   * 新增菜单
   */
  @SaCheckPermission("system:menu:add")
  @Log(title = "菜单管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysMenuAddReq addReq) {
    return Resp.ofAffected(menuService.insertMenu(addReq));
  }

  /**
   * 修改菜单
   */
  @SaCheckPermission("system:menu:edit")
  @Log(title = "菜单管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysMenuModifyReq modifyReq) {
    return Resp.ofAffected(menuService.updateMenu(modifyReq));
  }

  /**
   * 删除菜单
   */
  @SaCheckPermission("system:menu:remove")
  @Log(title = "菜单管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{menuId}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path("menuId") Long menuId) {
    return Resp.ofAffected(menuService.deleteMenuById(menuId));
  }
}