package com.github.link2fun.generator.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 代码生成业务表响应
 * {@link com.github.link2fun.generator.domain.GenTable }
 */
@Data
public class GenTableDTO implements Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 编号 */
  private Long tableId;
  /** 表名称 */
  private String tableName;
  /** 表描述 */
  private String tableComment;
  /** 关联子表的表名 */
  private String subTableName;
  /** 子表关联的外键名 */
  private String subTableFkName;
  /** 实体类名称 */
  private String className;
  /** 使用的模板 */
  private String tplCategory;
  /** 前端模板类型 */
  private String tplWebType;
  /** 生成包路径 */
  private String packageName;
  /** 生成模块名 */
  private String moduleName;
  /** 生成业务名 */
  private String businessName;
  /** 生成功能名 */
  private String functionName;
  /** 生成功能作者 */
  private String functionAuthor;
  /** 生成代码方式 */
  private String genType;
  /** 生成路径 */
  private String genPath;
  /** 其它生成选项 */
  private String options;
  /** 备注 */
  private String remark;
  /** 创建者 */
  private String createBy;
  /** 创建时间 */
  private LocalDateTime createTime;
  /** 更新者 */
  private String updateBy;
  /** 更新时间 */
  private LocalDateTime updateTime;
}
