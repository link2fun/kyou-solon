package com.github.link2fun.generator.template.pdmaner;


import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import org.noear.solon.annotation.Component;

import java.io.File;

@Component
public class CodeTemplateB08ServiceItf implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 8;
  }

  @Override
  public String getTemplateCode() {
    return "B08_SERVICE_ITF";
  }

  @Override
  public String getTemplateName() {
    return "[Java] Service Interface";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    // 定义包名
    result.append("package ").append(tableInfo.javaModularPackage())
      .packageSep().append("service").append(";")
      .lineBreak().lineBreak();


    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Empty");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Page");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Resp");
    result.importClass(tableInfo.javaModularPackage() + ".model." + entity.getProperties().getWebEntityType());
    result.importClass(tableInfo.javaModularPackage() + ".model.req." + entity.getProperties().getWebEntityType() + "Req");
    result.importClass(tableInfo.getEntity().hasEntityMethod("import") || tableInfo.getEntity().hasEntityMethod("export"), tableInfo.javaModularPackage() + ".model.excel." + entity.getProperties().getWebEntityType() + "ExcelModel");

    result.importClass("java.util.List");

    result.lineBreak();


    // 类注释
    result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName() + " Service");
    // 接口声明
    result.append("public interface ").append(entity.getJavaServiceItfClassName()).append(" {")
      .lineBreak()
      .lineBreak();


    // 分页查询
    if (entity.hasEntityMethod("pageSearch")) {

      result
        .tab_2().line("/** 分页查询 " + entity.getDefName() + " */")

        // eg: <T> Page<T> pageSearchConfig(ActionContext context, Page<T> page, ConfigReq.SearchReq searchReq, Class<T> resultClass);
        .tab_2().lineTemplate("<T> Page<T> pageSearch{}(ActionContext context, Page<T> page, {} searchReq, Class<T> resultClass);",
          entity.entityVarUpperCamel(),entity.entityReqSearchReqClassName());
        result.lineBreak();
    }


    // 2. 列表查询
    if (entity.hasEntityMethod("listSearch")) {
      result
        .tab_2().line("/** 列表查询 " + entity.getDefName() + " */");

      // eg: <T> List<T> listSearchConfig(ActionContext context, SysConfig searchReq, Class<T> resultClass);
      result.tab_2().lineTemplate("<T> List<T> listSearch{}(ActionContext context, {} searchReq, Class<T> resultClass);", entity.entityVarUpperCamel(), entity.entityReqSearchReqClassName());

       result .lineBreak();

    }


    // 3. 根据主键查询
    boolean hasId = entity.getFields().stream().anyMatch(PDManerEntityField::getPrimaryKey);
    if (hasId && !entity.isDetailTable()) {

      // 有主键，创建一个根据主键查询数据的方法

      result.tab_2().line(" /** 根据 " + entity.getDefName() + "主键 查询" + entity.getDefName() + " */")
        // eg: <T> T detailById(ActionContext context, String configId, Class<T> resultClass);
        .tab_2().lineTemplate("<T> T findOneBy{}(ActionContext context, {} {}, Class<T> resultClass);", entity.primaryKeyField().getFieldNameUpperCamel(), entity.primaryKeyField().getFieldJavaType(), entity.primaryKeyVar())
        .lineBreak();
    }


    // 4. 根据唯一编码查询
    if (entity.hasUniqueCode()) {
      entity.uniqueFieldList().forEach(uniqueField -> {
        result.tab_2().oneLineComment("根据 " + uniqueField.getDefName() + " 查询" + entity.getDefName());
        result.tab_2().append(entity.entityClassName()).append(" findBy" + uniqueField.aliasPascalCaseFirst()).append("(").append(uniqueField.getFieldJavaType()).append(" ").append(uniqueField.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();

      });
    }


    // 5. 断言编码不重复
    if (entity.hasUniqueCode()) {
      entity.uniqueFieldList().forEach(uniqueField -> {
        result.tab_2().oneLineComment("断言 " + entity.getDefName() + " " + uniqueField.getDefName() + " 不重复")
          .tab_2().append("void").append(" assert" + uniqueField.aliasPascalCaseFirst()).append("NotDuplicate(").append(uniqueField.getFieldJavaType()).append(" ").append(uniqueField.aliasCamelCaseFirst()).append(", String ").append(entity.getProperties().getWebEntityVar() + "Id").append(");").lineBreak().lineBreak();

      });
    }


    if (entity.hasEntityMethod("add")) {
      // 6. 新增
      result.tab_2().oneLineComment("添加 " + entity.getDefName() + "")
        .tab_2().append("Resp<String>").append(" add" + entity.entityVarUpperCamel()).append("(").append(entity.getProperties().getWebEntityType()).append("Req.AddReq ").append("addReq").append(");").lineBreak().lineBreak();

    }

    if (entity.hasEntityMethod("modify")) {

      // 6. 修改
      result.tab_2().oneLineComment("修改 " + entity.getDefName() + "")
        .tab_2().append("Resp<String>").append(" modify" + entity.entityVarUpperCamel()).append("(").append(entity.getProperties().getWebEntityType()).append("Req.ModifyReq ").append("modifyReq").append(");").lineBreak().lineBreak();
    }

    if (entity.hasEntityMethod("del")) {

      // 删除
      result.tab_2().oneLineComment("删除 " + entity.getDefName() + "")
        .tab_2().append("Resp<Empty>").append(" del" + entity.entityVarUpperCamel()).append("(String ").append(entity.getProperties().getWebEntityVar() + "Id").append(");").lineBreak().lineBreak();
    }


    if (entity.hasEntityMethod("import")) {

      result.tab_2().oneLineComment("导入 " + entity.getDefName() + "")
        .tab_2().append("Resp<Empty>").append(" import" + entity.entityVarUpperCamel()).append("WithReplaceAll(").append("List<").append(entity.getProperties().getWebEntityType()).append("ExcelModel> ").append(entity.getProperties().getWebEntityVar() + "ExcelModelList").append(");").lineBreak().lineBreak();
    }


    // findListBy 方法生成
    entity.fieldMethod("findListBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().append("List<").append(entity.getProperties().getWebEntityType()).append("> findListBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
      });


    // findByList 方法生成
    entity.fieldMethod("findByList")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().append("List<").append(entity.getProperties().getWebEntityType()).append("> findListBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append("> ").append(field.aliasCamelCaseFirst()).append("List);").lineBreak().lineBreak();

      });


    // findOneBy 方法生成
    entity.fieldMethod("findOneBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        if (field.hasAlias()) {
          result.tab_2().append("").append(entity.getProperties().getWebEntityType()).append(" findOneBy").append(field.aliasPascalCase()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCase()).append(");").lineBreak().lineBreak();
        } else {
          result.tab_2().append("").append(entity.getProperties().getWebEntityType()).append(" findOneBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
        }
      });

    // findOptBy 方法生成
    entity.fieldMethod("findOptBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().append("Optional<").append(entity.getProperties().getWebEntityType()).append("> findOptBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
      });

    // countBy 方法生成
    entity.fieldMethod("countBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 计数 " + entity.getDefName());
        result.tab_2().append("").append("Long").append(" countBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
      });

    // countByList
    entity.fieldMethod("countByList")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 计数 " + entity.getDefName());
        result.tab_2().append("").append("Long").append(" countBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append("> ").append(field.aliasCamelCaseFirst()).append("List);").lineBreak().lineBreak();
      });

    // existsCheckBy
    entity.fieldMethod("existsCheckBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 判断 " + entity.getDefName() + " 是否存在");
        result.tab_2().append("").append("Boolean").append(" existsCheckBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
      });

    // ExistsCheckByIgnoreCurrent
    entity.fieldMethod("ExistsCheckByIgnoreCurrent")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 判断 " + entity.getDefName() + " 是否存在, 忽略当前对象");
        result.tab_2().append("").append("Boolean").append(" existsCheckBy").append(field.aliasPascalCaseFirst()).append("IgnoreCurrent(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append(", ").append(entity.primaryKeyField().getFieldJavaType()).append(" ").append(entity.primaryKeyField().aliasCamelCaseFirst()).append(");").lineBreak().lineBreak();
      });


    // delBy 方法生成
    entity.fieldMethod("delBy")
      .forEach(field -> {
        result.tab_2().append(" /** 根据 ").append(field.getDefName()).append(" 删除 ").append(entity.getDefName()).append(" */").lineBreak()
          .tab_2().append("void ").append("delBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append(");")
          .lineBreak().lineBreak();
      });


    // delByList 方法生成
    entity.fieldMethod("delByList")
      .forEach(field -> {
        result.tab_2().append(" /** 根据 ").append(field.getDefName()).append(" 删除 ").append(entity.getDefName()).append(" */").lineBreak()
          .tab_2().append("void ").append("delBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append(">").space().append(field.aliasCamelCaseFirst()).append("List);")
          .lineBreak().lineBreak();
      });


    // loadOneBy 方法生成
    entity.fieldMethod("loadOneBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 从缓存中加载 " + entity.getDefName());
        result.tab_2().append("default ").append(entity.getProperties().getWebEntityType()).append(" loadOneNullableBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak();
        result.tab_2().tab_2().lineTemplate("return EntityCacheHolder.loadOptByField({}::{}, {}, (_key) -> java.util.Optional.ofNullable(findOneBy{}(_key))).orElse(null);", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst(), field.aliasPascalCaseFirst())
          .tab_2().line("}").lineBreak();

        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 从缓存中加载 " + entity.getDefName());
        result.tab_2().append("default Optional<").append(entity.getProperties().getWebEntityType()).append("> loadOneOptBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak();
        result.tab_2().tab_2().lineTemplate("return EntityCacheHolder.loadOptByField({}::{}, {}, (_key) -> java.util.Optional.ofNullable(findOneBy{}(_key)));", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst(), field.aliasPascalCaseFirst())
          .tab_2().line("}").lineBreak();

        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 移除缓存中的 " + entity.getDefName());
        result.tab_2().append("default void invalidateCacheBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak();
        result.tab_2().tab_2().lineTemplate("EntityCacheHolder.removeByField({}::{}, {});", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst())
          .tab_2().line("}").lineBreak();
      });


    // loadListBy 方法生成
    entity.fieldMethod("loadListBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 从缓存中加载 " + entity.getDefName());
        result.tab_2().append("default List<").append(entity.getProperties().getWebEntityType()).append("> loadListBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak();
        result.tab_2().tab_2().lineTemplate("return EntityCacheHolder.loadListByField({}::{}, {}, (_key) -> java.util.Optional.ofNullable(findListBy{}(_key)));", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst(), field.aliasPascalCaseFirst())
          .tab_2().line("}").lineBreak();

        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 移除缓存中的 " + entity.getDefName());
        result.tab_2().append("default void invalidateCacheBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak();
        result.tab_2().tab_2().lineTemplate("EntityCacheHolder.removeByField({}::{}, {});", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst())
          .tab_2().line("}").lineBreak();
      });


    result.append("}");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.javaModularPackage() + ".service";
    File javaRoot = new File(GenUtils.getGenConfig().getBackendLocation(), "main/java");
    File fileDir = new File(javaRoot, subPackage.replace(".", "/"));

    File file = new File(fileDir, "I" + tableInfo.getEntity().getProperties().getWebFunctionType() + "Service.java");

    return file.getAbsolutePath();
  }
}
