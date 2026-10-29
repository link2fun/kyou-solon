package com.github.link2fun.system.modular.dict.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.domain.dto.SysDictDataDTO;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataAddReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataModifyReq;
import com.github.link2fun.system.modular.dict.model.req.SysDictDataSearchReq;
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
public class SystemDictDataController {
  @Inject
  private ISystemDictDataService dictDataService;

  @Inject
  private ISystemDictTypeService dictTypeService;

  @SaCheckPermission("system:dict:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysDictDataSearchReq searchReq) {
    Page<SysDictDataDTO> page = dictDataService.pageSearch(Page.ofCurrentContext(), searchReq, SysDictDataDTO.class);
    return TableDataInfo.of(page);
  }

  @Log(title = "字典数据", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:dict:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context response, SysDictDataSearchReq searchReq) {
    List<SysDictDataDTO> list = dictDataService.selectDictDataList(searchReq, SysDictDataDTO.class);
    ExcelUtil<SysDictDataDTO> util = new ExcelUtil<>(SysDictDataDTO.class);
    util.exportExcel(response, list, "字典数据");
  }

  /**
   * 查询字典数据详细
   */
  @SaCheckPermission("system:dict:query")
  @Mapping(value = "/{dictCode}", method = MethodType.GET)
  public Resp<SysDictDataDTO> getInfo(@Path Long dictCode) {
    return Resp.successData(dictDataService.selectDictDataById(dictCode, SysDictDataDTO.class));
  }

  /**
   * 根据字典类型查询字典数据信息
   */
  @Mapping(value = "/type/{dictType}", method = MethodType.GET)
  public Resp<List<SysDictDataDTO>> dictType(@Path String dictType) {
    List<SysDictDataDTO> data = dictTypeService.selectDictDataByType(dictType);
    if (ObjectUtil.isNull(data)) {
      data = new ArrayList<>();
    }
    return Resp.successData(data);
  }

  /**
   * 新增字典值
   */
  @SaCheckPermission("system:dict:add")
  @Log(title = "字典数据", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysDictDataAddReq addReq) {
    return Resp.ofAffected(dictDataService.insertDictData(addReq));
  }

  /**
   * 修改保存字典类型
   */
  @SaCheckPermission("system:dict:edit")
  @Log(title = "字典数据", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysDictDataModifyReq modifyReq) {
    return Resp.ofAffected(dictDataService.updateDictData(modifyReq));
  }

  /**
   * 删除字典类型
   */
  @SaCheckPermission("system:dict:remove")
  @Log(title = "字典类型", businessType = BusinessType.DELETE)
  @Mapping(value = "/{dictCodes}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> dictCodes) {
    dictDataService.deleteDictDataByIds(dictCodes);
    return Resp.success();
  }
}
