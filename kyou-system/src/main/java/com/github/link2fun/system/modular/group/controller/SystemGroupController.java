package com.github.link2fun.system.modular.group.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.group.model.dto.SysGroupDTO;
import com.github.link2fun.system.modular.group.model.req.SysGroupAddReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupModifyReq;
import com.github.link2fun.system.modular.group.model.req.SysGroupSearchReq;
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
public class SystemGroupController {


  @Inject
  private ISystemGroupService groupService;


  /** 获取群组列表 */
  @SaCheckPermission("system.group.list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysGroupSearchReq searchReq) {
    final Page<SysGroupDTO> page = groupService.pageSearchGroup(Page.ofCurrentContext(), searchReq, SysGroupDTO.class);

    return TableDataInfo.of(page);
  }


  /** 导出群组信息 */
  @Log(title = "群组管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system.group.export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysGroupSearchReq searchReq) {
    final Page<SysGroupDTO> page = groupService.pageSearchGroup(Page.ofAll(), searchReq, SysGroupDTO.class);
    ExcelUtil<SysGroupDTO> util = new ExcelUtil<>(SysGroupDTO.class);
    util.exportExcel(context, page.getRecords(), "群组数据");
  }


  /** 根据群组编号获取详细信息 */
  @SaCheckPermission("system.group.query")
  @Mapping(value = "/{groupId}", method = MethodType.GET)
  public Resp<SysGroupDTO> getInfo(@Path Long groupId) {
    return Resp.successData(groupService.getById(groupId, SysGroupDTO.class));
  }


  /** 新增群组 */
  @SaCheckPermission("system.group.add")
  @Mapping(value = "/add", method = MethodType.POST)
  @Log(title = "群组管理", businessType = BusinessType.INSERT)
  public Resp<Void> add(@Validated @Body SysGroupAddReq addReq) {
    return Resp.ofAffected(groupService.insertGroup(addReq));
  }


  /** 修改群组 */
  @SaCheckPermission("system.group.edit")
  @Mapping(value = "/edit", method = MethodType.POST)
  public Resp<Void> edit(@Validated @Body SysGroupModifyReq modifyReq) {
    return Resp.ofAffected(groupService.updateGroup(modifyReq));
  }


  /** 删除群组 */
  @SaCheckPermission("system.group.remove")
  @Mapping(value = "/{groupIds}", method = MethodType.DELETE)
  public Resp<Void> remove(List<Long> groupIds) {
    return Resp.ofAffected(groupService.deleteGroupByIds(groupIds));
  }


  /** 群组选择框列表 */
  @Mapping(value = "/optionSelect", method = MethodType.GET)
  public Resp<List<SysGroupDTO>> optionSelect() {
    return Resp.successData(groupService.listAll(SysGroupDTO.class));
  }


}
