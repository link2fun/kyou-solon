package com.github.link2fun.generator.template.pdmaner;


import com.github.link2fun.generator.config.GenConfig;
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
public class CodeTemplateB10WebApi implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 9;
  }

  @Override
  public String getTemplateCode() {
    return "B10_WEB_API";
  }

  @Override
  public String getTemplateName() {
    return "[Java] Controller";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    // 定义包名
    result.append("package ").append(tableInfo.javaModularPackage())
      .packageSep().append("api").append(";")
      .lineBreak().lineBreak();

    result.importClass("cn.dev33.satoken.annotation.*");
    result.importClass("cn.hutool.core.util.StrUtil");
    result.importClass("io.swagger.annotations.*");
    result.importClass("org.apache.poi.ss.usermodel.Workbook");
    result.importClass("org.springframework.validation.annotation.Validated");
    result.importClass("org.springframework.web.bind.annotation.*");
    result.importClass("com.github.liaochong.myexcel.core.DefaultExcelBuilder");
    result.importClass("com.github.liaochong.myexcel.core.DefaultExcelReader");
    result.importClass("com.github.liaochong.myexcel.utils.AttachmentExportUtil");

    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.exception.DataNotFoundException");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Empty");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Page");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.model.Resp");
    result.importClass(tableInfo.getSupportModulePackage() + ".support.core.annotation.BusinessLog");
    result.importClass("org.springframework.beans.factory.annotation.Autowired");
    result.importClass(tableInfo.javaModularPackage() + ".model." + entity.entityClassName());
    result.importClass(tableInfo.javaModularPackage() + ".service.I" + entity.getProperties().getWebFunctionType() + "Service");
    result.importClass(tableInfo.javaModularPackage() + ".model.req." + entity.entityClassName() + "Req");
    result.importClass(tableInfo.javaModularPackage() + ".model.vo." + entity.entityClassName() + "VO");
    result.importClass(tableInfo.javaModularPackage() + ".model.excel." + entity.entityClassName() + "ExcelModel");

    result.importClass("java.util.List");
    result.importClass("java.util.Objects;");
    result.importClass("java.io.IOException");
    result.importClass("java.util.stream.Collectors");

    result.lineBreak();


    // 类注释
    result.oneLineComment(viewGroup.getDefName() + " - " + entity.getDefName() + " Api");
    // 接口声明
    // 如果启用了 swagger 则加上 swagger @Api 注解
    GenConfig genConfig = GenUtils.getGenConfig();
    result.line(genConfig.getSwaggerEnable(), "@Api(value = \"" + viewGroup.getDefName() + "_" + entity.getDefName() + "\")");
    result.line("@Validated");
    result.line("@Controller");
    result.line("@Mapping(path = \"/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "\")");
    result.append("public class ").append(entity.getJavaApi()).append(" extends BaseController").append(" {")
      .lineBreak()
      .lineBreak();


    // 注入变量
    result.tab_2().line("@Inject")
      .tab_2().line("private " + entity.getJavaServiceItfClassName() + " " + entity.getJavaServiceItfVar() + ";").lineBreak();


    if (entity.hasEntityMethod("pageSearch")) {
      // 分页查询   ======================================================
      result
        .tab_2().line(" /** 分页查询 " + entity.getDefName() + " */")
        .tab_2().line(genConfig.getSwaggerEnable(), "@ApiOperation(value = \"分页查询" + entity.getDefName() + "\")");


      // eg: @SaCheckPermission(value = {"system:config:search"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})
      result.tab_2().lineTemplate("@SaCheckPermission(value = {\"{}:search\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})", tableInfo.permissionKey());


      // eg: @Mapping("/list", method = MethodType.GET)
      result.tab_2().line("@Mapping(\"/list\", method = MethodType.GET)");

      // eg: public TableDataInfo list(SysConfigReq.SearchReq searchReq){
      result.tab_2().lineTemplate("public TableDataInfo list({} searchReq){", entity.entityReqSearchReqClassName()).lineBreak();

      // eg: ActionContext context = ActionContext.current();
      result.tab_2().tab_2().line("ActionContext context = ActionContext.current(); // 加载上下文");


      result.lineTemplate("    // todo {} 需要使用 EasyQuery Idea 插件手动生成", entity.pageDTOClassName());
      // eg: Page<SysConfigPageDTO> page = configService.pageSearchConfig(context, Page.ofCurrentRequest(), searchReq, SysConfigPageDTO.class)
      result.tab_2().tab_2().line("Page<" + entity.pageDTOClassName() + "> page = " + entity.getProperties().getWebEntityVar() + "Service.pageSearch" + entity.entityVarUpperCamel() + "(context, Page.ofCurrentRequest(), searchReq, " + entity.pageDTOClassName() + ".class);");
      result.lineBreak();

      // eg: return getDataTable(page);
      result.tab_2().tab_2().line("return getDataTable(page);");


      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    // 2. 列表查询   ======================================================
    if (entity.hasEntityMethod("listSearch")) {
      result
        .tab_2().line(" /** 列表查询 " + entity.getDefName() + " */")
//                .tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":search\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@ApiOperation(value = \"列表查询" + entity.getDefName() + "\")")
        .tab_2().line("@GetMapping(\"/listSearch" + entity.entityVarUpperCamel() + "\")")
        .tab_2().append("public Resp<List<").append(entity.pageDTOClassName()).append(">> ")
        .append("listSearch").append(entity.entityVarUpperCamel()).append("(")
        .append(entity.entityClassName()).append("Req.SearchReq searchReq){")

        .tab_2().tab_2().lineBreak()
        .tab_2().tab_2().line("List<" + entity.entityClassName() + "> " + entity.vari() + "List = " + entity.vari() + "Service.listSearch" + entity.varUpper() + "(searchReq);")

        .lineBreak()
        .tab_2().tab_2().append("List<").append(entity.entityClassName()).append("VO> ").append(entity.vari()).append("VOList = EntityCacheHolder.loadRef(" + entity.entityVar() + "List, " + entity.entityClassName() + "VO.class);").lineBreak().lineBreak();


      result.tab_2().tab_2().line("return Resp.ofSuccessData(" + entity.vari() + "VOList);");


      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    // 3. 根据主键查询   ======================================================
    boolean hasId = entity.getFields().stream().anyMatch(PDManerEntityField::getPrimaryKey);
    if (hasId) {
      // 有主键，创建一个根据主键查询数据的方法

      PDManerEntityField primaryKeyField = entity.primaryKeyField();
      result.tab_2().line(" /** 根据 " + entity.getDefName() + "主键 查询" + entity.getDefName() + ", 并断言数据存在 */")
//                .tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":query\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@ApiOperation(value = \"查看" + entity.getDefName() + "\")")

        // restful 0
        .tab_2().line(genConfig.isRestfulLevel0(),"@Mapping(value = \"/detail\", method = MethodType.GET)")
        .line(genConfig.isRestfulLevel2(),"@Mapping(value = \"/{" + entity.primaryKeyVar() + "}\", method = MethodType.GET)")


        .tab_2().line("public Resp<" + entity.detailDTOClassName() + "> detail("+(genConfig.isRestfulLevel2()?"@Path ":"") + primaryKeyField.getFieldJavaType() + " " + entity.primaryKeyVar() + "){");

      result.lineBreak();

      if (primaryKeyField.isString()) {
        result.tab_2().tab_2().line("if (StrUtil.isBlank(" + entity.primaryKeyVar() + "Id)) {")
          .tab_2().tab_2().tab_2().line("throw new DataNotFoundException(\"" + entity.getDefName() + " 主键 不能为空\");")
          .tab_2().tab_2().line("}");
      } else {
        result.tab_2().tab_2().line("if (Objects.nonNull(" + entity.primaryKeyVar(), ")) {")
          .tab_2().tab_2().tab_2().line("throw new DataNotFoundException(\"" + entity.getDefName() + " 主键 不能为空\");")
          .tab_2().tab_2().line("}");
      }

      result.lineTemplate("    // todo {} 需要使用 EasyQuery Idea 插件手动生成", entity.detailDTOClassName());
      result.tab_2().tab_2().append(entity.detailDTOClassName()).space().append("detailDTO").append(" = ").append(entity.vari()).append("Service.findOneBy").append(entity.primaryKeyField().getFieldNameUpperCamel()).append("(").append(entity.primaryKeyVar()).append(", " + entity.detailDTOClassName() + ".class);").lineBreak()

        .lineBreak()

        .tab_2().tab_2().lineTemplate("return success({});", "detailDTO")

        .tab_2().line("}")
        .lineBreak();
    }


    // 4. 根据唯一编码查询 ======================================================
//    if (entity.hasUniqueCode()) {
//      entity.uniqueFieldList()
//        .forEach(uniqueField -> {
//          result.tab_2().oneLineComment("根据 " + uniqueField.getDefName() + " 查询" + entity.getDefName())
////                            .tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
//            .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":query\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
//            .tab_2().line("@ApiOperation(value = \"根据 " + uniqueField.getDefName() + " 查询 " + entity.getDefName() + "\")")
//            .tab_2().line("@GetMapping(\"/findBy" + uniqueField.aliasPascalCaseFirst() + "\")")
//            .tab_2().append("public Resp<").append(entity.entityType()).append("> findBy" + uniqueField.aliasPascalCaseFirst()).append("(").append(uniqueField.getFieldJavaType()).append(" ").append(uniqueField.aliasCamelCaseFirst()).append("){").lineBreak()
//            // 参数判断
//            .tab_2().tab_2().line("if (StrUtil.isBlank(" + uniqueField.aliasCamelCaseFirst() + ")) {")
//            .tab_2().tab_2().tab_2().line("throw new DataNotFoundException(\"" + uniqueField.getDefName() + " 不能为空\");")
//            .tab_2().tab_2().line("}")
//
//            .tab_2().tab_2()
//            .append(entity.entityType()).space().append(entity.getProperties().getWebEntityVar()).space().append("=")
//            .append(entity.getJavaServiceItfVar() + ".").append("findBy" + uniqueField.aliasPascalCaseFirst()).append("(").append(uniqueField.aliasCamelCaseFirst()).append(");").lineBreak()
//
//            .tab_2().tab_2().line("return Resp.ofSuccessDataNonNull(" + entity.getProperties().getWebEntityVar() + ");")
//
//
//            .tab_2().line("}")
//            .lineBreak().lineBreak();
//        });
//
//    }


    if (entity.hasEntityMethod("add")) {
      // 6. 新增  ======================================================
      result.tab_2().oneLineComment("添加" + entity.getDefName() + "")

//        .tab_2().line("@BusinessLog")
        .tab_2().line("@Log(title = \"" + entity.getDefName() + "\", businessType = BusinessType.INSERT)")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":add\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line(genConfig.getSwaggerEnable(), "@ApiOperation(value = \"添加" + entity.getDefName() + "\")")
        .tab_2().line("@Mapping(value = \"/add" + entity.entityVarUpperCamel() + "\", method = MethodType.POST)")
        .tab_2().append("public Resp<String>").append(" add" + entity.entityVarUpperCamel()).append("(@Validated @Body ").append(entity.entityClassName()).append("Req.AddReq ").append("addReq").append("){").lineBreak().lineBreak();

      // 返回结果
      result.tab_2().tab_2().line("ActionContext context = ActionContext.current();");


      result.tab_2().tab_2().line("return " + entity.getProperties().getWebEntityVar() + "Service.add" + entity.entityVarUpperCamel() + "(context, addReq);");


      result.tab_2().line("}")
        .lineBreak().lineBreak();
    }


    if (entity.hasEntityMethod("modify")) {
      // 6. 修改  ======================================================
      result.tab_2().oneLineComment("修改" + entity.getDefName() + "")
        .tab_2().line("@Log(title = \"" + entity.getDefName() + "\", businessType = BusinessType.UPDATE)")
//                .tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":edit\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line(genConfig.getSwaggerEnable(), "@ApiOperation(value = \"修改" + entity.getDefName() + "\")")
        .tab_2().line(genConfig.isRestfulLevel0(),"@Mapping(value = \"/edit\", method = MethodType.POST)")
        .tab_2().line(genConfig.isRestfulLevel2(),"@Mapping(value = \"\", method = MethodType.PUT)")
        .tab_2().append("public AjaxResult").append(" edit(@Validated @Body ").append(entity.entityClassName()).append("Req.ModifyReq ").append("modifyReq").append("){").lineBreak();


      // eg: ActionContext context = ActionContext.current();
      result.tab_2().tab_2().line("ActionContext context = ActionContext.current();");

      result.tab_2().tab_2().line("Boolean success " + entity.serviceVar() + ".modify" + entity.entityVarUpperCamel() + "(context, modifyReq);");

      result.tab_2().line("return toAjax(success);");

      result.tab_2().line("}").lineBreak()
        .lineBreak();
    }


    if (entity.hasEntityMethod("del")) {
      // 删除
      result.tab_2().oneLineComment("删除" + entity.getDefName() + "")
        .tab_2().line("@Log(title = \"" + entity.getDefName() + "\", businessType = BusinessType.DELETE)")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":del\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line(genConfig.getSwaggerEnable(), "@ApiOperation(value = \"删除" + entity.getDefName() + "\")")

        .tab_2().line(genConfig.isRestfulLevel0(),"@Mapping(value = \"/remove\", method = MethodType.POST)")
        .tab_2().line(genConfig.isRestfulLevel2(),"@Mapping(value = \"/{"+entity.primaryKeyVars()+"}\", method = MethodType.DELETE)");


      result.tab_2().append("public AjaxResult").append(" remove(List<").append(entity.primaryKeyField().getFieldJavaType()).append(">").space().append(entity.primaryKeyVars()).append("){").lineBreak().lineBreak();

      // eg: ActionContext context = ActionContext.current();
      result.tab_2().tab_2().line("ActionContext context = ActionContext.current();");

      // eg: return configService.removeConfig(context, configId);
      result.tab_2().tab_2().lineTemplate("{}.remove{}(context, {});", entity.serviceVar(), entity.entityVarUpperCamel(), entity.primaryKeyVars());

      // eg: return success();
      result.tab_2().tab_2().line("return success();");

      result.tab_2().line("}").lineBreak().lineBreak();
    }


    if (entity.hasEntityMethod("import")) {
      // 导入   ======================================================
      result.tab_2().oneLineComment("导入" + entity.getDefName() + "")
        .tab_2().line("@BusinessLog")
//                .tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":import\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().line("@ApiOperation(value = \"导入" + entity.getDefName() + "\")")
        .tab_2().line("@PostMapping(path = \"/import" + entity.entityVarUpperCamel() + "\")")
        .tab_2().append("public Resp<Empty>").append(" import" + entity.entityVarUpperCamel()).append("WithReplaceAll(@RequestPart(\"file\") MultipartFile file) throws IOException {").lineBreak();

      result.lineBreak()
        .tab_2().tab_2().append("List<").append(entity.entityClassName()).append("ExcelModel> ").append(entity.getProperties().getWebEntityVar()).append("List = DefaultExcelReader.of(").append(entity.entityClassName()).append("ExcelModel.class)").lineBreak()
        .tab_2().tab_2().tab_2().append(".rowFilter(row->row.getRowNum()>=1)").lineBreak()
        .tab_2().tab_2().tab_2().append(".read(file.getInputStream());").lineBreak();

      result.lineBreak()
        .tab_2().tab_2().append("return ").append(entity.getProperties().getWebEntityVar()).append("Service.import").append(entity.entityVarUpperCamel()).append("WithReplaceAll(").append(entity.getProperties().getWebEntityVar()).append("List);").lineBreak();


      result.tab_2().line("}").lineBreak().lineBreak();
    }


    if (entity.hasEntityMethod("export")) {
      // 导出 ===================================================
      result.tab_2().oneLineComment("导出 " + entity.getDefName());
//            result.tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
      result.tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":export\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        .tab_2().append("@ApiOperation(value = \"导出").append(entity.getDefName()).append("\")").lineBreak()
        .tab_2().append("@GetMapping(value = \"/export").append(entity.entityVarUpperCamel()).append("\")").lineBreak()
        .tab_2().append("public void export").append(entity.entityVarUpperCamel()).append("(").append(entity.entityReqSearchReqClassName()).append(" searchReq, HttpServletResponse response) {").lineBreak()
        .tab_2().tab_2().append("List<").append(entity.entityClassName()).append("ExcelModel> dataList = ").append(entity.serviceVar()).append(".list().stream().map(model -> {").lineBreak()
        .tab_2().tab_2().tab_2().append(entity.entityClassName()).append("ExcelModel excelModel = new ").append(entity.entityClassName()).append("ExcelModel();").lineBreak().lineBreak();

      entity.getFields().stream().filter(field -> field.isInAddReq() || field.isInModifyReq())
        .filter(field -> !field.getIsVersion() && !field.getPrimaryKey())
        .forEach(field -> {
          result.tab_2().tab_2().tab_2().append("excelModel.").append(field.getFieldSetter()).append("(model.").append(field.getFieldGetter()).append("()); // ").append(field.getDefName()).lineBreak();
        });

      result.lineBreak().tab_2().tab_2().tab_2().line("return excelModel;")
        .tab_2().tab_2().line("}).collect(Collectors.toList());")

        .tab_2().tab_2().append("Workbook workbook = DefaultExcelBuilder.of(").append(entity.entityClassName()).append("ExcelModel.class)").lineBreak()
        .tab_2().tab_2().tab_2().append(".build(dataList);").lineBreak().lineBreak()

        .tab_2().tab_2().append("AttachmentExportUtil.export(workbook, \"").append(entity.getDefName()).append("\", response);").lineBreak()
        .tab_2().append("}").lineBreak().lineBreak();
    }


    // findListBy 方法生成
    entity.findListBy()
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
//                result.tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        result.tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":search\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
          .tab_2().append("@ApiOperation(value = \"根据").append(field.getDefName() + " 查找 ").append(entity.getDefName()).append("\")").lineBreak();

        result.tab_2().line("@GetMapping(\"/findListBy" + field.aliasPascalCaseFirst() + "\")");
        result.tab_2().append("public Resp<List<").append(entity.entityClassName()).append(">> findListBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak()
          .tab_2().tab_2().append("List<").append(entity.entityClassName()).append("> dataList = ").append(entity.getJavaServiceItfVar()).append(".findListBy").append(field.aliasPascalCaseFirst()).append("(").append(field.aliasCamelCaseFirst()).append(");").lineBreak()

          .tab_2().tab_2().line("return Resp.ofSuccessDataNonNull(dataList);")


          .tab_2().line("}")
          .lineBreak()
        ;
      });


    // findOneBy 方法生成
    entity.fieldMethod("findOneBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找" + entity.getDefName());
//                result.tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        result.tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":search\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
          .tab_2().append("@ApiOperation(value = \"根据").append(field.getDefName() + " 查找 ").append(entity.getDefName()).append("\")").lineBreak();

        result.tab_2().line("@GetMapping(\"/findOneBy" + field.aliasPascalCaseFirst() + "\")");

        result.tab_2().append("public Resp<").append(entity.entityClassName()).append("> findOneBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak()
          .tab_2().tab_2().append("").append(entity.entityClassName()).append(" data = ").append(entity.getJavaServiceItfVar()).append(".findOneBy").append(field.aliasPascalCaseFirst()).append("(").append(field.aliasCamelCaseFirst()).append(");").lineBreak()
          .tab_2().tab_2().line("return Resp.ofSuccessDataNonNull(data);")


          .tab_2().line("}")
          .lineBreak();
      });

    // findOptBy 方法生成
    entity.fieldMethod("findOptBy")
      .forEach(field -> {
        result.tab_2().oneLineComment("根据 " + field.getDefName() + " 查找 " + entity.getDefName());
//                result.tab_2().line("@SaCheckRole(value = {}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
        result.tab_2().line("@SaCheckPermission(value = {\"" + tableInfo.permissionKey() + ":search\"}, mode = SaMode.OR, orRole = {RoleConst.PLATFORM_ADMIN})")
          .tab_2().append("@ApiOperation(value = \"根据").append(field.getDefName() + " 查找 ").append(entity.getDefName()).append("\")").lineBreak();

        result.tab_2().line("@GetMapping(\"/findOptBy" + field.aliasPascalCaseFirst() + "\")");

        result.tab_2().append("public Resp<Optional<").append(entity.entityClassName()).append(">> findOptBy").append(field.aliasPascalCaseFirst()).append("(").append(field.getFieldJavaType()).append(" ").append(field.aliasCamelCaseFirst()).append("){").lineBreak()

          .tab_2().tab_2().append("Optional<").append(entity.entityClassName()).append("> data = ").append(entity.getJavaServiceItfVar()).append(".findOptBy").append(field.aliasPascalCaseFirst()).append("(").append(field.aliasCamelCaseFirst()).append(");").lineBreak()
          .tab_2().tab_2().line("return Resp.ofSuccessData(data);")


          .tab_2().line("}")
          .lineBreak();
      });


    result.append("}");

    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.javaModularPackage() + ".api";
    File javaRoot = new File(GenUtils.getGenConfig().getBackendLocation(), "main/java");
    File fileDir = new File(javaRoot, subPackage.replace(".", "/"));

    File file = new File(fileDir, "" + tableInfo.getEntity().getProperties().getWebFunctionType() + "Controller.java");

    return file.getAbsolutePath();
  }
}
