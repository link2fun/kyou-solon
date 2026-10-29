package com.github.link2fun.system.modular.config.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.domain.Resp;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.config.model.dto.SysConfigDTO;
import com.github.link2fun.system.modular.config.model.req.SysConfigAddReq;
import com.github.link2fun.system.modular.config.model.req.SysConfigModifyReq;
import com.github.link2fun.system.modular.config.model.req.SysConfigSearchReq;
import com.github.link2fun.system.modular.config.service.ISystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.util.List;

@Slf4j
@Controller
@Mapping("/system/config")
public class SystemConfigController {

  @Inject
  private ISystemConfigService configService;

  /**
   * 获取参数配置列表
   */
  @SaCheckPermission("system:config:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysConfigSearchReq searchReq) {
    Page<SysConfigDTO> page = configService.pageSearchConfig(Page.ofCurrentContext(), searchReq, SysConfigDTO.class);
    return TableDataInfo.of(page);
  }

  // 搜索条件可带 configValue，导出接口也要脱敏
  @Log(title = "参数管理", businessType = BusinessType.EXPORT, mask = true)
  @SaCheckPermission("system:config:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysConfigSearchReq searchReq) {
    List<SysConfigDTO> list = configService.selectConfigList(searchReq, SysConfigDTO.class);
    ExcelUtil<SysConfigDTO> util = new ExcelUtil<>(SysConfigDTO.class);
    util.exportExcel(context, list, "参数数据");
  }

  /**
   * 根据参数编号获取详细信息
   */
  @SaCheckPermission("system:config:query")
  @Mapping(value = "/{configId}", method = MethodType.GET)
  public Resp<SysConfigDTO> getInfo(@Path Integer configId) {
    SysConfigDTO config = configService.findOneByConfigId(configId, SysConfigDTO.class);
    return Resp.successData(config);
  }

  /**
   * 根据参数键名查询参数值
   */
  @Mapping(value = "/configKey/{configKey}", method = MethodType.GET)
  public Resp<String> getConfigKey(@Path String configKey) {
    return Resp.successData(configService.selectConfigByKey(configKey));
  }

  /**
   * 新增参数配置
   */
  @SaCheckPermission("system:config:add")
  @Log(title = "参数管理", businessType = BusinessType.INSERT, mask = true)
  @Mapping(method = MethodType.POST)
  public Resp<Void> add(@Validated @Body SysConfigAddReq addReq) {
    return Resp.ofAffected(configService.insertConfig(addReq));
  }

  /**
   * 修改参数配置
   */
  @SaCheckPermission("system:config:edit")
  @Log(title = "参数管理", businessType = BusinessType.UPDATE, mask = true)
  @Mapping(method = MethodType.PUT)
  public Resp<Void> edit(@Validated @Body SysConfigModifyReq modifyReq) {
    return Resp.ofAffected(configService.updateConfig(modifyReq));
  }

  /**
   * 删除参数配置
   */
  @SaCheckPermission("system:config:remove")
  @Log(title = "参数管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{configIds}", method = MethodType.DELETE)
  public Resp<Void> remove(@Path List<Long> configIds) {
    configService.deleteConfigByIds(configIds);
    return Resp.success();
  }

  /**
   * 刷新参数缓存
   */
  @SaCheckPermission("system:config:remove")
  @Log(title = "参数管理", businessType = BusinessType.CLEAN)
  @Mapping(value = "/refreshCache", method = MethodType.DELETE)
  public Resp<Void> refreshCache() {
    configService.resetConfigCache();
    return Resp.success();
  }

}
