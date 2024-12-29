package com.github.link2fun.generator.service;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.easy.query.api.proxy.client.EasyEntityQuery;
import com.easy.query.core.api.pagination.EasyPageResult;
import com.easy.query.core.enums.SQLExecuteStrategyEnum;
import com.easy.query.solon.annotation.Db;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.link2fun.generator.config.GenConfig;
import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.GenTableColumn;
import com.github.link2fun.generator.domain.pdmaner.PDManer;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.proxy.GenTableProxy;
import com.github.link2fun.generator.util.*;
import com.github.link2fun.support.constant.Constants;
import com.github.link2fun.support.constant.GenConstants;
import com.github.link2fun.support.core.page.Page;
import com.github.link2fun.support.core.text.CharsetKit;
import com.github.link2fun.support.exception.ServiceException;
import com.github.link2fun.support.utils.SecurityUtils;
import com.github.link2fun.support.utils.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.anyline.metadata.Table;
import org.anyline.proxy.ServiceProxy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.noear.solon.core.BeanWrap;
import org.noear.solon.data.annotation.Tran;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 业务 服务层实现
 *
 * @author ruoyi
 */
@Slf4j
@Component
public class GenTableServiceImpl implements IGenTableService {

  @Inject
  private ObjectMapper objectMapper;
  @Inject
  private IGenTableService self;

  @Inject
  private IGenTableColumnService genTableColumnService;

  @Db
  private EasyEntityQuery entityQuery;


  /**
   * 查询业务信息
   *
   * @param id 业务ID
   * @return 业务信息
   */
  @Override
  public GenTable selectGenTableById(Long id) {
    GenTable genTable = entityQuery.queryable(GenTable.class).whereById(id).singleNotNull();
    setTableFromOptions(genTable);
    return genTable;
  }

  /**
   * 查询业务列表
   *
   * @param page      分页对象
   * @param searchReq 查询条件
   * @return 业务集合
   */
  @Override
  public Page<GenTable> selectGenTableList(final Page<GenTable> page, GenTable searchReq) {


    EasyPageResult<GenTable> pageResult = entityQuery.queryable(GenTable.class)
      .where(proxy -> {
        proxy.tableName().like(StrUtil.isNotBlank(searchReq.getTableName()), searchReq.getTableName());
        proxy.tableComment().like(StrUtil.isNotBlank(searchReq.getTableComment()), searchReq.getTableComment());
        proxy.createTime().ge(Objects.nonNull(searchReq.getParams().getBeginTime()), searchReq.getParams().getBeginTime());
        proxy.createTime().le(Objects.nonNull(searchReq.getParams().getEndTime()), searchReq.getParams().getEndTime());
      }).toPageResult(page.getPageNum(), page.getPageSize(), page.getTotal());
    return Page.of(pageResult);
  }

  /**
   * 查询据库列表
   *
   * @param searchReq 查询条件
   * @return 数据库表集合
   */
  @Override
  public Page<GenTable> selectDbTableList(final Page<GenTable> page, GenTable searchReq) {

    String anylineServiceName = Solon.context().getWrapsOfType(DataSource.class).stream().filter(BeanWrap::typed)
      .findFirst().map(BeanWrap::name).orElse("default");

    LinkedHashMap<String, Table<?>> tables = ServiceProxy.service(anylineServiceName).metadata().tables();
    List<GenTable> dataList = tables.values().stream()
      .filter(info -> !StrUtil.containsAny(info.getName(), "qrtz_", "gen_"))
      .map(t -> {
        GenTable genT = new GenTable();
        genT.setTableName(t.getName());
        genT.setTableComment(t.getComment());
        genT.setCreateTime(LocalDateTimeUtil.of(t.getCreateTime()));
        genT.setUpdateTime(LocalDateTimeUtil.of(t.getUpdateTime()));
        return genT;

      })
      // 根据查询条件进行过滤
      .filter(t -> StrUtil.isBlank(searchReq.getTableName()) || StrUtil.contains(t.getTableName(), searchReq.getTableName()))
      .filter(t -> StrUtil.isBlank(searchReq.getTableComment()) || StrUtil.contains(t.getTableComment(), searchReq.getTableComment()))
      .toList();


    // 构建返回结果
    Page<GenTable> _page = new Page<>();
    _page.setTotal(dataList.size());
    _page.setRecords(dataList);
    _page.setCurrent(_page.getCurrent());
    _page.setSize(_page.getSize());
    return _page;


  }

