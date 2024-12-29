package com.github.link2fun.generator.template.pdmaner;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
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
public class CodeTemplateF02DataTs implements CodeTemplate {
  @Override
  public Integer getTemplateIndex() {
    return 52;
  }

  @Override
  public String getTemplateCode() {
    return "F02_DATA_D_TS";
  }

  @Override
  public String getTemplateName() {
    return "[前端] 类型定义";
  }

  @Override
  public String getTemplateResult(TableInfo tableInfo) {
    CodeBuilder result = CodeBuilder.newBuilder();

    PDManerEntity entity = tableInfo.getEntity();
    PDManerViewGroup viewGroup = tableInfo.getViewGroup();


    result.line("import type { FlowControl } from '@/models/typing';");
    result.line("import type { PaginationProps } from 'antd/es/pagination';");
    result.line("import type { Effect, Reducer } from '@@/plugin-dva/connect';").lineBreak();

    result.line("export interface " + entity.getProperties().getWebEntityType() + " {");

    entity.getFields().stream().filter(PDManerEntityField::isInModifyReq)
      .forEach(field -> {
        result.tab_2().line("/** " + field.getDefName() + " */");
        result.tab_2().append(field.fieldName());
        if (!field.getNotNull()) {
          result.append("?:");
        } else {
          result.append(":");
        }
        result.append(" " + field.getFieldTsType() + ";").lineBreak();
      });

    result.line("}").lineBreak();

    result.line("export interface " + entity.getProperties().getWebEntityType() + "QueryCondition {");

    entity.getFields().stream().filter(PDManerEntityField::isInSearchReq)
      .forEach(field -> {

        if (StrUtil.equalsAny(field.getFieldJavaType(), "LocalDateTime", "LocalDate")) {
          // Start
          result.tab_2().line("/** " + field.getDefName() + " 起 */");
          result.tab_2().append(field.fieldName() + "Start");
          if (!field.getNotNull()) {
            result.append("?:");
          } else {
            result.append(":");
          }
          result.append(" " + field.getFieldTsType() + ";").lineBreak();

          // End
          result.tab_2().line("/** " + field.getDefName() + " 止 */");
          result.tab_2().append(field.fieldName() + "End");
          if (!field.getNotNull()) {
            result.append("?:");
          } else {
            result.append(":");
          }
          result.append(" " + field.getFieldTsType() + ";").lineBreak();
        } else {
          result.tab_2().line("/** " + field.getDefName() + " */");
          result.tab_2().append(field.fieldName());
          if (!field.getNotNull()) {
            result.append("?:");
          } else {
            result.append(":");
          }
          result.append(" " + field.getFieldTsType() + ";").lineBreak();
        }

      });

    result.line("}").lineBreak();

    result.line("export interface " + entity.getProperties().getWebFunctionType() + "ModelState {");
    result.line("  " + entity.getProperties().getWebEntityVar() + "List: " + entity.getProperties().getWebEntityType() + "[];");
    result.line("  currentModel: Partial<" + entity.getProperties().getWebEntityType() + ">;");
    result.line("  flowControl: FlowControl;");
    result.line("  queryCondition: Partial<" + entity.getProperties().getWebEntityType() + "QueryCondition>;");
    result.line("  pagination: Partial<PaginationProps>;");
    result.line("}").lineBreak();

    result.line("export interface " + entity.getProperties().getWebFunctionType() + "Model {");
    result.line("  namespace: '" + entity.getProperties().getWebFunctionVar() + "';");
    result.line("  state: " + entity.getProperties().getWebFunctionType() + "ModelState;");
    result.line("  effects: {");
    result.line("    /** 准备修改 */");
    result.line("    prepareModify: Effect;");

    result.line("    /** 执行添加 */");
    result.line("    processAdd: Effect;");
    result.line("    /** 执行修改 */");
    result.line("    processModify: Effect;");
    result.line("    /** 执行删除 */");
    result.line("    processDel: Effect;");

    result.line("    /** 执行分页搜索 */");
    result.line("    processPageSearch: Effect;");
    if (Convert.toBool(entity.getProperties().getUseListSearch(), false)) {
      result.line("    /** 执行列表搜索 */");
      result.line("    processListSearch: Effect;");

    }
    result.line("    /** 执行刷新数据（使用现有条件） */");
    result.line("    processRefresh: Effect;");
    result.line("  };");
    result.line("  reducers: {");
    result.line("    /** 准备查询条件 */");
    result.line("    prepareQueryCondition: Reducer<" + entity.getProperties().getWebFunctionType() + "ModelState>;");
    result.line("    /** 解析分页搜索结果 */");
    result.line("    resolvePageSearch: Reducer<" + entity.getProperties().getWebFunctionType() + "ModelState>;");
    if (Convert.toBool(entity.getProperties().getUseListSearch(), false)) {
      result.line("    /** 解析列表搜索结果 */");
      result.line("    resolveListSearch: Reducer<" + entity.getProperties().getWebFunctionType() + "ModelState>;");
    }

    result.line("    /** 暂存当前操作对象 */");
    result.line("    saveCurrentModel: Reducer<" + entity.getProperties().getWebFunctionType() + "ModelState>;");
    result.line("    /** 更新流程控制 */");
    result.line("    changeFlowControl: Reducer<" + entity.getProperties().getWebFunctionType() + "ModelState>;");
    result.line("  };");
    result.line("}");


    return result.toString();
  }

  @Override
  public String getFilePath(TableInfo tableInfo) {
    String subPackage = tableInfo.getViewGroup().getProperties().getDirName() + "/" + tableInfo.getEntity().getProperties().getWebEntityVar();
    File javaRoot = new File(GenUtils.getGenConfig().getFrontendLocation(), "pages");
    File fileDir = new File(javaRoot, subPackage.replace("\\.", "/"));

    File file = new File(fileDir, "data.d.ts");

    return file.getAbsolutePath();
  }
}
