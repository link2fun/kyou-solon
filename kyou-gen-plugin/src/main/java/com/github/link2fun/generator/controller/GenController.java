package com.github.link2fun.generator.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.service.IGenTableColumnService;
import com.github.link2fun.generator.service.IGenTableService;
import com.github.link2fun.support.annotation.Log;
import com.github.link2fun.support.core.controller.BaseController;
import com.github.link2fun.support.core.domain.AjaxResult;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.page.TableDataInfo;
import com.github.link2fun.support.core.text.Convert;
import com.github.link2fun.support.enums.BusinessType;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.core.handle.MethodType;
import org.noear.solon.validation.annotation.Validated;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 代码生成 操作处理
 *
 * @author link2fun
 */
@Controller
@Mapping("/tool/gen")
public class GenController extends BaseController {

  @Inject
  private IGenTableService genTableService;

  @Inject
  private IGenTableColumnService genTableColumnService;

  /**
   * 查询代码生成列表
   */
  @SaCheckPermission("tool:gen:list")
  @Mapping(value = "/list", method = MethodType.GET)
  public TableDataInfo genList(GenTable searchReq) {
    Page<GenTable> list = genTableService.selectGenTableList(Page.ofCurrentContext(), searchReq);
    return getDataTable(list);
  }

  /**
   * 修改代码生成业务
   */
  @SaCheckPermission("tool:gen:query")
  @Mapping(value = "/{tableId}", method = MethodType.GET)
  public AjaxResult getInfo(@Path Long tableId) {
    GenTable table = genTableService.selectGenTableById(tableId);
    List<GenTable> tables = genTableService.selectGenTableAll();
    List<GenTableColumn> tableColumnList = genTableColumnService.selectGenTableColumnListByTableId(tableId);
    Map<String, Object> map = new HashMap<>();
    map.put("info", table);
    map.put("rows", tableColumnList);
    map.put("tables", tables);
    return success(map);
  }

  /**
   * 查询数据库列表
   */
  @SaCheckPermission("tool:gen:list")
  @Mapping(value = "/db/list", method = MethodType.GET)
  public TableDataInfo dataList(GenTable genTable) {
    Page<GenTable> list = genTableService.selectDbTableList(Page.ofCurrentContext(), genTable);
    return getDataTable(list);
  }

  /**
   * 查询数据表字段列表
   */
  @SaCheckPermission("tool:gen:list")
  @Mapping(value = "/column/{tableId}", method = MethodType.GET)
  public TableDataInfo columnList(@Path Long tableId) {
    TableDataInfo dataInfo = new TableDataInfo();
    List<GenTableColumn> list = genTableColumnService.selectGenTableColumnListByTableId(tableId);
    dataInfo.setRows(list);
    dataInfo.setTotal(list.size());
    return dataInfo;
  }

  /**
   * 导入表结构（保存）
   */
  @SaCheckPermission("tool:gen:import")
  @Log(title = "代码生成", businessType = BusinessType.IMPORT)
  @Mapping(value = "/importTable", method = MethodType.POST)
  public AjaxResult importTableSave(String tables) {
    String[] tableNames = Convert.toStrArray(tables);
    // 查询表信息
    List<GenTable> tableList = genTableService.selectDbTableListByNames(tableNames);
    genTableService.importGenTable(tableList);
    return success();
  }

  /**
   * 修改保存代码生成业务
   */
  @SaCheckPermission("tool:gen:edit")
  @Log(title = "代码生成", businessType = BusinessType.UPDATE)
  @Mapping(method = MethodType.PUT)
  public AjaxResult editSave(@Validated @Body GenTable genTable) throws JsonProcessingException {
    genTableService.validateEdit(genTable);
    genTableService.updateGenTable(genTable);
    return success();
  }

  /**
   * 删除代码生成
   */
  @SaCheckPermission("tool:gen:remove")
  @Log(title = "代码生成", businessType = BusinessType.DELETE)
  @Mapping(value = "/{tableIds}", method = MethodType.DELETE)
  public AjaxResult remove(@Path Long[] tableIds) {
    genTableService.deleteGenTableByIds(tableIds);
    return success();
  }

  /**
   * 预览代码
   */
  @SaCheckPermission("tool:gen:preview")
  @Mapping(value = "/preview/{tableId}", method = MethodType.GET)
  public AjaxResult preview(@Path("tableId") Long tableId) {
    Map<String, String> dataMap = genTableService.previewCode(tableId);
    return success(dataMap);
  }

  /**
   * 生成代码（下载方式）
   */
  @SaCheckPermission("tool:gen:code")
  @Log(title = "代码生成", businessType = BusinessType.GENCODE)
  @Mapping(value = "/download/{tableName}", method = MethodType.GET)
  public void download(Context response, @Path("tableName") String tableName) throws IOException {
    byte[] data = genTableService.downloadCode(tableName);
    genCode(response, data);
  }

  /**
   * 生成代码（自定义路径）
   */
  @SaCheckPermission("tool:gen:code")
  @Log(title = "代码生成", businessType = BusinessType.GENCODE)
  @Mapping(value = "/genCode/{tableName}", method = MethodType.GET)
  public AjaxResult genCode(@Path("tableName") String tableName) {
    genTableService.generatorCode(tableName);
    return success();
  }

  /**
   * 同步数据库
   */
  @SaCheckPermission("tool:gen:edit")
  @Log(title = "代码生成", businessType = BusinessType.UPDATE)
  @Mapping(value = "/synchDb/{tableName}", method = MethodType.GET)
  public AjaxResult syncDb(@Path("tableName") String tableName) {
    genTableService.syncDb(tableName);
    return success();
  }

  /**
   * 批量生成代码
   */
  @SaCheckPermission("tool:gen:code")
  @Log(title = "代码生成", businessType = BusinessType.GENCODE)
  @Mapping(value = "/batchGenCode", method = MethodType.GET)
  public void batchGenCode(Context response, String tables) throws IOException {
    String[] tableNames = Convert.toStrArray(tables);
    byte[] data = genTableService.downloadCode(tableNames);
    genCode(response, data);
  }

  /**
   * 生成zip文件
   */
  private void genCode(Context context, byte[] data) throws IOException {
    context.headerSet("Access-Control-Allow-Origin", "*");
    context.headerSet("Access-Control-Expose-Headers", "Content-Disposition");
    context.headerSet("Content-Disposition", "attachment; filename=\"kyou.zip\"");
    context.headerSet("Content-Length", "" + data.length);
    context.contentType("application/octet-stream; charset=UTF-8");
    context.output(data);

  }
}