  /**
   * 查询据库列表
   *
   * @param tableNames 表名称组
   * @return 数据库表集合
   */
  @Override
  public List<GenTable> selectDbTableListByNames(String[] tableNames) {

    String syncSource = GenUtils.getGenConfig().getSyncSource();
    if (StrUtil.equalsAnyIgnoreCase(syncSource, "db")) {
      String anylineServiceName = Solon.context().getWrapsOfType(DataSource.class).stream().filter(BeanWrap::typed)
        .findFirst().map(BeanWrap::name).orElse("default");

      LinkedHashMap<String, Table<?>> tables = ServiceProxy.service(anylineServiceName).metadata().tables();
      // 根据查询条件进行过滤

      return tables.values().stream()
        .filter(info -> StrUtil.containsAny(info.getName(), tableNames))
        .map(t -> {
          GenTable genT = new GenTable();
          genT.setTableName(t.getName());
          genT.setTableComment(t.getComment());
          genT.setCreateTime(LocalDateTimeUtil.of(t.getCreateTime()));
          genT.setUpdateTime(LocalDateTimeUtil.of(t.getUpdateTime()));

          return genT;

        })
        // 根据查询条件进行过滤
        .toList();
    }

    // 使用的不是 db, 那应该是 pdmaner
    PDManer pdManer = PDManerTool.loadPDManer(GenUtils.getGenConfig().getPdmanerJsonPath());

    return pdManer.getEntities().stream()
      .filter(entity -> ArrayUtil.contains(tableNames, entity.getDefKey()))
      .map(entity -> {
        GenTable genT = new GenTable();
        genT.setTableName(entity.getDefKey());
        genT.setTableComment(entity.getDefName());
        genT.setCreateTime(LocalDateTimeUtil.of(new Date()));
        genT.setUpdateTime(LocalDateTimeUtil.of(new Date()));

        return genT;
      })
      .collect(Collectors.toList());


  }

  /**
   * 查询所有表信息
   *
   * @return 表信息集合
   */
  @Override
  public List<GenTable> selectGenTableAll() {
    return entityQuery.queryable(GenTable.class)
      .includes(GenTableProxy::columns)
      .toList();
  }

  /**
   * 修改业务
   *
   * @param genTable 业务信息
   */
  @Override
  @Tran
  public void updateGenTable(GenTable genTable) {
    String options = JSONUtil.toJsonStr(genTable.getParams());
    genTable.setOptions(options);
    long row = entityQuery.updatable(genTable)
      .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
      .executeRows();
    if (row > 0) {
      entityQuery.updatable(genTable.getColumns())
        .batch()
        .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
        .executeRows();
    }
  }

  /**
   * 删除业务对象
   *
   * @param tableIds 需要删除的数据ID
   */
  @Override
  @Tran
  public void deleteGenTableByIds(Long[] tableIds) {
    entityQuery.deletable(GenTable.class)
      .allowDeleteStatement(true)
      .where(table -> table.tableId().in(tableIds))
      .executeRows();
    entityQuery.deletable(GenTableColumn.class)
      .allowDeleteStatement(true)
      .where(tableColumn -> tableColumn.tableId().in(tableIds))
      .executeRows();
  }

  /**
   * 导入表结构
   *
   * @param tableList 导入表列表
   */
  @Override
  @Tran
  public void importGenTable(List<GenTable> tableList) {
    String operatorName = SecurityUtils.getUsername();
    try {
      for (GenTable table : tableList) {
        String tableName = table.getTableName();
        GenUtils.initTable(table, operatorName);
        long row = entityQuery.insertable(table)
          .setSQLStrategy(SQLExecuteStrategyEnum.ONLY_NOT_NULL_COLUMNS)
          .executeRows(true);
        if (row > 0) {
          // 保存列信息
          List<GenTableColumn> genTableColumns;
          if (GenUtils.getGenConfig().syncFromDb()) {
            genTableColumns = genTableColumnService.selectDbTableColumnsByName(tableName);
          } else {
            genTableColumns = PDManerTool.extractTableColumns(PDManerTool.loadPDManer(GenUtils.getGenConfig().getPdmanerJsonPath()), tableName);
          }
          for (GenTableColumn column : genTableColumns) {
            GenUtils.initColumnField(column, table);
            column.setTableId(table.getTableId());
          }
          genTableColumnService.insertGenTableColumn(genTableColumns);
        }
      }
    } catch (Exception e) {
      throw new ServiceException("导入失败：" + e.getMessage());
    }
  }

