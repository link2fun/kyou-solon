package com.github.link2fun.generator.domain.req;

import com.easy.query.core.annotation.Navigate;
import com.easy.query.core.enums.RelationTypeEnum;
import com.github.link2fun.support.core.domain.BaseEntityParams;
import lombok.Data;
import org.noear.solon.validation.annotation.NotNull;

import java.util.List;

/**
 * 修改代码生成配置请求。
 * {@link com.github.link2fun.generator.domain.GenTable}
 */
@Data
public class GenTableModifyReq {

  /** 表ID */
  @NotNull(message = "表ID不能为空")
  private Long tableId;

  /** 表描述 */
  private String tableComment;

  /** 关联子表的表名 */
  private String subTableName;

  /** 子表关联的外键名 */
  private String subTableFkName;

  /** 实体类名称 */
  private String className;

  /** 使用的模板（crud单表操作 tree树表操作 sub主子表操作） */
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

  /** 生成作者 */
  private String functionAuthor;

  /** 生成代码方式（0zip压缩包 1自定义路径） */
  private String genType;

  /** 生成路径（不填默认项目路径） */
  private String genPath;

  /** 备注 */
  private String remark;

  /** 其它生成选项（树编码字段等） */
  private BaseEntityParams params;

  /** 字段配置 */
  @Navigate(value = RelationTypeEnum.OneToMany)
  private List<GenTableColumnReq> columns;

}
