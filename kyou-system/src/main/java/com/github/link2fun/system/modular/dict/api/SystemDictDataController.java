package com.github.link2fun.system.modular.dict.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.domain.entity.SysDictData;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.StringUtils;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dict.service.ISystemDictDataService;
import com.github.link2fun.system.modular.dict.service.ISystemDictTypeService;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据字典信息
 *
 * @author ruoyi
 */
@Controller
@Mapping("/system/dict/data")
public class SystemDictDataController extends BaseController {
  @Inject
  private ISystemDictDataService dictDataService;

  @Inject
  private ISystemDictTypeService dictTypeService;

  @SaCheckPermission("system:dict:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysDictData dictData) {
    Page<SysDictData> page = dictDataService.pageSearch(getPage(), dictData);
    return getDataTable(page);
  }

  @Log(title = "字典数据", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:dict:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysDictData dictData) {
    List<SysDictData> list = dictDataService.selectDictDataList(dictData);
    ExcelUtil<SysDictData> util = new ExcelUtil<>(SysDictData.class);
    util.exportExcel(response, list, "字典数据");
  }

  /**
   * 查询字典数据详细
   */
  @SaCheckPermission("system:dict:query")
  @Mapping(value = "/{dictCode}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long dictCode) {
    return success(dictDataService.selectDictDataById(dictCode));
  }

  /**
   * 根据字典类型查询字典数据信息
   */
  @Mapping(value = "/type/{dictType}", method = MethodType.GET)
  public AjaxResult dictType(@Path String dictType) {
    List<SysDictData> data = dictTypeService.selectDictDataByType(dictType);
    if (StringUtils.isNull(data)) {
      data = new ArrayList<>();
    }
    return success(data);
  }

  /**
   * 新增字典值
   */
  @SaCheckPermission("system:dict:add")
  @Log(title = "字典数据", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysDictData dict) {
    dict.setCreateBy(getUsername());
    return toAjax(dictDataService.insertDictData(dict));
  }

  /**
   * 修改保存字典类型
   */
  @SaCheckPermission("system:dict:edit")
  @Log(title = "字典数据", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysDictData dict) {
    dict.setUpdateBy(getUsername());
    return toAjax(dictDataService.updateDictData(dict));
  }

  /**
   * 删除字典类型
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.DELETE)
  @Mapping(value = "/{dictCodes}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> dictCodes) {
    dictDataService.deleteDictDataByIds(dictCodes);
    return success();
  }
}
