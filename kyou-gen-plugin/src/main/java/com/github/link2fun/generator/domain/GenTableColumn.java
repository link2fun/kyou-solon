package com.github.link2fun.generator.domain;


import com.easy.query.core.annotation.Column;
import com.easy.query.core.annotation.EasyAlias;
import com.easy.query.core.annotation.EntityProxy;
import com.easy.query.core.annotation.Table;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.link2fun.generator.domain.proxy.GenTableColumnProxy;
import com.github.link2fun.generator.util.GenUtils;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.utils.StringUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.NotBlank;

import java.io.Serial;

/**
 * 代码生成业务字段表 gen_table_column
 *
 * @author ruoyi
 */
@FieldNameConstants
@EqualsAndHashCode(callSuper = true)
@Data
@EntityProxy
@Table("gen_table_column")
@EasyAlias("tableColumn")
public class GenTableColumn extends BaseEntity implements ProxyEntityAvailable<GenTableColumn, GenTableColumnProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 编号 */
  @Column(value = "column_id", primaryKey = true, generatedKey = true)
  private Long columnId;

  /** 归属表编号 */
  @Column("table_id")
  private Long tableId;

  /** 列名称 */
  @Column("column_name")
  private String columnName;

  /** 列描述 */
  @Column("column_comment")
  private String columnComment;

  /** 列类型 */
  @Column("column_type")
  private String columnType;

  /** JAVA类型 */
  @Column("java_type")
  private String javaType;

  /** JAVA字段名 */
  @NotBlank(message = "Java属性不能为空")
  @Column("java_field")
  private String javaField;

  /** 是否主键（1是） */
  @Column("is_pk")
  private String isPk;

  /** 是否自增（1是） */
  @Column("is_increment")
  private String isIncrement;

  /** 是否必填（1是） */
  @Column("is_required")
  private String isRequired;

  /** 是否为插入字段（1是） */
  @Column("is_insert")
  private String isInsert;

  /** 是否编辑字段（1是） */
  @Column("is_edit")
  private String isEdit;

  /** 是否列表字段（1是） */
  @Column("is_list")
  private String isList;

  /** 是否查询字段（1是） */
  @Column("is_query")
  private String isQuery;

  /** 查询方式（EQ等于、NE不等于、GT大于、LT小于、LIKE模糊、BETWEEN范围） */
  @Column("query_type")
  private String queryType;

  /** 显示类型（input文本框、textarea文本域、select下拉框、checkbox复选框、radio单选框、datetime日期控件、image图片上传控件、upload文件上传控件、editor富文本控件） */
  @Column("html_type")
  private String htmlType;

  public String getHtmlAntDesignProComponent() {
    return GenUtils.getAntDesignProComponent(htmlType);
  }

  /** 字典类型 */
  @Column("dict_type")
  private String dictType;

  /** 排序 */
  @Column("sort")
  private Integer sort;

  public String getCapJavaField() {
    return StringUtils.capitalize(javaField);
  }

  @JsonIgnore
  public boolean isPk() {
    return isPk(this.isPk);
  }

  @JsonIgnore
  public boolean isPk(String isPk) {
    return isPk != null && StringUtils.equals("1", isPk);
  }

  @JsonIgnore
  public boolean isIncrement() {
    return isIncrement(this.isIncrement);
  }

  @JsonIgnore
  public boolean isIncrement(String isIncrement) {
    return isIncrement != null && StringUtils.equals("1", isIncrement);
  }

  @JsonIgnore
  public boolean isRequired() {
    return isRequired(this.isRequired);
  }

  @JsonIgnore
  public boolean isRequired(String isRequired) {
    return isRequired != null && StringUtils.equals("1", isRequired);
  }

  @JsonIgnore
  public boolean isInsert() {
    return isInsert(this.isInsert);
  }

  @JsonIgnore
  public boolean isInsert(String isInsert) {
    return isInsert != null && StringUtils.equals("1", isInsert);
  }

  @JsonIgnore
  public boolean isEdit() {
    return isInsert(this.isEdit);
  }

  @JsonIgnore
  public boolean isEdit(String isEdit) {
    return isEdit != null && StringUtils.equals("1", isEdit);
  }

  @JsonIgnore
  public boolean isList() {
    return isList(this.isList);
  }

  @JsonIgnore
  public boolean isList(String isList) {
    return isList != null && StringUtils.equals("1", isList);
  }

  @JsonIgnore
  public boolean isQuery() {
    return isQuery(this.isQuery);
  }

  @JsonIgnore
  public boolean isQuery(String isQuery) {
    return isQuery != null && StringUtils.equals("1", isQuery);
  }

  @JsonIgnore
  public boolean isSuperColumn() {
    return isSuperColumn(this.javaField);
  }

  @JsonIgnore
  public static boolean isSuperColumn(String javaField) {
    return StringUtils.equalsAnyIgnoreCase(javaField,
      // BaseEntity
      "createBy", "createTime", "updateBy", "updateTime", "remark",
      // TreeEntity
      "parentName", "parentId", "orderNum", "ancestors");
  }

  @JsonIgnore
  public boolean isUsableColumn() {
    return isUsableColumn(javaField);
  }

  @JsonIgnore
  public static boolean isUsableColumn(String javaField) {
    // isSuperColumn()中的名单用于避免生成多余Domain属性，若某些属性在生成页面时需要用到不能忽略，则放在此处白名单
    return StringUtils.equalsAnyIgnoreCase(javaField, "parentId", "orderNum", "remark");
  }

  public String readConverterExp() {
    String remarks = StringUtils.subBetween(this.columnComment, "（", "）");
    StringBuffer sb = new StringBuffer();
    if (StringUtils.isNotEmpty(remarks)) {
      for (String value : remarks.split(" ")) {
        if (StringUtils.isNotEmpty(value)) {
          Object startStr = value.subSequence(0, 1);
          String endStr = value.substring(1);
          sb.append("").append(startStr).append("=").append(endStr).append(",");
        }
      }
      return sb.deleteCharAt(sb.length() - 1).toString();
    } else {
      return this.columnComment;
    }
  }
}