  /**
   * 预览代码
   *
   * @param tableId 表编号
   * @return 预览数据列表
   */
  @Override
  public Map<String, String> previewCode(Long tableId) {
    Map<String, String> dataMap = new LinkedHashMap<>();
    // 查询表信息
    GenTable table = entityQuery.queryable(GenTable.class)
      .whereById(tableId)
      .selectAutoInclude(GenTable.class)
      .singleNotNull();
    // 设置主子表信息
    setSubTable(table);
    // 设置主键列信息
    setPkColumn(table);
    VelocityInitializer.initVelocity();

    VelocityContext context = VelocityUtils.prepareContext(table);

    // 获取模板列表
    List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
    for (String template : templates) {
      // 渲染模板
      StringWriter sw = new StringWriter();
      Template tpl = Velocity.getTemplate(template, Constants.UTF8);
      tpl.merge(context, sw);
      dataMap.put(template, sw.toString());
    }

    PDManer pdManer = PDManerTool.loadPDManer(GenUtils.getGenConfig().getPdmanerJsonPath());
    GenConfig genConfig = GenUtils.getGenConfig();


    TableInfo tableInfo = new TableInfo(pdManer, table);

    tableInfo.setAuthor(genConfig.getAuthor());

    CodeGenerator.getHandlers()
      .forEach(handler-> dataMap.put(handler.getTemplateName(), handler.getTemplateResult(tableInfo)));

    return dataMap;
  }

