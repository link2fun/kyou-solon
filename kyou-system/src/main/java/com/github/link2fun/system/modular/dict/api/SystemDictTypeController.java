package com.github.link2fun.system.modular.dict.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysDictTypeDTO;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeAddReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeModifyReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictTypeSearchReq;
import com.github.link2fun.system.modular.dict.service.ISystemDictTypeService;

import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

/**
 * 数据字典信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/dict/type")
public class SystemDictTypeController {
  @Inject
  private ISystemDictTypeService dictTypeService;

  @SaCheckPermission("system:dict:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysDictTypeSearchReq searchReq) {

    Page<SysDictTypeDTO> list = dictTypeService.pageSearch(Page.ofCurrentContext(), searchReq, SysDictTypeDTO.class);
    return TableDataInfo.of(list);
  }

  @Log(title = "字典类型", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:dict:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysDictTypeSearchReq searchReq) {
    List<SysDictTypeDTO> list = dictTypeService.selectDictTypeList(searchReq, SysDictTypeDTO.class);
    ExcelUtil<SysDictTypeDTO> util = new ExcelUtil<>(SysDictTypeDTO.class);
    util.exportExcel(response, list, "字典类型");
  }

  /**
   * 查询字典类型详细
   */
  @SaCheckPermission("system:dict:query")
  @Mapping(value = "/{dictId}", method = MethodType.GET)
  public Resp<SysDictTypeDTO> getInfo(@Path Long dictId) {
    return Resp.successData(dictTypeService.selectDictTypeById(dictId, SysDictTypeDTO.class));
  }

  /**
   * 新增字典类型
   */
  @SaCheckPermission("system:dict:add")
  @Log(title = "字典类型", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysDictTypeAddReq addReq) {
    return Resp.ofAffected(dictTypeService.insertDictType(addReq));
  }

  /**
   * 修改字典类型
   */
  @SaCheckPermission("system:dict:edit")
  @Log(title = "字典类型", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysDictTypeModifyReq modifyReq) {
    return Resp.ofAffected(dictTypeService.updateDictType(modifyReq));
  }

  /**
   * 删除字典类型
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.DELETE)
  @Mapping(value = "/{dictIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> dictIds) {
    dictTypeService.deleteDictTypeByIds(dictIds);
    return Resp.success();
  }

  /**
   * 刷新字典缓存
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.CLEAN)
  @Mapping(value = "/refreshCache", method = MethodType.DELETE)
  public Resp<Void> refreshCache() {
    dictTypeService.resetDictCache();
    return Resp.success();
  }

  /**
   * 获取字典选择框列表
   */
  @Mapping(value = "/optionselect", method = MethodType.GET)
  public Resp<List<SysDictTypeDTO>> optionselect() {
    List<SysDictTypeDTO> dictTypes = dictTypeService.selectDictTypeAll(SysDictTypeDTO.class);
    return Resp.successData(dictTypes);
  }
}
