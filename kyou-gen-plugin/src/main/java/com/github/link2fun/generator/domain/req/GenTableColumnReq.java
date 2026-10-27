package com.github.link2fun.generator.domain.req;

import lombok.Data;

/**
 * {@link com.github.link2fun.generator.domain.GenTableColumn}
 * 代码生成列配置请求; 只含编辑页真正会改的列, 表结构与字段类型等来自库表元数据
 */
@Data
public class GenTableColumnReq {

  /** 列ID */
  private Long columnId;

  /** 归属表ID */
  private Long tableId;

  /** 列名 */
  private String columnName;

  /** 列描述 */
  private String columnComment;

  /** 列类型 */
  private String columnType;

  /** Java类型 */
  private String javaType;

  /** Java属性名 */
  private String javaField;

  /** 是否主键（1是） */
  private String isPk;

  /** 是否自增（1是） */
  private String isIncrement;

  /** 是否必填（1是） */
  private String isRequired;

  /** 是否为插入字段（1是） */
  private String isInsert;

  /** 是否编辑字段（1是） */
  private String isEdit;

  /** 是否列表字段（1是） */
  private String isList;

  /** 是否查询字段（1是） */
  private String isQuery;

  /** 查询方式 */
  private String queryType;

  /** 显示类型 */
  private String htmlType;

  /** 字典类型 */
  private String dictType;

  /** 排序 */
  private Integer sort;

}
