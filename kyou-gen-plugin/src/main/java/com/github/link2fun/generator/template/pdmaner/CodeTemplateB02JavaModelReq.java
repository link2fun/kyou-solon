package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.collection.CollectionUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;

import java.io.File;
import java.util.List;

@Component
public class CodeTemplateB02JavaModelReq implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 2;
  }

  @Override
  public String getTemplateCode() {
    return "B02_JAVA_MODEL_REQ";
  }

  @Override
  public String getTemplateName() {
    return "[Java] 请求参数";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    // 定义包名
    //region package 定义
    result.append("package ").append(tableInfo.getBasePackage())
      .packageSep().append(viewGroup.getProperties().getPackageName())
      .packageSep().append("modular")
      .packageSep().append(entity.getProperties().getPackageName())
      .packageSep().append("model").packageSep().append("req").append(";")
      .lineBreak().lineBreak();
    //endregion


    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.enums.YesOrNoEnum");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.pojo.base.entity.BaseModel");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.pojo.base.entity.Model");
    result.importClass("lombok.*");
    result.lineBreak();
    result.importClass("javax.validation.constraints.NotBlank");
    result.importClass("javax.validation.constraints.NotNull");
    result.importClass("java.math.BigDecimal");
    result.importClass("java.time.LocalDate");
    result.importClass("java.time.LocalDateTime");
    result.importClass("java.util.Map");

    result.lineBreak();


    result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName() + " 请求参数");

    // 类名
    result.append("public class ").append(entity.getProperties().getWebEntityType()).append("Req").append(" {").lineBreak().lineBreak();


    //region ===== 新增参数定义 =====
    result.tab_2().oneLineComment(entity.getDefName() + " 新增参数");
    result.tab_2().append("@Getter").lineBreak()
      .tab_2().append("@Setter").lineBreak()
      .tab_2().append("public static class AddReq implements Model {").lineBreak()
      .lineBreak();

    entity.getFields().stream().filter(PDManerEntityField::isInAddReq).forEach(field -> {

      result.tab_2().tab_2().oneLineComment(field.getDefName());
      if (field.isRequired()) {
        if (field.isString()) {
          if (field.getStrNotNull()) {
            // String 使用 NotNull 注解, 意味着默认值是空字符串, 方便建立索引
            result.tab_2().tab_2().notNull(field.getDefName());
          } else {
            result.tab_2().tab_2().notBlank(field.getDefName());
          }
        } else {
          result.tab_2().tab_2().notNull(field.getDefName());
        }
      }
      result.tab_2().tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append(";").lineBreak().lineBreak();
    });

    result.tab_2().append("}").lineBreak().lineBreak();
    //endregion


    //region ===== 修改参数定义 =====
    result.tab_2().oneLineComment(entity.getDefName() + " 修改参数");
    result.tab_2().append("@Getter").lineBreak()
      .tab_2().append("@Setter").lineBreak()
      .tab_2().append("public static class ModifyReq implements Model {").lineBreak()
      .lineBreak();

    entity.getFields().stream().filter(PDManerEntityField::isInModifyReq).forEach(field -> {

      result.tab_2().tab_2().oneLineComment(field.getDefName());
      if (field.isRequired()) {
        if (field.isString()) {
          if (field.getStrNotNull()) {
            // String 使用 NotNull 注解, 意味着默认值是空字符串, 方便建立索引
            result.tab_2().tab_2().notNull(field.getDefName() + "");
          } else {
            result.tab_2().tab_2().notBlank(field.getDefName() + "");
          }
        } else {
          result.tab_2().tab_2().notNull(field.getDefName());
        }
      }
      result.tab_2().tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append(";").lineBreak().lineBreak();
    });

    result.tab_2().append("}").lineBreak().lineBreak();
    //endregion


    //region ===== 查询参数定义 =====
    // ===== 查询参数 start =====
    result.tab_2().oneLineComment(entity.getDefName() + " 查询参数");
    result.tab_2().append("@Getter").lineBreak()
      .tab_2().append("@Setter").lineBreak()
      .tab_2().append("public static class SearchReq implements Model {").lineBreak()
      .lineBreak();

    entity.getFields().stream().filter(PDManerEntityField::isInSearchReq)
      .forEach(field -> {

        if (field.isQueryTypeBetweenOrRange()) {

          // 当前是范围查询
          result.tab_2().tab_2().oneLineComment(field.getDefName() + "-起始");
          result.tab_2().tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst() + "Start").append(";").lineBreak().lineBreak();

          result.tab_2().tab_2().oneLineComment(field.getDefName() + "-截至");
          result.tab_2().tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst() + "End").append(";").lineBreak().lineBreak();


        } else if (field.isQueryTypeIn()) {
          // 当前是多值查询
          // eg: private List<T> field;
          result.tab_2().tab_2().oneLineComment("List<"+field.getDefName()+">");
          result.tab_2().tab_2().append("private List<").append(field.getFieldJavaType()).append("> ").append(field.aliasCamelCaseFirst()).append(";").lineBreak().lineBreak();

        } else {
          result.tab_2().tab_2().oneLineComment(field.getDefName());
          result.tab_2().tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append(";").lineBreak().lineBreak();

        }
      });

    // 看看是否有关键字查询
    final List<PDManerEntityField> keywordFieldList = entity.fieldMethod("keyword");
    if (CollectionUtil.isNotEmpty(keywordFieldList)) {
      result.tab_2().tab_2().oneLineComment("关键字查询");
      result.tab_2().tab_2().append("private String keyword;").lineBreak().lineBreak();
    }

    result.tab_2().append("}").lineBreak().lineBreak();
    // ===== 查询参数End =====
    //endregion


    result.append("}");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.javaModularPackage() + ".model.req";
    File javaRoot = new File(GenUtils.getGenConfig().getBackendLocation(), "main/java");
    File fileDir = new File(javaRoot, subPackage.replace(".", "/"));

    File file = new File(fileDir, tableInfo.getEntity().getProperties().getWebEntityType() + "Req.java");

    return file.getAbsolutePath();
  }
}
