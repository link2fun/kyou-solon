package com.github.link2fun.generator.service;


import com.github.link2fun.generator.domain.GenTable;
import com.github.link2fun.generator.domain.req.GenTableModifyReq;
import com.github.link2fun.generator.domain.req.GenTableSearchReq;
import com.github.link2fun.support.core.page.Page;

import java.util.List;
import java.util.Map;

/**
 * 业务 服务层
 *
 * @author ruoyi
 */
public interface IGenTableService {
  /**
   * 查询业务列表
   *
   * @param pageRequest 分页对象
   * @param searchReq   查询条件
   * @return 业务集合
   */
  Page<GenTable> selectGenTableList(final Page<GenTable> pageRequest, GenTableSearchReq searchReq);

  /**
   * 查询据库列表
   *
   * <p>结果取自数据库元数据而非业务表, 不分页: 返回全部匹配表, total 记为条数。
   *
   * @param searchReq 查询条件
   * @return 数据库表集合
   */
  Page<GenTable> selectDbTableList(GenTableSearchReq searchReq);

  /**
   * 查询据库列表
   *
   * @param tableNames 表名称组
   * @return 数据库表集合
   */
  List<GenTable> selectDbTableListByNames(String[] tableNames);

  /**
   * 查询所有表信息
   *
   * @return 表信息集合
   */
  List<GenTable> selectGenTableAll();

  /**
   * 查询业务信息
   *
   * @param id 业务ID
   * @return 业务信息
   */
  GenTable selectGenTableById(Long id);

  /**
   * 修改业务
   *
   * @param modifyReq 修改业务请求
   */
  void updateGenTable(GenTableModifyReq modifyReq);

  /**
   * 删除业务信息
   *
   * @param tableIds 需要删除的表数据ID
   */
  void deleteGenTableByIds(Long[] tableIds);

  /**
   * 导入表结构
   *
   * @param tableList 导入表列表
   */
  void importGenTable(List<GenTable> tableList);

  /**
   * 按表名导入表结构: 读取库表元数据并写入生成配置, 两步在同一事务内
   *
   * @param tableNames 要导入的表名
   */
  void importGenTableByNames(String[] tableNames);

  /**
   * 预览代码
   *
   * @param tableId 表编号
   * @return 预览数据列表
   */
  Map<String, String> previewCode(Long tableId);

  /**
   * 生成代码（下载方式）
   *
   * @param tableName 表名称
   * @return 数据
   */
  byte[] downloadCode(String tableName);

  /**
   * 生成代码（自定义路径）
   *
   * @param tableName 表名称
   */
  void generatorCode(String tableName);

  /**
   * 同步数据库
   *
   * @param tableName 表名称
   */
  void syncDb(String tableName);

  /**
   * 批量生成代码（下载方式）
   *
   * @param tableNames 表数组
   * @return 数据
   */
  byte[] downloadCode(String[] tableNames);

  GenTable selectGenTableByName(String tableName);
}
