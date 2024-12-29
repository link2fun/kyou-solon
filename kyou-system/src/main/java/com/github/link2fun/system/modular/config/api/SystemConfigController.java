package com.github.link2fun.system.modular.config.api;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.context.action.ActionContext;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.enums.BusinessType;
import com.github.link2fun.support.utils.poi.ExcelUtil;
import com.github.link2fun.system.modular.config.model.SysConfig;
import com.github.link2fun.system.modular.config.model.vo.SysConfigDetailVO;
import com.github.link2fun.system.modular.config.model.vo.SysConfigPageVO;
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
public class SystemConfigController extends BaseController {

  @Inject
  private ISystemConfigService configService;

  /**
   * 获取参数配置列表
   */
  @SaCheckPermission("system:config:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo list(SysConfig searchReq) {
    ActionContext context = ActionContext.current(); // 加载上下文
    Page<SysConfigPageVO> page = configService.pageSearchConfig(context, Page.ofCurrentRequest(), searchReq, SysConfigPageVO.class);
    return getDataTable(page);
  }

  @Log(title = "参数管理", businessType = BusinessType.EXPORT)
  @SaCheckPermission("system:config:export")
  @Mapping(value = "/export", method = MethodType.POST)
  public void export(Context context, SysConfig config) {
    List<SysConfigDetailVO> list = configService.selectConfigList(config,SysConfigDetailVO.class);
    ExcelUtil<SysConfigDetailVO> util = new ExcelUtil<>(SysConfigDetailVO.class);
    util.exportExcel(context, list, "参数数据");
  }

  /**
   * 根据参数编号获取详细信息
   */
  @SaCheckPermission("system:config:query")
  @Mapping(value = "/{configId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Integer configId) {
    ActionContext context = ActionContext.current();
    SysConfig config = configService.findOneByConfigId(context, configId, SysConfig.class);
    return success(config);
  }

  /**
   * 根据参数键名查询参数值
   */
  @Mapping(value = "/configKey/{configKey}", method = MethodType.GET)
  public AjaxResult getConfigKey(@Path String configKey) {
    return success(configService.selectConfigByKey(configKey));
  }

  /**
   * 新增参数配置
   */
  @SaCheckPermission("system:config:add")
  @Log(title = "参数管理", businessType = BusinessType.INSERT)
  @Mapping(method = MethodType.POST)
  public AjaxResult add(@Validated @Body SysConfig config) {
    if (!configService.checkConfigKeyUnique(config)) {
      return error("新增参数'" + config.getConfigName() + "'失败，参数键名已存在");
    }
    config.setCreateBy(getUsername());
    return toAjax(configService.insertConfig(config));
  }

  /**
   * 修改参数配置
   */
  @SaCheckPermission("system:config:edit")
  @Log(title = "参数管理", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult edit(@Validated @Body SysConfig config) {
    if (!configService.checkConfigKeyUnique(config)) {
      return error("修改参数'" + config.getConfigName() + "'失败，参数键名已存在");
    }
    config.setUpdateBy(getUsername());
    return toAjax(configService.updateConfig(config));
  }

  /**
   * 删除参数配置
   */
  @SaCheckPermission("system:config:remove")
  @Log(title = "参数管理", businessType = BusinessType.DELETE)
  @Mapping(value = "/{configIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path List<Long> configIds) {
    configService.deleteConfigByIds(configIds);
    return success();
  }

  /**
   * 刷新参数缓存
   */
  @SaCheckPermission("system:config:remove")
  @Log(title = "参数管理", businessType = BusinessType.CLEAN)
  @Mapping(value = "/refreshCache", method = MethodType.DELETE)
  public AjaxResult refreshCache() {
    configService.resetConfigCache();
    return success();
  }

}
