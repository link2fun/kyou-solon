package com.github.link2fun.generator.domain;

import cn.hutool.core.util.ArrayUtil;
import com.easy.query.core.annotation.*;
import com.easy.query.core.enums.RelationTypeEnum;
import com.easy.query.core.proxy.ProxyEntityAvailable;
import com.github.link2fun.generator.domain.proxy.GenTableProxy;
import com.github.link2fun.support.constant.GenConstants;
import com.github.link2fun.support.core.domain.BaseEntity;
import com.github.link2fun.support.utils.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldNameConstants;
import org.noear.solon.validation.annotation.NotBlank;
import org.noear.solon.validation.annotation.Validated;

import java.io.Serial;
import java.util.List;

/**
 * 业务表 gen_table
 *
 * @author ruoyi
 */
@EqualsAndHashCode(callSuper = true)
@FieldNameConstants
@Data
@Table("gen_table")
@EntityProxy
public class GenTable extends BaseEntity implements ProxyEntityAvailable<GenTable, GenTableProxy> {
  @Serial
  private static final long serialVersionUID = 1L;

  /** 编号 */
  @Column(value = "table_id", primaryKey = true, generatedKey = true)
  private Long tableId;

  /** 表名称 */
  @NotBlank(message = "表名称不能为空")
  @Column("table_name")
  private String tableName;

  /** 表描述 */
  @NotBlank(message = "表描述不能为空")
  @Column("table_comment")
  private String tableComment;

  /** 关联父表的表名 */
  @Column("sub_table_name")
  private String subTableName;

  /** 本表关联父表的外键名 */
  @Column("sub_table_fk_name")
  private String subTableFkName;

  /** 实体类名称(首字母大写) */
  @NotBlank(message = "实体类名称不能为空")
  @Column("class_name")
  private String className;

  /** 使用的模板（crud单表操作 tree树表操作 sub主子表操作） */
  @Column("tpl_category")
  private String tplCategory;

  /** 前端类型（element-ui模版 element-plus模版） */
  @Column("tpl_web_type")
  private String tplWebType;

  /** 生成包路径 */
  @NotBlank(message = "生成包路径不能为空")
  @Column("package_name")
  private String packageName;

  /** 生成模块名 */
  @NotBlank(message = "生成模块名不能为空")
  @Column("module_name")
  private String moduleName;

  /** 生成业务名 */
  @NotBlank(message = "生成业务名不能为空")
  @Column("business_name")
  private String businessName;

  /** 生成功能名 */
  @NotBlank(message = "生成功能名不能为空")
  @Column("function_name")
  private String functionName;

  /** 生成作者 */
  @NotBlank(message = "作者不能为空")
  @Column("function_author")
  private String functionAuthor;

  /** 生成代码方式（0zip压缩包 1自定义路径） */
  @Column("gen_type")
  private String genType;

  /** 生成路径（不填默认项目路径） */
  @Column("gen_path")
  private String genPath;

  /** 主键信息 */
  @ColumnIgnore
  private GenTableColumn pkColumn;

  /** 子表信息 */
  @ColumnIgnore
  private GenTable subTable;

  /** 表列信息 */
  @Validated
  @Navigate(value = RelationTypeEnum.OneToMany, selfProperty = Fields.tableId, targetProperty = GenTableColumn.Fields.tableId)
  private List<GenTableColumn> columns;

  /** 其它生成选项 */
  @Column("options")
  private String options;

  /** 备注 */
  @Column(value = "remark")
  private String remark;

  /** 树编码字段 */
  @ColumnIgnore
  private String treeCode;

  /** 树父编码字段 */
  @ColumnIgnore
  private String treeParentCode;

  /** 树名称字段 */
  @ColumnIgnore
  private String treeName;

  /** 上级菜单ID字段 */
  @ColumnIgnore
  private String parentMenuId;

  /** 上级菜单名称字段 */
  @ColumnIgnore
  private String parentMenuName;

  public boolean isSub() {
    return isSub(this.tplCategory);
  }

  public static boolean isSub(String tplCategory) {
    return tplCategory != null && StringUtils.equals(GenConstants.TPL_SUB, tplCategory);
  }

  public boolean isTree() {
    return isTree(this.tplCategory);
  }

  public static boolean isTree(String tplCategory) {
    return tplCategory != null && StringUtils.equals(GenConstants.TPL_TREE, tplCategory);
  }

  public boolean isCrud() {
    return isCrud(this.tplCategory);
  }

  public static boolean isCrud(String tplCategory) {
    return tplCategory != null && StringUtils.equals(GenConstants.TPL_CRUD, tplCategory);
  }

  public boolean isSuperColumn(String javaField) {
    return isSuperColumn(this.tplCategory, javaField);
  }

  public static boolean isSuperColumn(String tplCategory, String javaField) {
    if (isTree(tplCategory)) {
      return StringUtils.equalsAnyIgnoreCase(javaField,
          ArrayUtil.addAll(GenConstants.TREE_ENTITY, GenConstants.BASE_ENTITY));
    }
    return StringUtils.equalsAnyIgnoreCase(javaField, GenConstants.BASE_ENTITY);
  }
}