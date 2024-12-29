package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.util.StrUtil;
import com.github.link2fun.generator.domain.pdmaner.TableInfo;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntity;
import com.github.link2fun.generator.domain.pdmaner.dto.entity.PDManerEntityField;
import com.github.link2fun.generator.domain.pdmaner.dto.viewgroup.PDManerViewGroup;
import com.github.link2fun.generator.template.CodeTemplate;
import com.github.link2fun.generator.util.CodeBuilder;
import com.github.link2fun.generator.util.GenUtils;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Component;

import java.io.File;

@Slf4j
@Component
public class CodeTemplateB09ServiceImpl implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 9;
  }

  @Override
  public String getTemplateCode() {
    return "B09_SERVICE_IMPL";
  }

  @Override
  public String getTemplateName() {
    return "[Java] Service Impl";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    // 定义包名
    result.append("package ").append(tableInfo.javaModularPackage())
      .packageSep().append("service.impl").append(";")
      .lineBreak().lineBreak();


    result.importClass("cn.hutool.core.lang.Assert");
    result.importClass("cn.hutool.core.util.ObjectUtil");
    result.importClass("cn.hutool.core.util.StrUtil");
    result.importClass("com.google.common.base.Preconditions");

    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Empty");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Page");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Resp");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.exception.DataNotFoundException");
    result.importClass(tableInfo.javaModularPackage() + ".model." + entity.getProperties().getWebEntityType());
    result.importClass(tableInfo.javaModularPackage() + ".model.req." + entity.getProperties().getWebEntityType() + "Req");
    result.importClass(tableInfo.javaModularPackage() + ".model.excel." + entity.getProperties().getWebEntityType() + "ExcelModel");
    result.importClass(tableInfo.javaModularPackage() + ".mapper." + entity.getProperties().getWebEntityType() + "Mapper");
    result.importClass(tableInfo.javaModularPackage() + ".service.I" + entity.getProperties().getWebFunctionType() + "Service");

    result.importClass("lombok.extern.slf4j.Slf4j");
    result.importClass("org.springframework.beans.factory.annotation.Autowired");
    result.importClass("org.springframework.stereotype.Service");
    result.importClass("org.springframework.transaction.annotation.Propagation");
    result.importClass("org.springframework.transaction.annotation.Transactional");
    result.importClass("cn.hutool.core.util.StrUtil");
    result.importClass("cn.hutool.core.lang.Assert");
    result.importClass("java.util.List");
    result.importClass("java.util.stream.Collectors");
    result.importClass("java.util.Objects");
    //

    result.lineBreak();


    // 类注释
    result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName() + " Service");
    // 接口声明
    result.line("@Slf4j");
    result.line("@Service");
    result.append("public class ").append(entity.getJavaServiceImpl()).append(" extends ServiceImpl<")
      .append(entity.getJavaMapper()).append(",").append(entity.getProperties().getWebEntityType()).append("> implements ")
      .append(entity.getJavaServiceItfClassName()).append(" {")
      .lineBreak()
      .lineBreak();


    // 注入变量
    result.tab_2().line("@Autowired")
      .tab_2().line("private " + entity.getJavaServiceItfClassName() + " " + entity.getJavaServiceItfVar() + ";").lineBreak();


    // 分页查询   ======================================================
    if (entity.hasEntityMethod("pageSearch")) {

      result
        .tab_2().line(" /** 分页查询 " + entity.getDefName() + " */")
        .tab_2().line("@Override")
        .tab_2().line("@Tran(policy = TranPolicy.supports)");


      result.tab_2().lineTemplate("public <T> Page<T> pageSearchConfig(final ActionContext context,final Page<T> page, final {} searchReq, final Class<T> resultClass) {",
        entity.entityReqSearchReqClassName());


      result.tab_2().tab_2().lineBreak()
        .tab_2().tab_2().lineTemplate("EasyPageResult<T> pageResult = entityQuery.queryable({}.class)", entity.entityClassName());

      result.tab_2().tab_2().lineTemplate(".where({} -> {", entity.entityProxyVar());


      entity.getFields().stream()
        .filter(PDManerEntityField::isInSearchReq).forEach(field -> {

          if (field.isQueryTypeBetweenOrRange()) {
            // 范围查询
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().ge(Objects.nonNull(searchReq.{}Start()), searchReq.{}Start()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().le(Objects.nonNull(searchReq.{}End()), searchReq.{}End()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());

          } else if (field.isQueryTypeIn()) {
            // 多值查询 in list
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().{}({}(searchReq.{}()), searchReq.{}()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.easyQueryType(),
             "CollectionUtil.isNotEmpty",
              field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());
          } else {
            // 普通的单值查询
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().{}({}(searchReq.{}()), searchReq.{}()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.easyQueryType(),
              field.isString() ? "StrUtil.isNotBlank" : "Objects.nonNull",
              field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());

          }
        });
      // 看看是否有关键字查询
//      final List<PDManerEntityField> keywordFieldList = entity.fieldMethod("keyword");
//      if (CollectionUtil.isNotEmpty(keywordFieldList)){
//        result.tab_2().tab_2().tab_2().append(".nested(StrUtil.isNotBlank(searchReq.getKeyword()),")
//          .lineBreak()
//          .tab_2().tab_2().tab_2().tab_2().append("wrapper -> wrapper").lineBreak();
//        keywordFieldList.forEach(field -> {
//          result.tab_2().tab_2().tab_2().tab_2().tab_2().append(".or().like(")
//            .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", searchReq.getKeyword())").lineBreak();
//        });
//        result.tab_2().tab_2().tab_2().line(")");
//
//      }
      result.tab_2().tab_2().line("})");
//      result.tab_2().tab_2().tab_2().line(".page(page);");
      //.selectAutoInclude(resultClass)
      result.tab_2().tab_2().line(".selectAutoInclude(resultClass)");
      //      .toPageResult(page.getPageNum(), page.getPageSize());
      result.tab_2().tab_2().line(".toPageResult(page.getPageNum(), page.getPageSize(), page.getTotal());");
      //    return Page.of(pageResult);
      result.tab_2().tab_2().line("return Page.of(pageResult);");


      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    // 2. 列表查询   ======================================================
    if (entity.hasEntityMethod("listSearch")) {
      result
        .tab_2().line(" /** 列表查询 " + entity.getDefName() + " */")
        .tab_2().line("@Override")
        .tab_2().line("@Tran(policy = TranPolicy.supports)");

      // public <T> List<T> listSearchConfig(ActionContext context, SysConfig searchReq, Class<T> resultClass) {
      result.tab_2().lineTemplate("public <T> List<T> listSearch{}(final ActionContext context, final {} searchReq, final Class<T> resultClass) {",
        entity.entityVarUpperCamel(),
        entity.entityReqSearchReqClassName());

      result.tab_2().tab_2().lineBreak()
        .tab_2().tab_2().lineTemplate("return entityQuery.queryable({}.class)", entity.entityClassName());

      result.tab_2().tab_2().tab_2().lineTemplate(".where({} -> {", entity.entityProxyVar());


      entity.getFields().stream()
        .filter(PDManerEntityField::isInSearchReq).forEach(field -> {
          if (field.isQueryTypeBetweenOrRange()) {
            // 范围查询
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().ge(Objects.nonNull(searchReq.{}Start()), searchReq.{}Start()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().le(Objects.nonNull(searchReq.{}End()), searchReq.{}End()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());

          } else if (field.isQueryTypeIn()) {
            // 多值查询 in list
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().{}({}(searchReq.{}()), searchReq.{}()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.easyQueryType(),
              "CollectionUtil.isNotEmpty",
              field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());
          } else {
            // 普通的单值查询
            result.tab_2().tab_2().tab_2().lineTemplate("{}.{}().{}({}(searchReq.{}()), searchReq.{}()); // {}",
              entity.entityProxyVar(), field.fieldName(), field.easyQueryType(),
              field.isString() ? "StrUtil.isNotBlank" : "Objects.nonNull",
              field.aliasGetterFirst(), field.aliasGetterFirst(), field.getDefName());

          }
        });
      // 看看是否有关键字查询
//      final List<PDManerEntityField> keywordFieldList = entity.fieldMethod("keyword");
//      if (CollectionUtil.isNotEmpty(keywordFieldList)){
//        result.tab_2().tab_2().tab_2().append(".nested(StrUtil.isNotBlank(searchReq.getKeyword()),")
//          .lineBreak()
//          .tab_2().tab_2().tab_2().tab_2().append("wrapper -> wrapper").lineBreak();
//        keywordFieldList.forEach(field -> {
//          result.tab_2().tab_2().tab_2().tab_2().tab_2().append(".or().like(")
//            .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", searchReq.getKeyword())").lineBreak();
//        });
//        result.tab_2().tab_2().tab_2().line(")");
//
//      }
      result.tab_2().tab_2().tab_2().line("})");
//      result.tab_2().tab_2().tab_2().line(".page(page);");
      //.selectAutoInclude(resultClass)
      result.tab_2().tab_2().tab_2().line(".selectAutoInclude(resultClass)");
      //      .toPageResult(page.getPageNum(), page.getPageSize());
      result.tab_2().tab_2().tab_2().line(".toList();");

      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    // 3. 根据主键查询   ======================================================
    boolean hasId = entity.getFields().stream().anyMatch(PDManerEntityField::getPrimaryKey);
    if (hasId && !entity.isDetailTable()) {
      // 有主键，创建一个根据主键查询数据的方法

      result.tab_2().line(" /** 根据 " + entity.getDefName() + "主键 查询" + entity.getDefName() + " */")
        .tab_2().line("@Override")

        // eg: @Tran(policy = TranPolicy.supports)
        .tab_2().line("@Tran(policy = TranPolicy.supports)")

        // eg: public <T> T findOneByConfigId(final ActionContext context, final String configId, Class<T> resultClass) {
        .tab_2().lineTemplate("public <T> T findOneBy{}(final ActionContext context, final {} {}, final Class<T> resultClass){", entity.primaryKeyField().getFieldNameUpperCamel(), entity.primaryKeyField().getFieldJavaType(), entity.primaryKeyVar());

      //Preconditions.checkNotNull(configId, "configId is null");
      result.tab_2().tab_2().lineTemplate("Preconditions.checkNotNull({}, \"{} is null\");", entity.primaryKeyVar(), entity.primaryKeyVar());
      //
      //    return entityQuery.queryable(SysConfig.class)
      result.tab_2().tab_2().lineTemplate("return entityQuery.queryable({}.class)", entity.entityClassName());
      //      .whereById(configId)
      result.tab_2().tab_2().tab_2().lineTemplate(".whereById({})", entity.primaryKeyVar());
      //      .selectAutoInclude(resultClass)
      result.tab_2().tab_2().tab_2().lineTemplate(".selectAutoInclude(resultClass)");
      //      .singleOrNull();
      result.tab_2().tab_2().tab_2().line(".singleOrNull();");


      result.tab_2().line("}")
        .lineBreak();
    }


    // 4. 根据唯一编码查询 ======================================================
    if (entity.hasUniqueCode()) {
      entity.uniqueFieldList()
        .forEach(uniqueField -> {
          result.tab_2().oneLineComment("根据 " + uniqueField.getDefName() + " 查询" + entity.getDefName())
            .tab_2().line("@Override")
            .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)")
            .tab_2().append("public ").append(entity.getProperties().getWebEntityType()).append(" findBy" + uniqueField.aliasPascalCaseFirst()).append("(").append(uniqueField.getFieldJavaType()).append(" ").append(uniqueField.aliasCamelCaseFirst()).append("){").lineBreak()
            // 参数判断
            .tab_2().tab_2().line("if (StrUtil.isBlank(" + uniqueField.aliasCamelCaseFirst() + ")) {")
            .tab_2().tab_2().tab_2().line("throw new DataNotFoundException(\"" + uniqueField.getDefName() + " 不能为空\");")
            .tab_2().tab_2().line("}")

            // 数据库查询
            .tab_2().tab_2()
            .append("return lambdaQuery().eq(").append(entity.getProperties().getWebEntityType()).append("::").append(uniqueField.aliasGetterFirst()).append(", ").append(uniqueField.aliasCamelCaseFirst()).append(").one();").lineBreak()


            .tab_2().line("}")
            .lineBreak().lineBreak();
        });

    }


    // 5. 断言编码不重复 ======================================================
    if (entity.hasUniqueCode()) {
      entity.uniqueFieldList().forEach(uniqueCodeField -> {
        result.tab_2().oneLineComment("断言 " + entity.getDefName() + " " + uniqueCodeField.getDefName() + " 不重复")
          .tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)")
          .tab_2().append("public void").append(" assert" + uniqueCodeField.aliasPascalCaseFirst()).append("NotDuplicate(").append(uniqueCodeField.getFieldJavaType()).append(" ").append(uniqueCodeField.aliasCamelCaseFirst()).append(", String ").append(entity.getProperties().getWebEntityVar() + "Id").append("){").lineBreak()

          .tab_2().tab_2().line("Long countBy" + uniqueCodeField.aliasPascalCaseFirst() + " = lambdaQuery().eq(" + entity.getProperties().getWebEntityType() + "::" + uniqueCodeField.aliasGetterFirst() + ", " + uniqueCodeField.aliasCamelCaseFirst() + ")")
          .tab_2().tab_2().tab_2().line(".ne(StrUtil.isNotBlank(" + entity.getProperties().getWebEntityVar() + "Id), " + entity.getProperties().getWebEntityType() + "::getId, " + entity.getProperties().getWebEntityVar() + "Id)")
          .tab_2().tab_2().tab_2().line(".last(\"limit 1\").count();")
          .tab_2().tab_2().line("Assert.isTrue(countBy" + uniqueCodeField.aliasPascalCaseFirst() + " < 1, \"" + entity.getDefName() + " " + uniqueCodeField.getDefName() + " \"+" + uniqueCodeField.aliasCamelCaseFirst() + "+\"重复\");")


          .tab_2().line("}")
          .lineBreak().lineBreak();
      });

    }


    // 6. 新增  ======================================================
    if (entity.hasEntityMethod("add")) {
      result.tab_2().oneLineComment("添加 " + entity.getDefName() + "")

        .tab_2().line("@Override")
        .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")
        .tab_2().append("public Resp<String>").append(" add" + entity.getProperties().getWebEntityVarUpperCamel()).append("(").append(entity.getProperties().getWebEntityType()).append("Req.AddReq ").append("addReq").append("){").lineBreak();


      // 去重
      if (entity.hasUniqueCode()) {

        entity.uniqueFieldList()
          .forEach(uniqueField -> {
            result.lineBreak()
              .tab_2().tab_2().append("// ").append(uniqueField.getDefName()).append(" 不允许重复").lineBreak()
              .tab_2().tab_2().append(entity.getJavaServiceItfVar()).append(".assert" + uniqueField.aliasPascalCaseFirst()).append("NotDuplicate(addReq.").append(uniqueField.aliasGetterFirst()).append("());").lineBreak();
          });


      }


      // 定义变量
      result.lineBreak().tab_2().tab_2().line("" + entity.getProperties().getWebEntityType() + " " + entity.getProperties().getWebEntityVar() + " = new " + entity.getProperties().getWebEntityType() + "();").lineBreak();


      // 循环复制
      entity.getFields().stream().filter(PDManerEntityField::isInAddReq)
        .forEach(field -> {
          result.tab_2().tab_2().line("" + entity.getProperties().getWebEntityVar() + "." + field.getFieldSetter() + "(addReq." + field.aliasGetterFirst() + "()); // " + field.getDefName() + "");
        });


      // 执行保存
      result.tab_2().tab_2().line("Assert.isTrue(save(" + entity.getProperties().getWebEntityVar() + "));").lineBreak();


      // 返回结果

      result.tab_2().tab_2().line("return Resp.ofSuccessData(" + entity.getProperties().getWebEntityVar() + ".getId());").lineBreak();


      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    // 6. 修改  ======================================================
    if (entity.hasEntityMethod("modify")) {
      result.tab_2().oneLineComment("修改 " + entity.getDefName() + "")
        .tab_2().line("@Override")
        .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")
        .tab_2().append("public Resp<String>").append(" modify" + entity.getProperties().getWebEntityVarUpperCamel()).append("(").append(entity.getProperties().getWebEntityType()).append("Req.ModifyReq ").append("modifyReq").append("){").lineBreak();


      result.tab_2().tab_2().line("" + entity.getProperties().getWebEntityType() + " " + entity.getProperties().getWebEntityVar() + " = " + entity.getProperties().getWebEntityVar() + "Service.findBy" + entity.getProperties().getWebEntityVarUpperCamel() + "IdAssertExists(modifyReq.getId());").lineBreak();

      // 循环复制
      entity.getFields().stream().filter(PDManerEntityField::isInAddReq)
        .forEach(field -> {
          result.tab_2().tab_2().line("" + entity.getProperties().getWebEntityVar() + "." + field.getFieldSetter() + "(modifyReq." + field.aliasGetterFirst() + "()); //" + field.getDefName() + "");
        });

      result.lineBreak().tab_2().tab_2().line("Assert.isTrue(updateById(" + entity.getProperties().getWebEntityVar() + "));").lineBreak();
      result.tab_2().tab_2().line("return Resp.ofSuccessData(" + entity.getProperties().getWebEntityVar() + ".getId());").lineBreak();


      result.tab_2().line("}").lineBreak()
        .lineBreak();
    }


    // 删除
    if (entity.hasEntityMethod("del")) {
      result.tab_2().oneLineComment("删除 " + entity.getDefName() + "")
        .tab_2().line("@Override")
        .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")
        .tab_2().append("public Resp<Empty>").append(" del" + entity.getProperties().getWebEntityVarUpperCamel()).append("(String ").append(entity.getProperties().getWebEntityVar() + "Id").append("){").lineBreak();

      result.tab_2().tab_2().line("removeById(" + entity.getProperties().getWebEntityVar() + "Id);");
      result.tab_2().tab_2().line("return Resp.ofSuccess();");


      result.tab_2().line("}").lineBreak().lineBreak();
    }


    // 导入   ======================================================
    if (entity.hasEntityMethod("import")) {
      result.tab_2().oneLineComment("导入 " + entity.getDefName() + "")
        .tab_2().line("@Override")
        .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")
        .tab_2().append("public Resp<Empty>").append(" import" + entity.getProperties().getWebEntityVarUpperCamel()).append("WithReplaceAll(").append("List<").append(entity.getProperties().getWebEntityType()).append("ExcelModel> ").append(entity.getProperties().getWebEntityVar() + "ExcelModelList").append("){").lineBreak();

      result.tab_2().tab_2().line("List<" + entity.getProperties().getWebEntityType() + "ExcelModel> rowList = " + entity.getProperties().getWebEntityVar() + "ExcelModelList.stream()");
      result.tab_2().tab_2().line("// TODO 修正Excel行过滤规则,去掉不符合条件的数据");
      result.tab_2().tab_2().line(".collect(Collectors.toList());").lineBreak();

      if (entity.hasUniqueCode()) {
        PDManerEntityField uniqueCodeField = entity.uniqueCodeField();

        result.tab_2().tab_2().line("long " + uniqueCodeField.aliasCamelCaseFirst() + "Count = rowList.stream()");
        result.tab_2().tab_2().tab_2().line("// TODO 修正去重规则");
        result.tab_2().tab_2().tab_2().line(".map(" + entity.getProperties().getWebEntityType() + "ExcelModel::" + uniqueCodeField.aliasGetterFirst() + ").distinct().count();");
        result.tab_2().tab_2().line("if (rowList.size() != " + uniqueCodeField.aliasCamelCaseFirst() + "Count) {");
        result.tab_2().tab_2().tab_2().line("return Resp.ofBusinessErrorMessage(\"存在重复的" + uniqueCodeField.getDefName() + "\");");
        result.tab_2().tab_2().line("}");
      }
      result.tab_2().tab_2().line("// 1. 移除所有历史");
      result.tab_2().tab_2().line("// lambdaUpdate().remove();").lineBreak();
      result.tab_2().tab_2().line("// 2. 导入所有");
      result.tab_2().tab_2().line("List<" + entity.getProperties().getWebEntityType() + "> dataList = rowList.stream().map(row -> {");
      result.tab_2().tab_2().tab_2().line("" + entity.getProperties().getWebEntityType() + " " + entity.getProperties().getWebEntityVar() + " = new " + entity.getProperties().getWebEntityType() + "();");

      entity.getFields().stream().filter(PDManerEntityField::isInAddReq)
        .forEach(field -> {

          result.tab_2().tab_2().tab_2().line("" + entity.getProperties().getWebEntityVar() + "." + field.getFieldSetter() + "(row." + field.aliasGetterFirst() + "()); // " + field.getDefName() + "");
        });

      result.tab_2().tab_2().tab_2().line("return " + entity.getProperties().getWebEntityVar() + ";");
      result.tab_2().tab_2().line("}).collect(Collectors.toList());").lineBreak();
      result.tab_2().tab_2().line("Assert.isTrue(saveBatch(dataList));").lineBreak();
      result.tab_2().tab_2().line("return Resp.ofSuccess();");

      result.tab_2().line("}").lineBreak().lineBreak();
    }


    // findListBy 方法生成
    entity.fieldMethod("findListBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");
        result.tab_2().append("public List<").append(entity.getProperties().getWebEntityType()).append("> findListBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").list();").lineBreak()

          .tab_2().line("}")

          .lineBreak();
      });


    // findByList 方法生成
    entity.fieldMethod("findByList")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");
        result.tab_2().append("public List<").append(entity.getProperties().getWebEntityType()).append("> findListBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append("> ").append(field.aliasCamelCaseFirst()).append("List){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "List),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "List]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().in(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append("List).list();").lineBreak()

          .tab_2().line("}")

          .lineBreak();
      });


    // findOneBy 方法生成
    entity.fieldMethod("findOneBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");

        result.tab_2().append("public ").append(entity.getProperties().getWebEntityType()).append(" findOneBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").one();").lineBreak()

          .tab_2().line("}").lineBreak()


          .lineBreak();
      });


    // findOptBy 方法生成
    entity.fieldMethod("findOptBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");

        result.tab_2().append("public Optional<").append(entity.getProperties().getWebEntityType()).append("> findOptBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak();

        if (StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(), "Long", "Integer")) {
          result.tab_2().tab_2().line("if (Objects.isNull(" + field.aliasCamelCaseFirst() + ") || " + field.aliasCamelCaseFirst() + " == 0) {")
            .tab_2().tab_2().tab_2().line("// " + field.getDefName() + " 为空,直接返回")
            .tab_2().tab_2().tab_2().line("return Optional.empty();")
            .tab_2().tab_2().line("}");
        } else {
          result.tab_2().tab_2().line("if (ObjectUtil.isEmpty(" + field.aliasCamelCaseFirst() + ")) {")
            .tab_2().tab_2().tab_2().line("return Optional.empty();")
            .tab_2().tab_2().line("}");
        }


        result.tab_2().tab_2().append("return lambdaQuery().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").oneOpt();").lineBreak()

          .tab_2().line("}").lineBreak()


          .lineBreak();
      });


    // countBy 方法生成
    entity.fieldMethod("countBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 计数 " + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");

        result.tab_2().append("public ").append("Long").append(" countBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").count();").lineBreak()

          .tab_2().line("}").lineBreak()


          .lineBreak();
      });


    // countByList 方法生成
    entity.fieldMethod("countByList")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 计数 " + entity.getDefName());
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS)");

        result.tab_2().append("public ").append("Long").append(" countBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append("> ").append(field.aliasCamelCaseFirst()).append("List){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "List),\"" + field.getDefName() + "List[" + field.aliasCamelCaseFirst() + "List]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().in(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append("List).count();").lineBreak()

          .tab_2().line("}").lineBreak()


          .lineBreak();
      });

    // existsCheckBy 方法生成
    entity.fieldMethod("existsCheckBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 判断 " + entity.getDefName() + " 是否存在");
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");

        result.tab_2().append("public ").append("Boolean").append(" existsCheckBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak()

          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");")

          .tab_2().tab_2().append("return lambdaQuery().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").exists();").lineBreak()

          .tab_2().line("}").lineBreak()


          .lineBreak();
      });


    // ExistsCheckByIgnoreCurrent 方法生成
    entity.fieldMethod("ExistsCheckByIgnoreCurrent")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 判断 " + entity.getDefName() + " 是否存在, 忽略当前对象");
        result.tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.SUPPORTS,rollbackFor = Exception.class)");


        result
          .tab_2().lineTemplate("public Boolean existsCheckBy{}IgnoreCurrent({} {}, {} {}){", field.aliasPascalCaseFirst(), field.getFieldJavaType(), field.aliasCamelCaseFirst(), entity.primaryKeyField().getFieldJavaType(), entity.primaryKeyField().aliasCamelCaseFirst())


          .tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");").lineBreak()

          .tab_2().tab_2().line("return lambdaQuery()")
          .tab_2().tab_2().tab_2().lineTemplate(".eq({}::{},{})", entity.entityClassName(), field.aliasGetterFirst(), field.aliasCamelCaseFirst())
          .tab_2().tab_2().tab_2().lineTemplate(field.getStrNotNull(), ".ne({}::{},StrUtil.EMPTY)", entity.entityClassName(), field.aliasGetterFirst())
          .tab_2().tab_2().tab_2().lineTemplate(".ne(StrUtil.isNotBlank({}),{}::{},{})", entity.primaryKeyField().aliasCamelCaseFirst(), entity.entityClassName(), entity.primaryKeyField().aliasGetterFirst(), entity.primaryKeyField().aliasCamelCaseFirst())
          .tab_2().tab_2().tab_2().line(".exists();")


          .tab_2().line("}").lineBreak()


          .lineBreak();
      });


    // delBy 方法生成
    entity.fieldMethod("delBy")
      .forEach(field -> {

        result.tab_2().append(" /** 根据 ").append(field.getDefName()).append(" 删除 ").append(entity.getDefName()).append(" */").lineBreak()

          .tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")

          .tab_2().append("public void ").append("delBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).space().append(field.aliasCamelCaseFirst()).append("){").lineBreak().lineBreak();

        if (StrUtil.equalsAnyIgnoreCase(field.getFieldJavaType(), "Long", "Integer")) {
          result.tab_2().tab_2().line("Preconditions.checkArgument(Objects.isNull(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");").lineBreak();
        } else {
          result.tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "),\"" + field.getDefName() + "[" + field.aliasCamelCaseFirst() + "]不能为空\");").lineBreak();
        }


        result.tab_2().tab_2().append("lambdaUpdate().eq(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append(").remove();").lineBreak();

        result.tab_2().line("}")
          .lineBreak().lineBreak();
      });


    // delByList 方法生成
    entity.fieldMethod("delByList")
      .forEach(field -> {

        result.tab_2().append(" /** 根据 ").append(field.getDefName()).append(" 删除 ").append(entity.getDefName()).append(" */").lineBreak()

          .tab_2().line("@Override")
          .tab_2().line("@Transactional(propagation = Propagation.REQUIRED,rollbackFor = Exception.class)")

          .tab_2().append("public void ").append("delBy").append(field.aliasPascalCaseFirst()).append("(List<").append(field.getFieldJavaType()).append(">").space().append(field.aliasCamelCaseFirst()).append("List){").lineBreak().lineBreak();

        result.tab_2().tab_2().line("Preconditions.checkArgument(ObjectUtil.isNotEmpty(" + field.aliasCamelCaseFirst() + "List),\"" + field.getDefName() + "List[" + field.aliasCamelCaseFirst() + "List]不能为空\");").lineBreak();


        result.tab_2().tab_2().append("lambdaUpdate().in(")
          .append(entity.getProperties().getWebEntityType()).append("::").append(field.aliasGetterFirst()).append(", ").append(field.aliasCamelCaseFirst()).append("List).remove();").lineBreak();

        result.tab_2().line("}")
          .lineBreak().lineBreak();
      });


    result.append("}");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.javaModularPackage() + ".service.impl";
    File javaRoot = new File(GenUtils.getGenConfig().getBackendLocation(), "main/java");
    File fileDir = new File(javaRoot, subPackage.replace(".", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "ServiceImpl.java");

    return file.getAbsolutePath();
  }
}
