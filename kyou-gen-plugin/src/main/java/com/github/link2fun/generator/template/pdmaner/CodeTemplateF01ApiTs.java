package com.github.link2fun.generator.template.pdmaner;


import cn.hutool.core.convert.Convert;
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
public class CodeTemplateF01ApiTs implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 51;
  }

  @Override
  public String getTemplateCode() {
    return "F01_API_TS";
  }

  @Override
  public String getTemplateName() {
    return "[前端] 请求接口定义";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("import { get, postForm, postJSON } from '@/utils/request';");
    result.line("import WebConst from '@/utils/WebConst';");
    result.line("        ");
    result.line("const Api" + entity.getProperties().getWebFunctionType() + " = {");
    result.line("  /** 添加 " + entity.getDefName() + " */");
    result.line("  add" + entity.getProperties().getWebEntityVarUpperCamel() + ": (data: any) =>");
    result.line("    postJSON(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/add" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { ...data }),");
    result.line("        ");
    result.line("  /** 修改 " + entity.getDefName() + " */");
    result.line("  modify" + entity.getProperties().getWebEntityVarUpperCamel() + ": (data: any) =>");
    result.line("    postJSON(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/modify" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { ...data }),");
    result.line("        ");
    result.line("  /** 删除 " + entity.getDefName() + " */");
    result.line("  del" + entity.getProperties().getWebEntityVarUpperCamel() + ": (" + entity.getProperties().getWebEntityVar() + "Id: string, revision?: number) =>");
    result.line("    postForm(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/del" + entity.getProperties().getWebEntityVarUpperCamel() + "`, {");
    result.line("      " + entity.getProperties().getWebEntityVar() + "Id,");
    result.line("      revision,");
    result.line("    }),");
    result.line("        ");
    result.line("  /** 查看 " + entity.getDefName() + " */");
    result.line("  select" + entity.getProperties().getWebEntityVarUpperCamel() + ": (" + entity.getProperties().getWebEntityVar() + "Id: string) =>");
    result.line("    get(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/select" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { " + entity.getProperties().getWebEntityVar() + "Id }),");
    result.line("        ");
    result.line("  /** 分页查询 " + entity.getDefName() + " */");
    result.line("  pageSearch" + entity.getProperties().getWebEntityVarUpperCamel() + ": (req: any) =>");
    result.line("    get(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/pageSearch" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { ...req }),");
    result.line("        ");
    if (Convert.toBool(entity.getProperties().getUseListSearch(), false)) {
      result.line("  /** 列表查询 " + entity.getDefName() + " */");
      result.line("  listSearch" + entity.getProperties().getWebEntityVarUpperCamel() + ": (req: any) =>");
      result.line("    get(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/listSearch" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { ...req }),");

    }
    result.line("  /** 批量操作 " + entity.getDefName() + " */");
    result.line("  batchAction" + entity.getProperties().getWebEntityVarUpperCamel() + ": (data: any) =>");
    result.line("    postJSON(`${WebConst.API_PREFIX}/" + viewGroup.getProperties().getModuleCodeLowerCamel() + "/" + entity.getProperties().getWebEntityVar() + "/batchAction" + entity.getProperties().getWebEntityVarUpperCamel() + "`, { ...data }),");
    result.line("        ");

    result.line("};");
    result.line("        ");
    result.line("export default Api" + entity.getProperties().getWebFunctionType() + ";");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName();
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "services");
    File fileDir = new File(javaRoot, subPackage);

    File file = new File(fileDir, "Api" + tableInfo.getEntity().getProperties().getWebFunctionType() + ".ts");

    return file.getAbsolutePath();
  }
}
