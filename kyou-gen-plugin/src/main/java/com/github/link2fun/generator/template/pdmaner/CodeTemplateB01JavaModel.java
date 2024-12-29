package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;


import java.io.File;

@Component
public class CodeTemplateB01JavaModel implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 1;
  }

  @Override
  public String getTemplateCode() {
    return "B01_JAVA_MODEL" ;
  }

  @Override
  public String getTemplateName() {
    return "[Java] 实体类" ;
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    // 定义包名
    result.append("package ").append(tableInfo.getBasePackage())
      .packageSep().append(viewGroup.getProperties().getPackageName())
      .packageSep().append("modular")
      .packageSep().append(entity.getProperties().getPackageName())
      .packageSep().append("model").append(";").lineBreak().lineBreak();

    // 如果有主键, 且主键类型不是String, 需要引入 import com.fasterxml.jackson.databind.annotation.JsonSerialize;
    //import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
    if (StrUtil.isNotBlank(entity.primaryKeyField().getDefKey())) {
      if (!StrUtil.equals(entity.primaryKeyField().getFieldJavaType(), "String")) {
        result.importClass("com.fasterxml.jackson.databind.annotation.JsonSerialize");
        result.importClass("com.fasterxml.jackson.databind.ser.std.ToStringSerializer");
      }
    }

//    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.enums.YesOrNoEnum");
//    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.pojo.base.entity.BaseModel");
    result.importClass("com.easy.query.core.annotation.*");
    result.importClass("com.easy.query.core.basic.extension.logicdel.LogicDeleteStrategyEnum");
    result.importClass("com.easy.query.core.basic.extension.version.VersionIntStrategy");
    result.importClass("lombok.*");
    result.lineBreak();
    result.importClass("javax.validation.constraints.NotBlank");
    result.importClass("javax.validation.constraints.NotNull");

    // 动态引入一些字段类型
    result.importClass(entity.getFields().stream().anyMatch(field->StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(),"BigDecimal")),"java.math.BigDecimal");
    result.importClass(entity.getFields().stream().anyMatch(field->StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(),"LocalDate")),"java.time.LocalDate");
    result.importClass(entity.getFields().stream().anyMatch(field->StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(),"LocalDateTime")),"java.time.LocalDateTime");
    result.importClass(entity.getFields().stream().anyMatch(field->StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(),"Map")),"java.util.Map");

    result.lineBreak();


    // 类注释
    if (entity.dbEntity()) {

      result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName() + " 数据库实体");
    } else {
      result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName());
    }

    // 类名
    result.line("@EntityProxy");
    result.line("@FieldNameConstants");
    result.line("@Data");
    result.line("@EasyAlias(value = \"" + entity.entityProxyVar() + "\")");
    if (entity.dbEntity()) {
      result.line("@Table(value = \"" + entity.getDefKey() + "\")");
    }
    result.append("public class ").append(entity.getProperties().getWebEntityType()).append(entity.dbEntity(), " implements ProxyEntityAvailable<").append(entity.entityClassName()).append(", ").append(entity.entityProxyClassName()).append(">").append(!entity.dbEntity(), "").append(" {").lineBreak();


    // 字段
    entity.getFields()
      .forEach(field -> {
        result.lineBreak()
          .tab_2().oneLineComment(field.getDefName());

        // 加上 @NotNull 或 @NotBlank
        if ((field.isInAddReq() || field.isInModifyReq())
          && !field.getIsVersion() && field.getNotNull() && !field.getPrimaryKey()) {
          if (StrUtil.equals(field.getFieldJavaType(), "String")) {
            if (field.getStrNotNull()) {
              // String 使用 NotNull 注解, 意味着默认值是空字符串, 方便建立索引
              result.tab_2().notNull(field.getDefName());
            } else {
              result.tab_2().notBlank(field.getDefName());
            }
          } else {
            result.tab_2().notNull(field.getDefName());
          }
        }

        // 如果是主键, 且类型不是 String的话, 需要加上 @JsonSerialize(using = ToStringSerializer.class)
        if (field.getPrimaryKey() && !StrUtil.equals(field.getFieldJavaType(), "String")) {
          result.tab_2().line("@JsonSerialize(using = ToStringSerializer.class)");
        }

        if (field.getIsLogic() && entity.dbEntity()) {
          result.tab_2().line("@LogicDelete(strategy = LogicDeleteStrategyEnum.BOOLEAN)");
        }
        if (field.getIsVersion() && entity.dbEntity()) {
          result.tab_2().line("@Version(strategy = VersionIntStrategy.class)");
        }

        // 加上 easy query @Column 注解
        if (entity.dbEntity()) {
          result.tab_2().append("@Column(value=\"").append(field.getDefKey()).append("\"")
            .append(field.getPrimaryKey(), ", primaryKey = true")
            .append(field.getPrimaryKey() && field.getAutoIncrement(), ", generatedKey = true")
            .append(field.getPrimaryKey() && !field.getAutoIncrement(), ", primaryKeyGenerator = SnowflakePrimaryKeyGenerator.class")
            .append(")").lineBreak();

        }

        // 加上 字段
        result.tab_2().append("private ").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append(";").lineBreak();


      });


    result.append("}");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.javaModularPackage() + ".model" ;
    File javaRoot = new File(GenUtils.getGenConfig().getBackendLocation(), "main/java");
    File fileDir = new File(javaRoot, subPackage.replace(".", "/"));

    File file = new File(fileDir, tableInfo.getEntity().getProperties().getWebEntityType() + ".java");

    return file.getAbsolutePath();
  }
}
