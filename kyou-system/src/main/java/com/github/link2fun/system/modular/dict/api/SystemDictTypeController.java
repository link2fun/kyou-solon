package com.github.link2fun.system.modular.dict.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysDictType;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
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
public class SystemDictTypeController extends BaseController {
  @Inject
  private ISystemDictTypeService dictTypeService;

  @SaCheckPermission("system:dict:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysDictType searchReq) {

    Page<SysDictType> list = dictTypeService.pageSearch(Page.ofCurrentContext(), searchReq);
    return getDataTable(list);
  }

  @Log(title = "字典类型", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:dict:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysDictType dictType) {
    List<SysDictType> list = dictTypeService.selectDictTypeList(dictType);
    ExcelUtil<SysDictType> util = new ExcelUtil<>(SysDictType.class);
    util.exportExcel(response, list, "字典类型");
  }

  /**
   * 查询字典类型详细
   */
  @SaCheckPermission("system:dict:query")
  @Mapping(value = "/{dictId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long dictId) {
    return success(dictTypeService.selectDictTypeById(dictId));
  }

  /**
   * 新增字典类型
   */
  @SaCheckPermission("system:dict:add")
  @Log(title = "字典类型", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysDictType dict) {
    if (!dictTypeService.checkDictTypeUnique(dict)) {
      return error("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
    }
    dict.setCreateBy(getUsername());
    return toAjax(dictTypeService.insertDictType(dict));
  }

  /**
   * 修改字典类型
   */
  @SaCheckPermission("system:dict:edit")
  @Log(title = "字典类型", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysDictType dict) {
    if (!dictTypeService.checkDictTypeUnique(dict)) {
      return error("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
    }
    dict.setUpdateBy(getUsername());
    return toAjax(dictTypeService.updateDictType(dict));
  }

  /**
   * 删除字典类型
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.DELETE)
  @Mapping(value = "/{dictIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> dictIds) {
    dictTypeService.deleteDictTypeByIds(dictIds);
    return success();
  }

  /**
   * 刷新字典缓存
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.CLEAN)
  @Mapping(value = "/refreshCache", method = MethodType.DELETE)
  public AjaxResult refreshCache() {
    dictTypeService.resetDictCache();
    return success();
  }

  /**
   * 获取字典选择框列表
   */
  @Mapping(value = "/optionselect", method = MethodType.GET)
  public AjaxResult optionselect() {
    List<SysDictType> dictTypes = dictTypeService.selectDictTypeAll();
    return success(dictTypes);
  }
}
