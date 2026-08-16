package com.github.link2fun.system.modular.group.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.group.model.SysGroup;
import com.github.link2fun.system.modular.group.service.ISystemGroupService;

import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

/**
 * 群组信息操作处理
 *
 * @author link2fun
 */
@Valid
@Controller
@Mapping("/system/group")
public class SystemGroupController extends BaseController {


  @Inject
  private ISystemGroupService groupService;


  /** 获取群组列表 */
  @SaCheckPermission("system.group.list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysGroup searchReq) {
    final Page<SysGroup> page = groupService.pageSearchGroup(Page.ofCurrentContext(), searchReq);

    return getDataTable(page);
  }


  /** 导出群组信息 */
  @Log(title = "群组管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system.group.export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysGroup searchReq) {
    final Page<SysGroup> page = groupService.pageSearchGroup(Page.ofAll(), searchReq);
    ExcelUtil<SysGroup> util = new ExcelUtil<>(SysGroup.class);
    util.exportExcel(context, page.getRecords(), "群组数据");
  }


  /** 根据群组编号获取详细信息 */
  @SaCheckPermission("system.group.query")
  @Mapping(value = "/{groupId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long groupId) {
    return successData(groupService.getById(groupId));
  }


  /** 新增群组 */
  @SaCheckPermission("system.group.add")
  @Mapping(value = "/add", method = MethodType.POST)
  @Log(title = "群组管理", businessType = BusinessType.INSERT)
  public AjaxResult add(@Validated @Body SysGroup group) {
    return toAjax(groupService.insertGroup(group));
  }


  /** 修改群组 */
  @SaCheckPermission("system.group.edit")
  @Mapping(value = "/edit", method = MethodType.POST)
  public AjaxResult edit(SysGroup group) {
    group.setUpdateBy(getUsername());
    return toAjax(groupService.updateGroup(group));
  }


  /** 删除群组 */
  @SaCheckPermission("system.group.remove")
  @Mapping(value = "/{groupIds}", method = MethodType.DELETE)
  public AjaxResult remove(List<Long> groupIds) {
    return toAjax(groupService.deleteGroupByIds(groupIds));
  }


  /** 群组选择框列表 */
  @Mapping(value = "/optionSelect", method = MethodType.GET)
  public AjaxResult optionSelect() {
    return successData(groupService.listAll());
  }


}