  /**
   * 生成代码（下载方式）
   *
   * @param tableName 表名称
   * @return 数据
   */
  @Override
  public byte[] downloadCode(String tableName) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(outputStream);
    generatorCode(tableName, zip);
    IOUtils.closeQuietly(zip);
    return outputStream.toByteArray();
  }

  /**
   * 生成代码（自定义路径）
   *
   * @param tableName 表名称
   */
  @Override
  public void generatorCode(String tableName) {
    // 查询表信息
    GenTable table = entityQuery.queryable(GenTable.class)
      .where(proxy -> proxy.tableName().eq(tableName))
      .selectAutoInclude(GenTable.class)
      .singleNotNull();
    // 设置主子表信息
    setSubTable(table);
    // 设置主键列信息
    setPkColumn(table);

    VelocityInitializer.initVelocity();

    VelocityContext context = VelocityUtils.prepareContext(table);

    // 获取模板列表
    List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
    for (String template : templates) {
      if (!StringUtils.containsAny(template, "sql.vm", "api.js.vm", "index.vue.vm", "index-tree.vue.vm")) {
        // 渲染模板
        StringWriter sw = new StringWriter();
        Template tpl = Velocity.getTemplate(template, Constants.UTF8);
        tpl.merge(context, sw);
        try {
          String path = getGenPath(table, template);
          FileUtils.writeStringToFile(new File(path), sw.toString(), CharsetKit.UTF_8);
        } catch (IOException e) {
          throw new ServiceException("渲染模板失败，表名：" + table.getTableName());
        }
      }
    }
  }

  /**
   * 同步数据库
   *
   * @param tableName 表名称
   */
  @Override
  @Tran
  public void syncDb(String tableName) {
    GenTable table = entityQuery.queryable(GenTable.class)
      .where(genTable -> genTable.tableName().eq(tableName))
      .includes(GenTableProxy::columns)
      .singleNotNull();
    List<GenTableColumn> tableColumns = table.getColumns();
    Map<String, GenTableColumn> tableColumnMap = tableColumns.stream().collect(Collectors.toMap(GenTableColumn::getColumnName, Function.identity()));

    List<GenTableColumn> dbTableColumns;
    if (GenUtils.getGenConfig().syncFromDb()) {
      // 从DB进行同步
      dbTableColumns = genTableColumnService.selectDbTableColumnsByName(tableName);
    } else {
      // 从PDManer进行同步
      PDManer pdManer = PDManerTool.loadPDManer(GenUtils.getGenConfig().getPdmanerJsonPath());
      // todo 从pdmaner中获取表结构
      dbTableColumns = PDManerTool.extractTableColumns(pdManer, tableName);
    }

    if (StringUtils.isEmpty(dbTableColumns)) {
      throw new ServiceException("同步数据失败，原表结构不存在");
    }
    List<String> dbTableColumnNames = dbTableColumns.stream().map(GenTableColumn::getColumnName).toList();

    List<GenTableColumn> updateList = Lists.newArrayList();
    List<GenTableColumn> insertList = Lists.newArrayList();

    dbTableColumns.forEach(column -> {
      GenUtils.initColumnField(column, table);
      if (tableColumnMap.containsKey(column.getColumnName())) {
        GenTableColumn prevColumn = tableColumnMap.get(column.getColumnName());
        column.setColumnId(prevColumn.getColumnId());
        if (column.isList()) {
          // 如果是列表，继续保留查询方式/字典类型选项
          column.setDictType(prevColumn.getDictType());
          column.setQueryType(prevColumn.getQueryType());
        }
        if (StringUtils.isNotEmpty(prevColumn.getIsRequired()) && !column.isPk()
          && (column.isInsert() || column.isEdit())
          && ((column.isUsableColumn()) || (!column.isSuperColumn()))) {
          // 如果是(新增/修改&非主键/非忽略及父属性)，继续保留必填/显示类型选项
          column.setIsRequired(prevColumn.getIsRequired());
          column.setHtmlType(prevColumn.getHtmlType());
        }
        updateList.add(column);
      } else {
        column.setTableId(table.getTableId());
        insertList.add(column);
      }
    });

    if (CollectionUtil.isNotEmpty(insertList)) {
      genTableColumnService.insertGenTableColumn(insertList);
    }
    if (CollectionUtil.isNotEmpty(updateList)) {
      genTableColumnService.updateGenTableColumn(updateList);
    }

    List<GenTableColumn> delColumns = tableColumns.stream().filter(column -> !dbTableColumnNames.contains(column.getColumnName())).collect(Collectors.toList());
    if (StringUtils.isNotEmpty(delColumns)) {
      genTableColumnService.deleteGenTableColumns(delColumns);
    }
  }

  /**
   * 批量生成代码（下载方式）
   *
   * @param tableNames 表数组
   * @return 数据
   */
  @Override
  public byte[] downloadCode(String[] tableNames) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ZipOutputStream zip = new ZipOutputStream(outputStream);
    for (String tableName : tableNames) {
      generatorCode(tableName, zip);
    }
    IOUtils.closeQuietly(zip);
    return outputStream.toByteArray();
  }

  /**
   * 查询表信息并生成代码
   */
  private void generatorCode(String tableName, ZipOutputStream zip) {
    // 查询表信息

    GenTable table = entityQuery.queryable(GenTable.class)
      .where(proxy -> proxy.tableName().eq(tableName))
      .includes(GenTableProxy::columns)
      .singleNotNull();

    // 设置主子表信息
    setSubTable(table);
    // 设置主键列信息
    setPkColumn(table);

    VelocityInitializer.initVelocity();

    VelocityContext context = VelocityUtils.prepareContext(table);

    // 获取模板列表
    List<String> templates = VelocityUtils.getTemplateList(table.getTplCategory(), table.getTplWebType());
    for (String template : templates) {
      // 渲染模板
      StringWriter sw = new StringWriter();
      Template tpl = Velocity.getTemplate(template, Constants.UTF8);
      tpl.merge(context, sw);
      try {
        // 添加到zip
        zip.putNextEntry(new ZipEntry(VelocityUtils.getFileName(template, table)));
        IOUtils.write(sw.toString(), zip, Constants.UTF8);
        IOUtils.closeQuietly(sw);
        zip.flush();
        zip.closeEntry();
      } catch (IOException e) {
        log.error("渲染模板失败，表名：{}", table.getTableName(), e);
      }
    }
  }

  /**
   * 修改保存参数校验
   *
   * @param genTable 业务信息
   */
  @Override
  public void validateEdit(GenTable genTable) throws JsonProcessingException {
    if (GenConstants.TPL_TREE.equals(genTable.getTplCategory())) {
      String options = objectMapper.writeValueAsString(genTable.getParams());
      JSONObject paramsObj = JSONUtil.parseObj(options);
      if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_CODE))) {
        throw new ServiceException("树编码字段不能为空");
      } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_PARENT_CODE))) {
        throw new ServiceException("树父编码字段不能为空");
      } else if (StringUtils.isEmpty(paramsObj.getStr(GenConstants.TREE_NAME))) {
        throw new ServiceException("树名称字段不能为空");
      } else if (GenConstants.TPL_SUB.equals(genTable.getTplCategory())) {
        if (StringUtils.isEmpty(genTable.getSubTableName())) {
          throw new ServiceException("关联子表的表名不能为空");
        } else if (StringUtils.isEmpty(genTable.getSubTableFkName())) {
          throw new ServiceException("子表关联的外键名不能为空");
        }
      }
    }
  }

  @Override
  public GenTable selectGenTableByName(String tableName) {
    return entityQuery.queryable(GenTable.class)
      .where(proxy -> proxy.tableName().eq(tableName))
      .includes(GenTableProxy::columns)
      .singleNotNull();
  }

  /**
   * 设置主键列信息
   *
   * @param table 业务表信息
   */
  public void setPkColumn(GenTable table) {
    for (GenTableColumn column : table.getColumns()) {
      if (column.isPk()) {
        table.setPkColumn(column);
        break;
      }
    }
    if (StringUtils.isNull(table.getPkColumn())) {
      table.setPkColumn(table.getColumns().get(0));
    }
    if (GenConstants.TPL_SUB.equals(table.getTplCategory())) {
      for (GenTableColumn column : table.getSubTable().getColumns()) {
        if (column.isPk()) {
          table.getSubTable().setPkColumn(column);
          break;
        }
      }
      if (StringUtils.isNull(table.getSubTable().getPkColumn())) {
        table.getSubTable().setPkColumn(table.getSubTable().getColumns().get(0));
      }
    }
  }

  /**
   * 设置主子表信息
   *
   * @param table 业务表信息
   */
  public void setSubTable(GenTable table) {
    String subTableName = table.getSubTableName();
    if (StringUtils.isNotEmpty(subTableName)) {
      table.setSubTable(self.selectGenTableByName(subTableName));
    }
  }

  /**
   * 设置代码生成其他选项值
   *
   * @param genTable 设置后的生成对象
   */
  public void setTableFromOptions(GenTable genTable) {
    JSONObject paramsObj = JSONUtil.parseObj(genTable.getOptions());
    String treeCode = paramsObj.getStr(GenConstants.TREE_CODE);
    String treeParentCode = paramsObj.getStr(GenConstants.TREE_PARENT_CODE);
    String treeName = paramsObj.getStr(GenConstants.TREE_NAME);
    String parentMenuId = paramsObj.getStr(GenConstants.PARENT_MENU_ID);
    String parentMenuName = paramsObj.getStr(GenConstants.PARENT_MENU_NAME);

    genTable.setTreeCode(treeCode);
    genTable.setTreeParentCode(treeParentCode);
    genTable.setTreeName(treeName);
    genTable.setParentMenuId(parentMenuId);
    genTable.setParentMenuName(parentMenuName);
  }

  /**
   * 获取代码生成地址
   *
   * @param table    业务表信息
   * @param template 模板文件路径
   * @return 生成地址
   */
  public static String getGenPath(GenTable table, String template) {
    String genPath = table.getGenPath();
    if (StringUtils.equals(genPath, "/")) {
      return System.getProperty("user.dir") + File.separator + "src" + File.separator + VelocityUtils.getFileName(template, table);
    }
    return genPath + File.separator + VelocityUtils.getFileName(template, table);
  }
}